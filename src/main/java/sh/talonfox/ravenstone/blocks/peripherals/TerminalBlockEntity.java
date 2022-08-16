package sh.talonfox.ravenstone.blocks.peripherals;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sh.talonfox.ravenstone.blocks.BlockRegister;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class TerminalBlockEntity extends PeripheralBlockEntity {
    public byte[] ScreenBuffer = new byte[80*50];
    public byte[] KeyboardBuffer = new byte[16];
    public int CursorX = 0;
    public int CursorY = 0;
    public int Row = 0;
    public int Command = 0;
    public int BlitXStart = 0;
    public int BlitYStart = 0;
    public int BlitXOffset = 0;
    public int BlitYOffset = 0;
    public int BlitWidth = 0;
    public int BlitHeight = 0;
    public TerminalBlockEntity(BlockPos pos, BlockState state) {
        super(BlockRegister.RAVEN_TERMINAL_ENTITY, pos, state, 1);
        Arrays.fill(ScreenBuffer,(byte)0x20);
    }

    @Override
    public String getIdentifier() {return "MonoTTY\0";}
    @Override
    public byte readData(byte at) {
        switch(at) {
            case 0x00 -> { // TTY Row
                return (byte)Row;
            }
            case 0x01 -> { // TTY Cursor X
                return (byte)CursorX;
            }
            case 0x02 -> { // TTY Cursor Y
                return (byte)CursorY;
            }
            case 0x04 -> { // Keyboard
                for(int i=0;i<16;i++) {
                    if(KeyboardBuffer[(15-i)] != 0) {
                        byte ret = KeyboardBuffer[(15-i)];
                        KeyboardBuffer[(15-i)] = 0;
                        return ret;
                    }
                }
                return 0;
            }
            case 0x07 -> { // TTY Command
                return (byte)Command;
            }
            case 0x08 -> { // Blit X Start
                return (byte)BlitXStart;
            }
            case 0x09 -> { // Blit Y Start
                return (byte)BlitYStart;
            }
            case 0x0a -> { // Blit X Offset
                return (byte)BlitXOffset;
            }
            case 0x0b -> { // Blit Y Offset
                return (byte)BlitYOffset;
            }
            case 0x0c -> { // Blit Width
                return (byte)BlitWidth;
            }
            case 0x0d -> { // Blit Height
                return (byte)BlitHeight;
            }
            default -> {
                if(at >= 0x10 && at <= 0x5F) {
                    return ScreenBuffer[((Row*80)+(at-0x10))];
                } else if(Byte.toUnsignedInt(at) >= 0xF8) {
                    return (getIdentifier().getBytes(StandardCharsets.US_ASCII)[Byte.toUnsignedInt(at)-0xF8]);
                }
                return 0;
            }
        }
    }
    @Override
    public void storeData(byte at, byte data) {
        switch(at) {
            case 0x00 -> { // TTY Row
                Row = Byte.toUnsignedInt(data) % 50;
            }
            case 0x01 -> { // TTY Cursor X
                CursorX = Byte.toUnsignedInt(data) % 80;
            }
            case 0x02 -> { // TTY Cursor Y
                CursorY = Byte.toUnsignedInt(data) % 50;
            }
            case 0x07 -> { // TTY Command
                Command = data;
            }
            case 0x08 -> { // Blit X Start
                BlitXStart = data;
            }
            case 0x09 -> { // Blit Y Start
                BlitYStart = data;
            }
            case 0x0a -> { // Blit X Offset
                BlitXOffset = data;
            }
            case 0x0b -> { // Blit Y Offset
                BlitYOffset = data;
            }
            case 0x0c -> { // Blit Width
                BlitWidth = data;
            }
            case 0x0d -> { // Blit Height
                BlitHeight = data;
            }
            default -> {
                if(at >= 0x10 && at <= 0x5F) {
                    ScreenBuffer[((Row*80)+(at-0x10))] = data;
                }
            }
        }
        if((at >= 0 && at <= 3) || (at >= 0x10 && at <= 0x5F)) {
            // Sync Data to Client(s)
            assert this.world != null;
            this.world.updateListeners(getPos(), this.getCachedState(), this.getCachedState(), 3);
            this.markDirty();
        }
    }

    public static void tick(World world, BlockPos pos, BlockState state, TerminalBlockEntity blockEntity) {
        if(blockEntity.Command != 0) {
            switch(blockEntity.Command) {
                case 0x01 -> { // Blit
                    for(int y=blockEntity.BlitYOffset; y < blockEntity.BlitYOffset+blockEntity.BlitHeight; y++) {
                        for(int x=blockEntity.BlitXOffset; x < blockEntity.BlitXOffset+blockEntity.BlitWidth; x++) {
                            blockEntity.ScreenBuffer[y*80+x] = (byte)blockEntity.BlitXStart;
                        }
                    }
                }
                case 0x02 -> { // Invert
                    for(int y=blockEntity.BlitYOffset; y < blockEntity.BlitYOffset+blockEntity.BlitHeight; y++) {
                        for(int x=blockEntity.BlitXOffset; x < blockEntity.BlitXOffset+blockEntity.BlitWidth; x++) {
                            blockEntity.ScreenBuffer[y*80+x] = (byte)(Byte.toUnsignedInt(blockEntity.ScreenBuffer[y*80+x]) ^ 0x80);
                        }
                    }
                }
                case 0x03 -> { // Shift (Copy)
                    for(int y=0; y < blockEntity.BlitHeight; y++) {
                        for(int x=0; x < blockEntity.BlitWidth; x++) {
                            blockEntity.ScreenBuffer[(y+blockEntity.BlitYOffset)*80+(x+blockEntity.BlitXOffset)] = blockEntity.ScreenBuffer[(y+blockEntity.BlitYStart)*80+(x+blockEntity.BlitXStart)];
                        }
                    }
                }
            }
            world.updateListeners(pos, state, state, 3);
            blockEntity.Command = 0;
        }
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        tag.putByteArray("ScreenBuffer", ScreenBuffer);
        tag.putByteArray("KeyboardBuffer", KeyboardBuffer);
        tag.putInt("CursorX", CursorX);
        tag.putInt("CursorY", CursorY);
    }
    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        ScreenBuffer = tag.getByteArray("ScreenBuffer");
        KeyboardBuffer = tag.getByteArray("KeyboardBuffer");
        CursorX = tag.getInt("CursorX");
        CursorY = tag.getInt("CursorY");
    }
}
