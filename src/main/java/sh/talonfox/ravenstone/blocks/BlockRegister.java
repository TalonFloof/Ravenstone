package sh.talonfox.ravenstone.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import sh.talonfox.ravenstone.ItemGroupRegister;

public class BlockRegister {
    public static final ComputerBlock RAVEN_COMPUTER_BLOCK = new ComputerBlock(FabricBlockSettings.of(Material.METAL).strength(4.0f));
    public static final TerminalBlock RAVEN_TERMINAL_BLOCK = new TerminalBlock(FabricBlockSettings.of(Material.METAL).strength(4.0f));
    public static BlockEntityType<ComputerBlockEntity> RAVEN_COMPUTER_ENTITY;
    public static BlockEntityType<TerminalBlockEntity> RAVEN_TERMINAL_ENTITY;

    public static void Initalize() {
        Registry.register(Registry.BLOCK, new Identifier("ravenstone", "computer"), RAVEN_COMPUTER_BLOCK);
        Registry.register(Registry.BLOCK, new Identifier("ravenstone", "terminal"), RAVEN_TERMINAL_BLOCK);
        Registry.register(Registry.ITEM, new Identifier("ravenstone", "computer"), new BlockItem(RAVEN_COMPUTER_BLOCK, new Item.Settings().group(ItemGroupRegister.ITEM_GROUP)));
        Registry.register(Registry.ITEM, new Identifier("ravenstone", "terminal"), new BlockItem(RAVEN_TERMINAL_BLOCK, new Item.Settings().group(ItemGroupRegister.ITEM_GROUP)));
        RAVEN_COMPUTER_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "ravenstone:computer", FabricBlockEntityTypeBuilder.create(ComputerBlockEntity::new, RAVEN_COMPUTER_BLOCK).build(null));
        RAVEN_TERMINAL_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "ravenstone:terminal", FabricBlockEntityTypeBuilder.create(TerminalBlockEntity::new, RAVEN_TERMINAL_BLOCK).build(null));
    }
}