package sh.talonfox.ravenstone.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import sh.talonfox.ravenstone.blocks.peripherals.HarddriveBlockEntity;

public class HardDrivePackets {
    public static final Identifier SET_HD_FLAG = new Identifier("ravenstone", "set_hd_flag");
    public static void SetHDFlagReceiver(MinecraftServer minecraftServer, ServerPlayerEntity serverPlayerEntity, ServerPlayNetworkHandler ignored, PacketByteBuf packetByteBuf, PacketSender ignored2) {
        BlockPos pos = packetByteBuf.readBlockPos();
        int flags = packetByteBuf.readInt();
        minecraftServer.execute(() -> {
            HarddriveBlockEntity blockEntity = (HarddriveBlockEntity) serverPlayerEntity.getWorld().getBlockEntity(pos);
            BlockState blockState = serverPlayerEntity.getWorld().getBlockState(pos);
            assert blockEntity != null;
            blockEntity.Flags = flags;
            serverPlayerEntity.getWorld().updateListeners(pos,blockState,blockState,3);
            blockEntity.markDirty();
        });
    }
}
