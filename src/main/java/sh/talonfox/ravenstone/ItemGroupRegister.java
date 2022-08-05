package sh.talonfox.ravenstone;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import sh.talonfox.ravenstone.blocks.BlockRegister;

public class ItemGroupRegister {
    public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.create(
            new Identifier("ravenstone", "item_group"))
            .icon(() -> new ItemStack(BlockRegister.RAVEN_COMPUTER_BLOCK)).appendItems(stacks -> {
                stacks.add(new ItemStack(BlockRegister.RAVEN_COMPUTER_BLOCK));
                stacks.add(new ItemStack(BlockRegister.RAVEN_TERMINAL_BLOCK));
            }).build();
}
