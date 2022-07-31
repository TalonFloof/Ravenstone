package sh.talonfox.ravynstone;

import net.fabricmc.api.ModInitializer;
import sh.talonfox.ravynstone.blocks.BlockRegister;

public class Ravynstone implements ModInitializer {

    @Override
    public void onInitialize() {
        BlockRegister.Initalize();
    }
}
