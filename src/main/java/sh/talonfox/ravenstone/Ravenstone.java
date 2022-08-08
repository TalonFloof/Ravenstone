package sh.talonfox.ravenstone;

import net.fabricmc.api.ModInitializer;
import sh.talonfox.ravenstone.blocks.BlockRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.talonfox.ravenstone.items.ItemRegister;
import sh.talonfox.ravenstone.network.PacketRegister;

public class Ravenstone implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("ravenstone");

    @Override
    public void onInitialize() {
        LOGGER.info("__________                                    __                        ");
        LOGGER.info("\\______   \\____  ___  __ ____   ____   ______/  |_  ____   ____   ____  ");
        LOGGER.info(" |       _/__  \\ \\  \\/ // __ \\ /    \\ /  ___/   __\\/ __ \\ /    \\_/ __ \\ ");
        LOGGER.info(" |    |   \\/ __ \\_\\   /\\  ___/_   |  \\\\___ \\ |  | (  \\_\\ )   |  \\  ___/_");
        LOGGER.info(" |____|_  /____  / \\_/  \\___  /___|  /____  \\|__|  \\____/|___|  /\\___  /");
        LOGGER.info("        \\/     \\/           \\/     \\/     \\/                  \\/     \\/ ");
        LOGGER.info("Copyright (C) 2022 TalonFox (Sorry for the bad ASCII logo...)");
        LOGGER.info("Registering Blocks...");
        BlockRegister.Initalize();
        LOGGER.info("Registering Items...");
        ItemRegister.Initalize();
        LOGGER.info("Registering Packets...");
        PacketRegister.Initalize();
        LOGGER.info("Initialization successful!");
    }
}