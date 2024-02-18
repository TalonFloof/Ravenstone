package sh.talonfox.ravenstone;

import net.fabricmc.api.ModInitializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import sh.talonfox.ravenstone.blocks.BlockRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.talonfox.ravenstone.items.ItemRegister;
import sh.talonfox.ravenstone.network.PacketRegister;
import sh.talonfox.ravenstone.sounds.SoundEventRegister;

import static sh.talonfox.ravenstone.ItemGroupRegister.ITEM_GROUP;

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
        LOGGER.info("Copyright (C) 2022-2024 TalonFloof (Sorry for the bad ASCII logo...)");
        LOGGER.info("Registering Blocks...");
        BlockRegister.Initialize();
        LOGGER.info("Registering Items...");
        ItemRegister.Initialize();
        Registry.register(Registries.ITEM_GROUP, new Identifier("ravenstone", "ravenstone"), ITEM_GROUP);
        LOGGER.info("Registering Packets...");
        PacketRegister.Initalize();
        LOGGER.info("Registering Resource Reload Handlers...");
        ResourceRegister.Initialize();
        LOGGER.info("Registering Sound Events...");
        SoundEventRegister.Initialize();
        LOGGER.info("Registering Images...");
        ResourceRegister.RegisterROM("r3000","r3000.bin");
        ResourceRegister.RegisterFloppy("Vix 1 (Boot)","vix1.bin");
        ResourceRegister.RegisterFloppy("Vix 2 (Core Utilities)","vix2.bin");
        ItemGroupRegister.Initialize();
        LOGGER.info("Initialization successful!");
    }
}
