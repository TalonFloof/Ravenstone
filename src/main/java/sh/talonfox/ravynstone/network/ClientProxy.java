package sh.talonfox.ravynstone.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import sh.talonfox.ravynstone.client.ComputerUI;

public class ClientProxy {
    static final MinecraftClient MC = MinecraftClient.getInstance();
    public static void OpenComputerUI() {
        MC.setScreen(new ComputerUI(Text.translatable("ui.ravynstone.computer")));
    }
}
