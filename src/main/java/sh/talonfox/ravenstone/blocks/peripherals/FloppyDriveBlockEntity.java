package sh.talonfox.ravenstone.blocks.peripherals;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import sh.talonfox.ravenstone.Ravenstone;
import sh.talonfox.ravenstone.blocks.BlockRegister;
import sh.talonfox.ravenstone.items.FloppyDisk;
import sh.talonfox.ravenstone.sounds.SoundEventRegister;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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
0xC4: Read Label
0xE4: Write Label
0xF4: Stop & Clear Data Buffer
 */

public class FloppyDriveBlockEntity extends PeripheralBlockEntity {
    private ItemStack stack = Items.AIR.getDefaultStack();
    private long spinTicks = 0;
    private int Command = 0;
    private int Flags = 0;
    private int SectorNumber = 0;
    private int TrackNumber = 0;
    private int CurrentTrack = 63;
    private int FinishDelay = -1;
    private int LightTimeout = 0;
    private int EngageDelay = -1;
    private int readsBeforeWait = 1;
    private byte[] Buffer = new byte[128];

    public FloppyDriveBlockEntity(BlockPos pos, BlockState state) {
        super(BlockRegister.RAVEN_FLOPPY_DRIVE_ENTITY, pos, state, 2);
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
        CurrentTrack = 63;
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
            CurrentTrack = 63;
            world.playSound(null, getPos(), SoundEventRegister.DISKETTE_EJECT_SOUND_EVENT, SoundCategory.BLOCKS, 0.25f, 1f);
        } else {
            ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), stack);
        }
        stack = Items.AIR.getDefaultStack();
        return true;
    }

    public void Seek(int track, BlockState state) {
        if(!state.get(FloppyDriveBlock.LIGHT)) {
            Flags |= 0x10;
            Flags &= ~1;
            return;
        }
        int distance = Math.abs(CurrentTrack-track);
        switch(distance) {
            case 4 -> {
                world.playSound(null, getPos(), SoundEventRegister.DISKETTE_SEEK_TINY3_EVENT, SoundCategory.BLOCKS, 1f, 1f);
                FinishDelay = 1;
            }
            case 3 -> {
                world.playSound(null, getPos(), SoundEventRegister.DISKETTE_SEEK_TINY2_EVENT, SoundCategory.BLOCKS, 1f, 1f);
                FinishDelay = 1;
            }
            case 2 -> {
                world.playSound(null, getPos(), SoundEventRegister.DISKETTE_SEEK_TINY1_EVENT, SoundCategory.BLOCKS, 1f, 1f);
                Flags &= ~1;
            }
            case 1 -> {
                world.playSound(null, getPos(), SoundEventRegister.DISKETTE_SEEK_TINY0_EVENT, SoundCategory.BLOCKS, 1f, 1f);
                Flags &= ~1;
            }
            default -> {
                if(distance >= 5) {
                    world.playSound(null, getPos(), SoundEventRegister.DISKETTE_SEEK_LARGE_EVENT, SoundCategory.BLOCKS, 1f, 1f);
                    FinishDelay = 9;
                } else {
                    Flags &= ~1;
                }
            }
        }
        CurrentTrack = track;
    }

    public boolean EngageHead() {
        if(this.getCachedState().get(FloppyDriveBlock.HAS_DISK)) {
            world.setBlockState(pos, this.getCachedState().with(FloppyDriveBlock.LIGHT, true));
            this.EngageDelay = 2;
            this.LightTimeout = 10;
            return true;
        } else {
            Flags &= ~9;
            Flags |= 8;
            Command = 0;
            return false;
        }
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
            blockEntity.LightTimeout = -1;
            blockEntity.EngageDelay = -1;
        }
        if(blockEntity.FinishDelay != -1) {
            blockEntity.FinishDelay -= 1;
            if(blockEntity.FinishDelay == -1) {
                blockEntity.Flags &= ~1;
            }
        }
        if(blockEntity.LightTimeout != -1) {
            blockEntity.LightTimeout -= 1;
            if(blockEntity.LightTimeout == -1) {
                world.setBlockState(pos, state.with(FloppyDriveBlock.LIGHT, false));
            }
        }
        if(blockEntity.EngageDelay != -1) {
            blockEntity.EngageDelay -= 1;
        } else if(blockEntity.Command != 0) {
            if(blockEntity.stack.isEmpty()) {
                blockEntity.Flags &= ~9;
                blockEntity.Flags |= 8;
            } else {
                if (blockEntity.Command == 0x01) { // Seek to Track 0
                    if (state.get(FloppyDriveBlock.LIGHT)) {
                        blockEntity.LightTimeout = 10;
                        blockEntity.Seek(0, state);
                        blockEntity.FinishDelay = 9;
                    } else if(!state.get(FloppyDriveBlock.LIGHT)) {
                        if(blockEntity.EngageHead()) {
                            return;
                        }
                    }
                } else if (blockEntity.Command == 0x10) { // Seek
                    if (state.get(FloppyDriveBlock.LIGHT) && blockEntity.TrackNumber < 64) {
                        blockEntity.LightTimeout = 10;
                        if (blockEntity.TrackNumber != blockEntity.CurrentTrack) {
                            blockEntity.Flags |= 0x10;
                            blockEntity.Flags &= ~1;
                        } else {
                            blockEntity.Seek(blockEntity.SectorNumber, state);
                        }
                    } else if(blockEntity.TrackNumber < 64){
                        blockEntity.LightTimeout = 10;
                        blockEntity.Flags |= 0x10;
                        blockEntity.Flags &= ~1;
                    } else if(!state.get(FloppyDriveBlock.LIGHT)) {
                        if(blockEntity.EngageHead()) {
                            blockEntity.Flags |= 0x1;
                            return;
                        } else {
                            blockEntity.Flags &= ~1;
                        }
                    }
                } else if (blockEntity.Command == 0x20) { // Retract Head
                    if (state.get(FloppyDriveBlock.LIGHT)) {
                        world.setBlockState(pos, state.with(FloppyDriveBlock.LIGHT, false));
                    }
                    blockEntity.Flags &= ~0x20;
                    blockEntity.Flags |= (state.get(FloppyDriveBlock.LIGHT) ? 0x20 : 0);
                    blockEntity.Flags &= ~1;
                } else if (blockEntity.Command == 0x21) { // Engage Head
                    if (!state.get(FloppyDriveBlock.LIGHT)) {
                        world.setBlockState(pos, state.with(FloppyDriveBlock.LIGHT, true));
                        blockEntity.FinishDelay = 2;
                    } else {
                        blockEntity.Flags &= ~1;
                    }
                    blockEntity.Flags &= ~0x20;
                    blockEntity.Flags |= (state.get(FloppyDriveBlock.LIGHT) ? 0x20 : 0);
                    blockEntity.LightTimeout = 10;
                } else if(blockEntity.Command == 0x80 && blockEntity.readsBeforeWait == 0) { // Read
                    blockEntity.readsBeforeWait = 1;
                    blockEntity.Flags &= ~1;
                    if (state.get(FloppyDriveBlock.LIGHT) && blockEntity.SectorNumber < 32) {
                        blockEntity.LightTimeout = 10;
                        if (blockEntity.TrackNumber != blockEntity.CurrentTrack) {
                            blockEntity.Flags |= 0x10;
                        } else {
                            var sec = ((FloppyDisk)blockEntity.stack.getItem()).readSector(blockEntity.stack, (ServerWorld) world, (blockEntity.TrackNumber * 32) + blockEntity.SectorNumber);
                            blockEntity.Buffer = Arrays.copyOf(sec, 128);
                        }
                    } else if(!state.get(FloppyDriveBlock.LIGHT)) {
                        if(blockEntity.EngageHead()) {
                            blockEntity.Flags |= 0x1;
                            return;
                        }
                    }
                } else if(blockEntity.Command == 0xA0 && blockEntity.readsBeforeWait == 0) { // Write
                    blockEntity.readsBeforeWait = 1;
                    blockEntity.Flags &= ~1;
                    if (state.get(FloppyDriveBlock.LIGHT) && blockEntity.SectorNumber < 32) {
                        blockEntity.LightTimeout = 10;
                        if (blockEntity.TrackNumber != blockEntity.CurrentTrack) {
                            blockEntity.Flags |= 0x10;
                        } else {
                            ((FloppyDisk)blockEntity.stack.getItem()).writeSector(blockEntity.stack, (ServerWorld) world, (blockEntity.TrackNumber * 32) + blockEntity.SectorNumber, blockEntity.Buffer);
                        }
                    } else if(!state.get(FloppyDriveBlock.LIGHT)) {
                        if(blockEntity.EngageHead()) {
                            blockEntity.Flags |= 0x1;
                            return;
                        }
                    }
                } else if(blockEntity.Command == 0xC4) { // Read Label
                    if (state.get(FloppyDriveBlock.LIGHT)) {
                        blockEntity.LightTimeout = 10;
                        String name = ((FloppyDisk) blockEntity.stack.getItem()).getLabel(blockEntity.stack);
                        blockEntity.Buffer = Arrays.copyOf(name.getBytes(StandardCharsets.US_ASCII), 128);
                    } else if(!state.get(FloppyDriveBlock.LIGHT)) {
                        if(blockEntity.EngageHead()) {
                            blockEntity.Flags |= 0x1;
                            return;
                        }
                    }
                    blockEntity.Flags &= ~1;
                } else if(blockEntity.Command == 0xE4) { // Write Label
                    if (state.get(FloppyDriveBlock.LIGHT)) {
                        blockEntity.LightTimeout = 10;
                        var length = 128;
                        for (int i = 0; i < 128; i++) {
                            if (blockEntity.Buffer[i] == 0) {
                                length = i;
                                break;
                            }
                        }
                        var label = new String(blockEntity.Buffer, 0, length, StandardCharsets.US_ASCII);
                        ((FloppyDisk) blockEntity.stack.getItem()).setLabel(blockEntity.stack, label);
                    }
                    blockEntity.Flags &= ~1;
                } else {
                    blockEntity.Flags |= 80;
                    blockEntity.Flags &= ~1;
                }
            }
            blockEntity.Command = 0;
        }
    }
    @Override
    public byte readData(short at) {
        switch(Short.toUnsignedInt(at)) {
            case 0x80 -> { // Flags
                return (byte)Flags;
            }
            case 0x81 -> { // Track Number
                return (byte)TrackNumber;
            }
            case 0x82 -> { // Sector Number
                return (byte)SectorNumber;
            }
            default -> {
                if(Short.toUnsignedInt(at) < 0x80) {
                    return Buffer[Short.toUnsignedInt(at)];
                }
                return 0;
            }
        }
    }
    @Override
    public void storeData(short at, byte data) {
        switch(Short.toUnsignedInt(at)) {
            case 0x80 -> { // Command
                if(Byte.toUnsignedInt(data) == 0xF4) {
                    Command = 0;
                    Flags = 0xfe;
                    Arrays.fill(Buffer, (byte)0);
                } else if((Flags & 1) == 0 && data != 0) {
                    Ravenstone.LOGGER.info("Command: 0x{} Track: 0x{} Sector: 0x{}", Integer.toHexString(Byte.toUnsignedInt(data)), Integer.toHexString(TrackNumber), Integer.toHexString(SectorNumber));
                    if(Byte.toUnsignedInt(data) == 0x80 && readsBeforeWait > 0) { // Read
                        readsBeforeWait--;
                        Flags = 0;
                        if (this.getCachedState().get(FloppyDriveBlock.LIGHT) && SectorNumber < 32) {
                            LightTimeout = 10;
                            if (TrackNumber != CurrentTrack) {
                                Flags |= 0x10;
                            } else {
                                var sec = ((FloppyDisk)stack.getItem()).readSector(stack, (ServerWorld) world, (TrackNumber * 32) + SectorNumber);
                                Buffer = Arrays.copyOf(sec, 128);
                            }
                        } else if(!this.getCachedState().get(FloppyDriveBlock.LIGHT)) {
                            EngageHead();
                            Command = 0x80;
                            Flags |= 0x1;
                        }
                    } else if(Byte.toUnsignedInt(data) == 0xA0 && readsBeforeWait > 0) { // Write
                        readsBeforeWait--;
                        Flags = 0;
                        if (this.getCachedState().get(FloppyDriveBlock.LIGHT) && SectorNumber < 32) {
                            LightTimeout = 10;
                            if (TrackNumber != CurrentTrack) {
                                Flags |= 0x10;
                            } else {
                                ((FloppyDisk)stack.getItem()).writeSector(stack, (ServerWorld) world, (TrackNumber * 32) + SectorNumber, Buffer);
                            }
                        } else if(!this.getCachedState().get(FloppyDriveBlock.LIGHT)) {
                            EngageHead();
                            Command = 0x80;
                            Flags |= 0x1;
                        }
                    } else {
                        Command = Byte.toUnsignedInt(data);
                        Flags = 1;
                    }
                }
            }
            case 0x81 -> { // Track Number
                TrackNumber = Byte.toUnsignedInt(data) % 64;
            }
            case 0x82 -> { // Sector Number
                SectorNumber = Byte.toUnsignedInt(data) % 64;
            }
            default -> {
                if(Short.toUnsignedInt(at) < 0x80) {
                    Buffer[Short.toUnsignedInt(at)] = data;
                }
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
