package sh.talonfox.ravenstone.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import sh.talonfox.ravenstone.items.FloppyDisk;
import sh.talonfox.ravenstone.sounds.SoundEventRegister;

import java.nio.charset.StandardCharsets;

/*
Some code used in this file is adapted from 2xsaiko's Retrocomputers mod.
The original code can be found here: https://github.com/2xsaiko/retrocomputers/blob/master/src/main/kotlin/common/block/DiskDrive.kt
Retrocomputers is under the MIT License.
 */

/*
Commands:
0x01: Restore (Seek to Track 0)
0x10: Seek
0x20: Retract Head
0x21: Engage Head
0x40: Step In
0x60: Step Out
0x80: Read
0xA0: Write
0xC4: Stop & Clear Data Buffer
0xE4: Read Label
0xF4: Write Label
 */

public class FloppyDriveBlockEntity extends PeripheralBlockEntity {
    private ItemStack stack = Items.AIR.getDefaultStack();
    private long spinTicks = 0;
    private int Command = 0;
    private int Flags = 0;
    private int SectorNumber = 0;
    private int TrackNumber = 0;
    private int CurrentTrack = 0;

    public FloppyDriveBlockEntity(BlockPos pos, BlockState state) {
        super(BlockRegister.RAVEN_FLOPPY_DRIVE_ENTITY, pos, state);
    }

    public boolean insertDisk(ItemStack stack) {
        var world = this.getWorld();
        if(stack.isEmpty() || !(stack.getItem() instanceof FloppyDisk))
            return false;
        this.stack = stack.split(1);
        assert world != null;
        world.setBlockState(getPos(), this.getCachedState().with(FloppyDriveBlock.HAS_DISK, true));
        world.setBlockState(getPos(), this.getCachedState().with(FloppyDriveBlock.LIGHT, false));
        if(!world.isClient()) {
            world.playSound(null, getPos(), SoundEventRegister.DISKETTE_INSERT_SOUND_EVENT, SoundCategory.BLOCKS, 0.25f, 1f);
        }
        return true;
    }

    public boolean ejectDisk(boolean broken) {
        var world = this.getWorld();
        if(world == null)
            return false;
        if(world.isClient())
            return true;
        if(!broken) {
            var block_direction = Vec3d.of(this.getCachedState().get(Properties.HORIZONTAL_FACING).getVector());
            var block_position = Vec3d.ofCenter(pos).add(block_direction.multiply(0.75));
            var item = new ItemEntity(world, block_position.x, block_position.y, block_position.z, stack);
            item.setVelocity(block_direction.multiply(0.1));
            world.spawnEntity(item);
            world.setBlockState(getPos(), this.getCachedState().with(FloppyDriveBlock.HAS_DISK, false));
            world.setBlockState(getPos(), this.getCachedState().with(FloppyDriveBlock.LIGHT, false));
            world.playSound(null, getPos(), SoundEventRegister.DISKETTE_EJECT_SOUND_EVENT, SoundCategory.BLOCKS, 0.25f, 1f);
        } else {
            ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), stack);
        }
        stack = Items.AIR.getDefaultStack();
        return true;
    }

    public static void tick(World world, BlockPos pos, BlockState state, FloppyDriveBlockEntity blockEntity) {
        if(world.isClient())
            return;
        if(state.get(FloppyDriveBlock.HAS_DISK) && state.get(FloppyDriveBlock.LIGHT)) {
            if (blockEntity.spinTicks == 0) {
                world.playSound(null, pos, SoundEventRegister.DISKETTE_START_SOUND_EVENT, SoundCategory.BLOCKS, 1f, 1f);
            } else if (blockEntity.spinTicks >= 4) {
                if (((blockEntity.spinTicks - 4) % 24) == 0) {
                    world.playSound(null, pos, SoundEventRegister.DISKETTE_SPIN_SOUND_EVENT, SoundCategory.BLOCKS, 1f, 1f);
                }
            }
            blockEntity.spinTicks += 1;
        } else {
            blockEntity.spinTicks = 0;
        }
        if(blockEntity.Command != 0) {
            if(blockEntity.Command == 0x20) {
                if(state.get(FloppyDriveBlock.LIGHT)) {
                    world.setBlockState(pos, state.with(FloppyDriveBlock.LIGHT, false));
                }
                blockEntity.Flags &= 0x20; blockEntity.Flags |= (state.get(FloppyDriveBlock.LIGHT)?0x20:0);
            } else if(blockEntity.Command == 0x21) {
                if(!state.get(FloppyDriveBlock.LIGHT)) {
                    world.setBlockState(pos, state.with(FloppyDriveBlock.LIGHT, true));
                }
                blockEntity.Flags &= 0x20; blockEntity.Flags |= (state.get(FloppyDriveBlock.LIGHT)?0x20:0);
            }
            blockEntity.Flags &= ~1;
            blockEntity.Command = 0;
        }
    }

    @Override
    public String getIdentifier() {return "FlpyDriv";}
    @Override
    public byte readData(byte at) {
        switch(Byte.toUnsignedInt(at)) {
            case 0x0 -> { // Flags
                return (byte)Flags;
            }
            case 0x1 -> { // Track Number
                return (byte)TrackNumber;
            }
            case 0x2 -> { // Sector Number
                return (byte)SectorNumber;
            }
            default -> {
                if(Byte.toUnsignedInt(at) >= 0xF8) {
                    return (getIdentifier().getBytes(StandardCharsets.US_ASCII)[Byte.toUnsignedInt(at)-0xF8]);
                }
                return 0;
            }
        }
    }
    @Override
    public void storeData(byte at, byte data) {
        switch(Byte.toUnsignedInt(at)) {
            case 0x0 -> { // Command
                if((Flags & 1) == 0) {
                    Command = Byte.toUnsignedInt(data);
                    Flags = 1;
                }
            }
            case 0x1 -> { // Track Number
                TrackNumber = Byte.toUnsignedInt(data);
            }
            case 0x2 -> { // Track Number
                SectorNumber = Byte.toUnsignedInt(data);
            }
        }
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        stack = ItemStack.fromNbt(tag.getCompound("Item"));
    }
    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        tag.put("Item", stack.writeNbt(new NbtCompound()));
    }
}
