package sh.talonfox.ravenstone.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import sh.talonfox.ravenstone.Ravenstone;
import sh.talonfox.ravenstone.blocks.peripherals.PeripheralBlockEntity;
import sh.talonfox.ravenstone.blocks.upgrades.RAMUpgradeBlock;
import sh.talonfox.ravenstone.blocks.upgrades.RAMUpgradeBlockEntity;
import sh.talonfox.ravenstone.processor.Processor;
import sh.talonfox.ravenstone.processor.ProcessorHost;

import java.util.Objects;

import static sh.talonfox.ravenstone.blocks.ComputerBlock.RUNNING;

public class ComputerBlockEntity extends PeripheralBlockEntity implements ProcessorHost {
    public Processor CPU = new Processor(this);
    public byte[] RAM = new byte[8192]; // Only 8 KiB can be accessed without an upgrade
    private PeripheralBlockEntity CachedPeripheral = null;
    private RAMUpgradeBlockEntity CachedUpgrade = null;

    public ComputerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockRegister.RAVEN_COMPUTER_ENTITY, pos, state, 0);
    }

    @Override
    public byte memRead(short at) {
        if(Short.toUnsignedInt(at) >= 0xff00) {
            return Processor.ROM[(Short.toUnsignedInt(at) - 0xff00)];
        } else if(Short.toUnsignedInt(at) < 8192) {
            if(at >= 0x200 && at <= 0x2FF) { // Bus Read
                var peripheral = CachedPeripheral==null?PeripheralBlockEntity.findPeripheral(this.getWorld(),this.getPos(),CPU.BusOffset):CachedPeripheral;
                if(peripheral != null) {
                    if(CachedPeripheral == null)
                        CachedPeripheral = peripheral;
                    return peripheral.readData((byte) (at - 0x200));
                }
            } else {
                return RAM[Short.toUnsignedInt(at)];
            }
        } else {
            if(CachedUpgrade != null)
                return CachedUpgrade.readData((short)(Short.toUnsignedInt(at)-8192));
        }
        return (byte)0xFF;
    }

    @Override
    public void memStore(short at, byte data) {
        if(Short.toUnsignedInt(at) < 8192) {
            if(at >= 0x200 && at <= 0x2FF) { // Bus Page
                var peripheral = CachedPeripheral==null?PeripheralBlockEntity.findPeripheral(this.getWorld(),this.getPos(),CPU.BusOffset):CachedPeripheral;
                if(peripheral != null) {
                    if(CachedPeripheral == null)
                        CachedPeripheral = peripheral;
                    peripheral.storeData((byte) (at - 0x200), data);
                }
            } else if(Short.toUnsignedInt(at) < 0xFF00) {
                RAM[Short.toUnsignedInt(at)] = data;
            }
        } else if(Short.toUnsignedInt(at) < 0xFF00) {
            if(CachedUpgrade != null)
                CachedUpgrade.storeData((short)(Short.toUnsignedInt(at)-8192),data);
        }
    }
    public void explode() {
        BlockPos pos = this.getPos();
        Objects.requireNonNull(this.getWorld()).createExplosion(null, DamageSource.GENERIC.setExplosive(), null,(double)pos.getX()+0.5,(double)pos.getY()+0.5,(double)pos.getZ()+0.5,2F,false, Explosion.DestructionType.NONE);
    }
    public void invalidatePeripheral() {
        CachedPeripheral = null;
    }
    public static void tick(World world, BlockPos pos, BlockState state, ComputerBlockEntity blockEntity) {
        if(!world.isClient()) {
            if (!blockEntity.CPU.Stop) {
                blockEntity.CPU.Wait = false;
                for (int i = 0; i < (100000 / 20); i++) {
                    blockEntity.CPU.next();
                    if (blockEntity.CPU.Stop) {
                        world.setBlockState(pos, state.with(RUNNING, !state.get(RUNNING)));
                        blockEntity.markDirty();
                        break;
                    } else if (blockEntity.CPU.Wait) {
                        break;
                    }
                }
            } else {
                BlockPos upgradePos = pos.add(state.get(Properties.HORIZONTAL_FACING).getOpposite().getVector());
                BlockState upgradeState = world.getBlockState(upgradePos);
                if (!upgradeState.isAir()) {
                    boolean isUpgrade = upgradeState.getBlock() instanceof RAMUpgradeBlock;
                    if (isUpgrade && blockEntity.CachedUpgrade == null) {
                        Ravenstone.LOGGER.info("RAM Upgrade Attached!");
                        blockEntity.CachedUpgrade = (RAMUpgradeBlockEntity)world.getBlockEntity(upgradePos);
                    } else if(!isUpgrade && blockEntity.CachedUpgrade != null) {
                        Ravenstone.LOGGER.info("RAM Upgrade Removed!");
                        blockEntity.CachedUpgrade = null;
                    }
                } else if(blockEntity.CachedUpgrade != null){
                    Ravenstone.LOGGER.info("RAM Upgrade Removed!");
                    blockEntity.CachedUpgrade = null;
                }
            }
        }
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        var processor = new NbtCompound();
        processor.putByte("A",CPU.A);
        processor.putByte("X",CPU.X);
        processor.putByte("Y",CPU.Y);
        processor.putShort("PC",CPU.PC);
        processor.putShort("SP",CPU.SP);
        processor.putShort("BrkAddr",CPU.BrkAddr);
        processor.putShort("ResetAddr",CPU.ResetAddr);
        processor.putInt("BusOffset",CPU.BusOffset);
        processor.putBoolean("Error",CPU.Error);
        processor.putBoolean("Stop",CPU.Stop);
        processor.putBoolean("Wait",CPU.Wait);
        processor.putBoolean("FlagC",CPU.FlagC);
        processor.putBoolean("FlagZ",CPU.FlagZ);
        processor.putBoolean("FlagI",CPU.FlagI);
        processor.putBoolean("FlagD",CPU.FlagD);
        processor.putBoolean("FlagV",CPU.FlagV);
        processor.putBoolean("FlagN",CPU.FlagN);

        tag.put("Processor",processor);
        tag.putByteArray("RAM", RAM);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        var processor = tag.getCompound("Processor");
        CPU.A = processor.getByte("A");
        CPU.X = processor.getByte("X");
        CPU.Y = processor.getByte("Y");
        CPU.PC = processor.getShort("PC");
        CPU.SP = processor.getShort("SP");
        CPU.BrkAddr = processor.getShort("BrkAddr");
        CPU.ResetAddr = processor.getShort("ResetAddr");
        CPU.BusOffset = processor.getInt("BusOffset");
        CPU.Error = processor.getBoolean("Error");
        CPU.Stop = processor.getBoolean("Stop");
        CPU.Wait = processor.getBoolean("Wait");
        CPU.FlagC = processor.getBoolean("FlagC");
        CPU.FlagZ = processor.getBoolean("FlagZ");
        CPU.FlagI = processor.getBoolean("FlagI");
        CPU.FlagD = processor.getBoolean("FlagD");
        CPU.FlagV = processor.getBoolean("FlagV");
        CPU.FlagN = processor.getBoolean("FlagN");
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