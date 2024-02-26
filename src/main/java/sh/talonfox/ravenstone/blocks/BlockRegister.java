package sh.talonfox.ravenstone.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import sh.talonfox.ravenstone.blocks.peripherals.*;
import sh.talonfox.ravenstone.blocks.upgrades.*;

public class BlockRegister {
    public static final ComputerBlock RAVEN_COMPUTER_BLOCK = new ComputerBlock(FabricBlockSettings.create().strength(2.0f).sounds(BlockSoundGroup.NETHERITE));
    public static final TerminalBlock RAVEN_TERMINAL_BLOCK = new TerminalBlock(FabricBlockSettings.create().strength(2.0f).sounds(BlockSoundGroup.NETHERITE));
    public static final ModemBlock RAVEN_MODEM_BLOCK = new ModemBlock(FabricBlockSettings.create().strength(2.0f).sounds(BlockSoundGroup.NETHERITE));
    public static final HarddriveBlock RAVEN_HARDDRIVE_BLOCK = new HarddriveBlock(FabricBlockSettings.create().strength(2.0f).sounds(BlockSoundGroup.NETHERITE));
    public static final FloppyDriveBlock RAVEN_FLOPPY_DRIVE_BLOCK = new FloppyDriveBlock(FabricBlockSettings.create().strength(2.0f).sounds(BlockSoundGroup.NETHERITE));
    public static final RibbonCableBlock RAVEN_RIBBON_CABLE_BLOCK = new RibbonCableBlock(FabricBlockSettings.create().strength(0.25f).sounds(BlockSoundGroup.WOOL));
    /*public static final RAM16KBlock RAVEN_16K_UPGRADE_BLOCK = new RAM16KBlock(FabricBlockSettings.create().strength(2.0f).sounds(BlockSoundGroup.NETHERITE));
    public static final RAM32KBlock RAVEN_32K_UPGRADE_BLOCK = new RAM32KBlock(FabricBlockSettings.create().strength(2.0f).sounds(BlockSoundGroup.NETHERITE));
    public static final RAM64KBlock RAVEN_64K_UPGRADE_BLOCK = new RAM64KBlock(FabricBlockSettings.create().strength(2.0f).sounds(BlockSoundGroup.NETHERITE));*/
    public static BlockEntityType<ComputerBlockEntity> RAVEN_COMPUTER_ENTITY;
    public static BlockEntityType<TerminalBlockEntity> RAVEN_TERMINAL_ENTITY;
    public static BlockEntityType<ModemBlockEntity> RAVEN_MODEM_ENTITY;
    public static BlockEntityType<HarddriveBlockEntity> RAVEN_HARDDRIVE_ENTITY;
    public static BlockEntityType<FloppyDriveBlockEntity> RAVEN_FLOPPY_DRIVE_ENTITY;
    public static BlockEntityType<RAM16KBlockEntity> RAVEN_16K_UPGRADE_ENTITY;
    public static BlockEntityType<RAM32KBlockEntity> RAVEN_32K_UPGRADE_ENTITY;
    public static BlockEntityType<RAM64KBlockEntity> RAVEN_64K_UPGRADE_ENTITY;

    public static void Initialize() {
        Registry.register(Registries.BLOCK, new Identifier("ravenstone", "computer"), RAVEN_COMPUTER_BLOCK);
        Registry.register(Registries.BLOCK, new Identifier("ravenstone", "terminal"), RAVEN_TERMINAL_BLOCK);
        Registry.register(Registries.BLOCK, new Identifier("ravenstone", "modem"), RAVEN_MODEM_BLOCK);
        Registry.register(Registries.BLOCK, new Identifier("ravenstone", "hard_drive"), RAVEN_HARDDRIVE_BLOCK);
        Registry.register(Registries.BLOCK, new Identifier("ravenstone", "floppy_drive"), RAVEN_FLOPPY_DRIVE_BLOCK);
        Registry.register(Registries.BLOCK, new Identifier("ravenstone", "ribbon_cable"), RAVEN_RIBBON_CABLE_BLOCK);
        /*Registry.register(Registries.BLOCK, new Identifier("ravenstone", "ram_upgrade_32k"), RAVEN_32K_UPGRADE_BLOCK);
        Registry.register(Registries.BLOCK, new Identifier("ravenstone", "ram_upgrade_64k"), RAVEN_64K_UPGRADE_BLOCK);
        Registry.register(Registries.BLOCK, new Identifier("ravenstone", "ram_upgrade_1m"), RAVEN_1M_UPGRADE_BLOCK);*/
        Registry.register(Registries.ITEM, new Identifier("ravenstone", "computer"), new BlockItem(RAVEN_COMPUTER_BLOCK, new Item.Settings()));
        Registry.register(Registries.ITEM, new Identifier("ravenstone", "terminal"), new BlockItem(RAVEN_TERMINAL_BLOCK, new Item.Settings()));
        Registry.register(Registries.ITEM, new Identifier("ravenstone", "modem"), new BlockItem(RAVEN_MODEM_BLOCK, new Item.Settings()));
        Registry.register(Registries.ITEM, new Identifier("ravenstone", "hard_drive"), new BlockItem(RAVEN_HARDDRIVE_BLOCK, new Item.Settings()));
        Registry.register(Registries.ITEM, new Identifier("ravenstone", "floppy_drive"), new BlockItem(RAVEN_FLOPPY_DRIVE_BLOCK, new Item.Settings()));
        Registry.register(Registries.ITEM, new Identifier("ravenstone", "ribbon_cable"), new BlockItem(RAVEN_RIBBON_CABLE_BLOCK, new Item.Settings()));
        /*Registry.register(Registries.ITEM, new Identifier("ravenstone", "ram_upgrade_16k"), new BlockItem(RAVEN_16K_UPGRADE_BLOCK, new Item.Settings()));
        Registry.register(Registries.ITEM, new Identifier("ravenstone", "ram_upgrade_32k"), new BlockItem(RAVEN_32K_UPGRADE_BLOCK, new Item.Settings()));
        Registry.register(Registries.ITEM, new Identifier("ravenstone", "ram_upgrade_64k"), new BlockItem(RAVEN_64K_UPGRADE_BLOCK, new Item.Settings()));*/
        RAVEN_COMPUTER_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, "ravenstone:computer", FabricBlockEntityTypeBuilder.create(ComputerBlockEntity::new, RAVEN_COMPUTER_BLOCK).build(null));
        RAVEN_TERMINAL_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, "ravenstone:terminal", FabricBlockEntityTypeBuilder.create(TerminalBlockEntity::new, RAVEN_TERMINAL_BLOCK).build(null));
        RAVEN_MODEM_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, "ravenstone:modem", FabricBlockEntityTypeBuilder.create(ModemBlockEntity::new, RAVEN_MODEM_BLOCK).build(null));
        RAVEN_HARDDRIVE_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, "ravenstone:hard_drive", FabricBlockEntityTypeBuilder.create(HarddriveBlockEntity::new, RAVEN_HARDDRIVE_BLOCK).build(null));
        RAVEN_FLOPPY_DRIVE_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, "ravenstone:floppy_drive", FabricBlockEntityTypeBuilder.create(FloppyDriveBlockEntity::new, RAVEN_FLOPPY_DRIVE_BLOCK).build(null));
        /*RAVEN_16K_UPGRADE_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, "ravenstone:ram_upgrade_16k", FabricBlockEntityTypeBuilder.create(RAM16KBlockEntity::new, RAVEN_16K_UPGRADE_BLOCK).build(null));
        RAVEN_32K_UPGRADE_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, "ravenstone:ram_upgrade_32k", FabricBlockEntityTypeBuilder.create(RAM32KBlockEntity::new, RAVEN_32K_UPGRADE_BLOCK).build(null));
        RAVEN_64K_UPGRADE_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, "ravenstone:ram_upgrade_64k", FabricBlockEntityTypeBuilder.create(RAM64KBlockEntity::new, RAVEN_64K_UPGRADE_BLOCK).build(null));*/
    }
}