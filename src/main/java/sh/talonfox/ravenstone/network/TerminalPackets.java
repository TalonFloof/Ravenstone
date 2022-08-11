package sh.talonfox.ravenstone.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import sh.talonfox.ravenstone.blocks.TerminalBlockEntity;

public class TerminalPackets {
    public static final Identifier TERMINAL_KEY = new Identifier("ravenstone", "terminal_key");
    public static void TerminalKeyReceiver(MinecraftServer minecraftServer, ServerPlayerEntity serverPlayerEntity, ServerPlayNetworkHandler ignored, PacketByteBuf packetByteBuf, PacketSender ignored2) {
        BlockPos pos = packetByteBuf.readBlockPos();
        byte key = packetByteBuf.readByte();
        minecraftServer.execute(() -> {
            TerminalBlockEntity terminal = (TerminalBlockEntity)serverPlayerEntity.getWorld().getBlockEntity(pos);
            assert terminal != null;
            for(int i=0;i < 16; i++) {
                if(terminal.KeyboardBuffer[(15-i)] == 0) {
                    terminal.KeyboardBuffer[(15-i)] = key;
                    return;
                }
            }
            for(int i=1;i < 16; i++)
                terminal.KeyboardBuffer[i-1] = terminal.KeyboardBuffer[i];
            terminal.KeyboardBuffer[15] = key;
        });
    }
}
