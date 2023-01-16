package sh.talonfox.ravenstone.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class PacketRegister {
    public static void Initalize() {
        ServerPlayNetworking.registerGlobalReceiver(TerminalPackets.TERMINAL_KEY, TerminalPackets::TerminalKeyReceiver);
        ServerPlayNetworking.registerGlobalReceiver(PeripheralPackets.SET_BUS_ID, PeripheralPackets::SetBusReceiver);
        ServerPlayNetworking.registerGlobalReceiver(HardDrivePackets.SET_HD_FLAG, HardDrivePackets::SetHDFlagReceiver);
    }
}
