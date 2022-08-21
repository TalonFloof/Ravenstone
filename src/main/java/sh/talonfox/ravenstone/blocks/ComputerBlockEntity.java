package sh.talonfox.ravenstone.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import sh.talonfox.ravenstone.blocks.peripherals.PeripheralBlockEntity;
import sh.talonfox.ravenstone.blocks.upgrades.RAMUpgradeBlock;
import sh.talonfox.ravenstone.blocks.upgrades.RAMUpgradeBlockEntity;
import sh.talonfox.ravenstone.processor.Processor;
import sh.talonfox.ravenstone.processor.ProcessorHost;

import static sh.talonfox.ravenstone.blocks.ComputerBlock.RUNNING;

public class ComputerBlockEntity extends PeripheralBlockEntity implements ProcessorHost {
    public ItemStack CPUStack = Items.AIR.getDefaultStack();
    public byte[] RAM = new byte[8192]; // Only 8 KiB can be accessed without an upgrade
    private PeripheralBlockEntity CachedPeripheral = null;
    private RAMUpgradeBlockEntity CachedUpgrade = null;

    public ComputerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockRegister.RAVEN_COMPUTER_ENTITY, pos, state, 0);
    }

    public boolean insertCPU(ItemStack stack) {
        var world = this.getWorld();
        if(stack.isEmpty() || !(stack.getItem() instanceof Processor))
            return false;
        this.CPUStack = stack.split(1);
        assert world != null;
        if(!world.isClient()) {
            world.playSound(null, getPos(), SoundEvents.UI_BUTTON_CLICK, SoundCategory.BLOCKS, 0.25f, 2f);
            ((Processor)this.CPUStack.getItem()).setStop(true);
            world.setBlockState(getPos(),getCachedState().with(ComputerBlock.HAS_CPU, true));
            this.markDirty();
        }
        return true;
    }

    public boolean ejectCPU(boolean broken) {
        var world = this.getWorld();
        if(world == null)
            return false;
        if(world.isClient())
            return true;
        if(!broken) {
            var block_direction = Vec3d.of(this.getCachedState().get(Properties.HORIZONTAL_FACING).getOpposite().getVector());
            var block_position = Vec3d.ofCenter(pos).add(block_direction.multiply(0.75));
            var item = new ItemEntity(world, block_position.x, block_position.y, block_position.z, this.CPUStack);
            item.setVelocity(block_direction.multiply(0.1));
            world.spawnEntity(item);
            world.playSound(null, getPos(), SoundEvents.UI_BUTTON_CLICK, SoundCategory.BLOCKS, 0.25f, 1.9f);
            world.setBlockState(getPos(),getCachedState().with(ComputerBlock.HAS_CPU, false).with(ComputerBlock.RUNNING,false));
            this.markDirty();
        } else {
            ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), this.CPUStack);
        }
        this.CPUStack = Items.AIR.getDefaultStack();
        return true;
    }

    @Override
    public byte busRead(byte id, byte at) {
        var peripheral = CachedPeripheral==null?PeripheralBlockEntity.findPeripheral(this.getWorld(),this.getPos(),Byte.toUnsignedInt(id)):CachedPeripheral;
        if(peripheral != null) {
            if (CachedPeripheral == null)
                CachedPeripheral = peripheral;
            else if(CachedPeripheral.getBusID() != Byte.toUnsignedInt(id)) { // Just in case...
                CachedPeripheral = PeripheralBlockEntity.findPeripheral(this.getWorld(),this.getPos(),Byte.toUnsignedInt(id));
                if(CachedPeripheral == null)
                    return 0;
                else
                    peripheral = CachedPeripheral;
            }
            return peripheral.readData(at);
        }
        return 0;
    }

    @Override
    public void busWrite(byte id, byte at, byte val) {
        var peripheral = CachedPeripheral==null?PeripheralBlockEntity.findPeripheral(this.getWorld(),this.getPos(),Byte.toUnsignedInt(id)):CachedPeripheral;
        if(peripheral != null) {
            if (CachedPeripheral == null)
                CachedPeripheral = peripheral;
            else if(CachedPeripheral.getBusID() != Byte.toUnsignedInt(id)) {
                CachedPeripheral = PeripheralBlockEntity.findPeripheral(this.getWorld(),this.getPos(),Byte.toUnsignedInt(id));
                if(CachedPeripheral == null)
                    return;
                else
                    peripheral = CachedPeripheral;
            }
            peripheral.storeData(at,val);
        }
    }

    @Override
    public byte memRead(int at) {
        if(Integer.toUnsignedLong(at) < 8192) {
            return RAM[at];
        } else {
            if(CachedUpgrade != null)
                return CachedUpgrade.readData(at-8192);
        }
        return (byte)0xFF;
    }

    @Override
    public void memStore(int at, byte data) {
        if(Integer.toUnsignedLong(at) < 8192) {
            RAM[at] = data;
        } else {
            if(CachedUpgrade != null)
                CachedUpgrade.storeData(at-8192,data);
        }
    }
    public void invalidatePeripheral() {
        CachedPeripheral = null;
    }
    public static void tick(World world, BlockPos pos, BlockState state, ComputerBlockEntity blockEntity) {
        if(!world.isClient()) {
            if (!blockEntity.CPUStack.isEmpty()) {
                Processor CPU = (Processor)blockEntity.CPUStack.getItem();
                if (state.get(ComputerBlock.RUNNING)) {
                    CPU.setWait(false);
                    for (int i = 0; i < (CPU.insnPerSecond() / 20); i++) {
                        CPU.next(blockEntity);
                        if (CPU.isStopped()) {
                            world.setBlockState(pos, state.with(RUNNING, false));
                            blockEntity.markDirty();
                            break;
                        } else if (CPU.isWaiting()) {
                            break;
                        }
                    }
                    return;
                }
            }
            BlockPos upgradePos = pos.add(state.get(Properties.HORIZONTAL_FACING).getOpposite().getVector());
            BlockState upgradeState = world.getBlockState(upgradePos);
            if (!upgradeState.isAir()) {
                boolean isUpgrade = upgradeState.getBlock() instanceof RAMUpgradeBlock;
                if (isUpgrade && blockEntity.CachedUpgrade == null) {
                    blockEntity.CachedUpgrade = (RAMUpgradeBlockEntity)world.getBlockEntity(upgradePos);
                } else if(!isUpgrade && blockEntity.CachedUpgrade != null) {
                    blockEntity.CachedUpgrade = null;
                }
            } else if(blockEntity.CachedUpgrade != null){
                blockEntity.CachedUpgrade = null;
            }
        }
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        if(!CPUStack.isEmpty())
            ((Processor)(CPUStack.getItem())).saveNBT(CPUStack);
        tag.put("CPUItem", CPUStack.writeNbt(new NbtCompound()));
        tag.putByteArray("RAM",RAM);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        CPUStack = ItemStack.fromNbt(tag.getCompound("CPUItem"));
        if(!CPUStack.isEmpty())
            ((Processor)(CPUStack.getItem())).loadNBT(CPUStack);
        if(tag.getByteArray("RAM").length == RAM.length) {
            RAM = tag.getByteArray("RAM");
        }
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }
}