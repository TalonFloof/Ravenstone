package sh.talonfox.ravynstone;

import net.fabricmc.api.ModInitializer;
import sh.talonfox.ravynstone.blocks.BlockRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ravynstone implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("ravynstone");

    @Override
    public void onInitialize() {
        LOGGER.info("__________                                    __                        ");
        LOGGER.info("\\______   \\____  ___  _____ __  ____   ______/  |_  ____   ____   ____  ");
        LOGGER.info(" |       _/__  \\ \\  \\/ /   |  |/    \\ /  ___/   __\\/ __ \\ /    \\_/ __ \\ ");
        LOGGER.info(" |    |   \\/ __ \\_\\   / \\___  |   |  \\\\___ \\ |  | (  \\_\\ )   |  \\  ___/_");
        LOGGER.info(" |____|_  /____  / \\_/  / ____|___|  /____  \\|__|  \\____/|___|  /\\___  /");
        LOGGER.info("        \\/     \\/       \\/         \\/     \\/                  \\/     \\/ ");
        LOGGER.info("Copyright (C) 2022 TalonFox (Talon396)");
        LOGGER.info("Registering Blocks...");
        BlockRegister.Initalize();
        LOGGER.info("Initialization successful!");
    }
}
