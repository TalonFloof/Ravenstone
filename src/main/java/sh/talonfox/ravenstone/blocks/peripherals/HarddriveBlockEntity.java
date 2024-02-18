package sh.talonfox.ravenstone.blocks.peripherals;

import com.google.common.io.Files;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sh.talonfox.ravenstone.blocks.BlockRegister;
import sh.talonfox.ravenstone.sounds.SoundEventRegister;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static sh.talonfox.ravenstone.blocks.peripherals.HarddriveBlock.LIGHT;

public class HarddriveBlockEntity extends PeripheralBlockEntity {
    public int Flags = 0;
    private long spinTicks = 0;
    /*
    Commands:
    0x01: Read
    0x02: Write
    0x03: Get Capacity
    0xFF: Stop & Clear Data Buffer
    */
    private int command = 0;
    private int sector = 0;
    private byte[] buffer = new byte[128];
    public boolean isReady = false;
    public UUID id;
    private byte[] data = new byte[10*1024*1024];
    public boolean shouldFlush = false;
    public boolean shouldRead = true;
    public int soundTick = 0;
    private int readsBeforeWait = 1;
    private boolean readBeforeTick = false;

    public HarddriveBlockEntity(BlockPos pos, BlockState state) {
        super(BlockRegister.RAVEN_HARDDRIVE_ENTITY, pos, state, 4);
        id = UUID.randomUUID();
        Flags = 0;
    }

    public void read(File file) {
        try {
            var stream = new ByteArrayInputStream(Files.toByteArray(file));
            var gstream = new GZIPInputStream(stream);
            gstream.read(data,0,(int)file.length());
            gstream.close();
        } catch(Exception e) {
            return;
        }
    }

    private void write(File file) {
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
            var stream = new ByteArrayOutputStream();
            var gstream = new GZIPOutputStream(stream);
            gstream.write(data);
            gstream.close();
            Files.write(stream.toByteArray(),file);
        } catch(Exception e) {
            return;
        }
    }

    public static void tick(World world, BlockPos pos, BlockState state, HarddriveBlockEntity blockEntity) {
        if(world.isClient())
            return;
        if((blockEntity.Flags & 0x8) != 0) {
            if(blockEntity.spinTicks == 0) {
                world.playSound(null, pos, SoundEventRegister.HARD_DRIVE_STARTUP_EVENT, SoundCategory.BLOCKS, 0.4f, 1f);
            } else if(blockEntity.spinTicks >= 600) {
                if(blockEntity.command != 0) {
                    if(blockEntity.command == 1) {
                        blockEntity.readsBeforeWait = 1;
                        world.setBlockState(pos,state.with(LIGHT,true));
                        if(blockEntity.soundTick == 0) {
                            world.playSound(null, pos, SoundEventRegister.HARD_DRIVE_SEEK_EVENT, SoundCategory.BLOCKS, 0.4f, 1f);
                            blockEntity.soundTick = 2;
                        }
                        if(blockEntity.shouldRead) {
                            blockEntity.read(new File(Objects.requireNonNull(world.getServer()).getSavePath(WorldSavePath.ROOT).toAbsolutePath() + "/disks/" + blockEntity.id.toString() + ".bin"));
                            blockEntity.shouldRead = false;
                        }
                        Arrays.copyOfRange(blockEntity.data, (blockEntity.sector * 128), (blockEntity.sector * 128) + 128);
                        blockEntity.command = 0;
                    } else if(blockEntity.command == 2) {
                        blockEntity.readsBeforeWait = 1;
                        world.setBlockState(pos,state.with(LIGHT,true));
                        if(blockEntity.soundTick == 0) {
                            world.playSound(null, pos, SoundEventRegister.HARD_DRIVE_SEEK_EVENT, SoundCategory.BLOCKS, 0.4f, 1f);
                            blockEntity.soundTick = 2;
                        }
                        if(!blockEntity.shouldFlush) {
                            blockEntity.shouldFlush = true;
                        }
                        Arrays.copyOfRange(blockEntity.data, (blockEntity.sector * 128), (blockEntity.sector * 128) + 128);
                        blockEntity.command = 0;
                    }
                } else if(state.get(LIGHT)) {
                    if(!blockEntity.readBeforeTick)
                        world.setBlockState(pos,state.with(LIGHT,false));
                    else
                        blockEntity.readBeforeTick = false;
                }
                if(blockEntity.soundTick > 0) {
                    blockEntity.soundTick--;
                }
                if((blockEntity.spinTicks % 20) == 0) {
                    world.playSound(null, pos, SoundEventRegister.HARD_DRIVE_IDLE_EVENT, SoundCategory.BLOCKS, 0.4f, 1f);
                }
            }
            if(blockEntity.isReady && blockEntity.spinTicks < 600) {
                blockEntity.spinTicks = 600;
            }
            if(blockEntity.spinTicks == 600) {
                blockEntity.isReady = true;
                world.updateListeners(pos,state,state,3);
                blockEntity.markDirty();
            }
            blockEntity.spinTicks += 1;
        } else {
            if(state.get(LIGHT)) {
                world.setBlockState(pos,state.with(LIGHT,false));
            }
            if((blockEntity.spinTicks % 20) != 0 && blockEntity.spinTicks >= 600) {
                blockEntity.spinTicks += 1;
            } else if(blockEntity.spinTicks >= 600) {
                world.playSound(null, pos, SoundEventRegister.HARD_DRIVE_SPINDOWN_EVENT, SoundCategory.BLOCKS, 0.4f, 1f);
                blockEntity.spinTicks = 0;
            } else if(blockEntity.spinTicks > 0) {
                blockEntity.spinTicks += 1;
            }
            blockEntity.isReady = false;
            world.updateListeners(pos,state,state,3);
            blockEntity.markDirty();
        }
    }

    @Override
    public void storeData(short at, byte data) {
        switch (Short.toUnsignedInt(at)) {
            case 0x80 -> { // Command
                if (readsBeforeWait > 0 && spinTicks >= 600) {
                    if (data == 1) {
                        readsBeforeWait--;
                        readBeforeTick = true;
                        world.setBlockState(pos, getCachedState().with(LIGHT, true));
                        if (soundTick == 0) {
                            world.playSound(null, pos, SoundEventRegister.HARD_DRIVE_SEEK_EVENT, SoundCategory.BLOCKS, 0.4f, 1f);
                            soundTick = 2;
                        }
                        if (shouldRead) {
                            read(new File(Objects.requireNonNull(world.getServer()).getSavePath(WorldSavePath.ROOT).toAbsolutePath() + "/disks/" + id.toString() + ".bin"));
                            shouldRead = false;
                        }
                        Arrays.copyOfRange(this.data, (sector * 128), (sector * 128) + 128);
                    } else if (data == 2) {
                        readsBeforeWait--;
                        readBeforeTick = true;
                        world.setBlockState(pos, getCachedState().with(LIGHT, true));
                        if (soundTick == 0) {
                            world.playSound(null, pos, SoundEventRegister.HARD_DRIVE_SEEK_EVENT, SoundCategory.BLOCKS, 0.4f, 1f);
                            soundTick = 2;
                        }
                        if (!shouldFlush) {
                            shouldFlush = true;
                        }
                        Arrays.copyOfRange(this.data, (sector * 128), (sector * 128) + 128);
                    }
                } else {
                    command = data;
                }
            }
            case 0x84 -> {
                sector &= 0xffffff00;
                sector |= Byte.toUnsignedInt(data);
            }
            case 0x85 -> {
                sector &= 0xffff00ff;
                sector |= Byte.toUnsignedInt(data) << 8;
            }
            case 0x86 -> {
                sector &= 0xff00ffff;
                sector |= Byte.toUnsignedInt(data) << 16;
            }
            default -> {
                if(Short.toUnsignedInt(at) < 0x80) {
                    buffer[Short.toUnsignedInt(at)] = data;
                }
            }
        }
    }

    @Override
    public byte readData(short at) {
        switch (Short.toUnsignedInt(at)) {
            case 0x80 -> { // Command
                return (byte)command;
            }
            case 0x84 -> {
                return (byte)(sector & 0xFF);
            }
            case 0x85 -> {
                return (byte)((sector & 0xFF00) >> 8);
            }
            case 0x86 -> {
                return (byte)((sector & 0xFF0000) >> 16);
            }
            default -> {
                if(Short.toUnsignedInt(at) < 0x80) {
                    return buffer[Short.toUnsignedInt(at)];
                } else {
                    return 0;
                }
            }
        }
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        Flags = tag.getInt("Flags");
        isReady = tag.getBoolean("IsReady");
        id = tag.getUuid("UUID");
    }
    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        tag.putInt("Flags", Flags);
        tag.putBoolean("IsReady", isReady);
        tag.putUuid("UUID", id);
        if(!world.isClient() && shouldFlush) {
            write(new File(Objects.requireNonNull(world.getServer()).getSavePath(WorldSavePath.ROOT).toAbsolutePath() + "/disks/" + id.toString() + ".bin"));
            shouldFlush = false;
        }
    }
}
