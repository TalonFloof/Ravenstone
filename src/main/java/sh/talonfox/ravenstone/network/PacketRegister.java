package sh.talonfox.ravenstone.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class PacketRegister {
    public static void Initalize() {
        ServerPlayNetworking.registerGlobalReceiver(TerminalPackets.TERMINAL_KEY, TerminalPackets::TerminalKeyReceiver);
    }
}