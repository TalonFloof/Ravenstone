package sh.talonfox.ravynstone.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class PacketRegister {
    public static void Initalize() {
        ServerPlayNetworking.registerGlobalReceiver(ComputerPackets.COMPUTER_C2S_SYNC_ID, ComputerPackets::ComputerC2SSyncReceiver);
        ServerPlayNetworking.registerGlobalReceiver(ComputerPackets.COMPUTER_STEP_ID, ComputerPackets::ComputerStepReceiver);
    }
}
