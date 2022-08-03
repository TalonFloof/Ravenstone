package sh.talonfox.ravynstone.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import sh.talonfox.ravynstone.blocks.ComputerBlockEntity;

public class ComputerPackets {
    public static final Identifier COMPUTER_C2S_SYNC_ID = new Identifier("ravynstone", "computer_c2s_sync");
    public static void ComputerC2SSyncReceiver(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        BlockPos target = buf.readBlockPos();
        NbtCompound new_data = buf.readNbt();
        server.execute(() -> {
            ComputerBlockEntity blockEntity = (ComputerBlockEntity)(player.getWorld().getBlockEntity(target));
            assert new_data != null;
            assert blockEntity != null;
            blockEntity.readNbt(new_data);
            blockEntity.markDirty();
        });
    }
}
