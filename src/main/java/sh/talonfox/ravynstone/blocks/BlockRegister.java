package sh.talonfox.ravynstone.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import sh.talonfox.ravynstone.ItemGroupRegister;

public class BlockRegister {
    public static final ComputerBlock RAVYN_COMPUTER_BLOCK = new ComputerBlock(FabricBlockSettings.of(Material.METAL).strength(4.0f));
    public static final TerminalBlock RAVYN_TERMINAL_BLOCK = new TerminalBlock(FabricBlockSettings.of(Material.METAL).strength(4.0f));
    public static BlockEntityType<ComputerBlockEntity> RAVYN_COMPUTER_ENTITY;
    public static BlockEntityType<TerminalBlockEntity> RAVYN_TERMINAL_ENTITY;

    public static void Initalize() {
        Registry.register(Registry.BLOCK, new Identifier("ravynstone", "computer"), RAVYN_COMPUTER_BLOCK);
        Registry.register(Registry.BLOCK, new Identifier("ravynstone", "terminal"), RAVYN_TERMINAL_BLOCK);
        Registry.register(Registry.ITEM, new Identifier("ravynstone", "computer"), new BlockItem(RAVYN_COMPUTER_BLOCK, new Item.Settings().group(ItemGroupRegister.ITEM_GROUP)));
        Registry.register(Registry.ITEM, new Identifier("ravynstone", "terminal"), new BlockItem(RAVYN_TERMINAL_BLOCK, new Item.Settings().group(ItemGroupRegister.ITEM_GROUP)));
        RAVYN_COMPUTER_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "ravynstone:computer", FabricBlockEntityTypeBuilder.create(ComputerBlockEntity::new, RAVYN_COMPUTER_BLOCK).build(null));
        RAVYN_TERMINAL_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "ravynstone:terminal", FabricBlockEntityTypeBuilder.create(TerminalBlockEntity::new, RAVYN_TERMINAL_BLOCK).build(null));
    }
}