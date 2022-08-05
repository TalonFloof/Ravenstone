package sh.talonfox.ravynstone;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import sh.talonfox.ravynstone.blocks.BlockRegister;

public class ItemGroupRegister {
    public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.create(
            new Identifier("ravynstone", "item_group"))
            .icon(() -> new ItemStack(BlockRegister.RAVYN_COMPUTER_BLOCK)).appendItems(stacks -> {
                stacks.add(new ItemStack(BlockRegister.RAVYN_COMPUTER_BLOCK));
                stacks.add(new ItemStack(BlockRegister.RAVYN_TERMINAL_BLOCK));
            }).build();
}
