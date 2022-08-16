package sh.talonfox.ravenstone.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import sh.talonfox.ravenstone.blocks.peripherals.PeripheralBlockEntity;

public class PeripheralPackets {
    public static final Identifier SET_BUS_ID = new Identifier("ravenstone", "set_bus_id");
    public static void SetBusReceiver(MinecraftServer minecraftServer, ServerPlayerEntity serverPlayerEntity, ServerPlayNetworkHandler ignored, PacketByteBuf packetByteBuf, PacketSender ignored2) {
        BlockPos pos = packetByteBuf.readBlockPos();
        int busID = packetByteBuf.readInt();
        minecraftServer.execute(() -> {
            PeripheralBlockEntity blockEntity = (PeripheralBlockEntity) serverPlayerEntity.getWorld().getBlockEntity(pos);
            BlockState blockState = serverPlayerEntity.getWorld().getBlockState(pos);
            assert blockEntity != null;
            blockEntity.storeBusID(busID);
            serverPlayerEntity.getWorld().updateListeners(pos,blockState,blockState,3);
            blockEntity.markDirty();
        });
    }
}
