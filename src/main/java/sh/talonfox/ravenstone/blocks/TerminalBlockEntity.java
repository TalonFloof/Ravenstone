package sh.talonfox.ravenstone.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class TerminalBlockEntity extends PeripheralBlockEntity {
    public byte[] ScreenBuffer = new byte[80*50];
    public byte[] KeyboardBuffer = new byte[16];
    public int CursorX = 0;
    public int CursorY = 0;
    public TerminalBlockEntity(BlockPos pos, BlockState state) {
        super(BlockRegister.RAVEN_TERMINAL_ENTITY, pos, state);
        Arrays.fill(ScreenBuffer,(byte)0x20);
    }

    public String getIdentifier() {return "MonoTTY\0";}

    @Override
    public void writeNbt(NbtCompound tag) {
        tag.putByteArray("ScreenBuffer", ScreenBuffer);
        tag.putByteArray("KeyboardBuffer", KeyboardBuffer);
        tag.putInt("CursorX", CursorX);
        tag.putInt("CursorY", CursorY);
    }
    @Override
    public void readNbt(NbtCompound tag) {
        ScreenBuffer = tag.getByteArray("ScreenBuffer");
        KeyboardBuffer = tag.getByteArray("KeyboardBuffer");
        CursorX = tag.getInt("CursorX");
        CursorY = tag.getInt("CursorY");
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
