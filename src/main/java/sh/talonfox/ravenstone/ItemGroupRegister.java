package sh.talonfox.ravenstone;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import sh.talonfox.ravenstone.blocks.BlockRegister;
import sh.talonfox.ravenstone.items.ItemRegister;
//import vazkii.patchouli.common.item.PatchouliItems;

public class ItemGroupRegister {
    //private static final ItemStack RAVENSTONE_GUIDE;
    static {
        /*var stack = new ItemStack(PatchouliItems.BOOK);
        stack.getOrCreateNbt().putString("patchouli:book","ravenstone:ravenstone_guide");
        RAVENSTONE_GUIDE = stack;*/
    }

    public static void Initialize() {
        // This is a stub.
    }

    public static final ItemGroup ITEM_GROUP = FabricItemGroup.builder(
            new Identifier("ravenstone", "ravenstone_group"))
            .icon(() -> new ItemStack(ItemRegister.RAVENSTONE_CUSTOM_ICON))
            .entries((enabledFeatures, stacks, operatorEnabled) -> {
                stacks.add(new ItemStack(BlockRegister.RAVEN_COMPUTER_BLOCK));
                stacks.add(new ItemStack(BlockRegister.RAVEN_TERMINAL_BLOCK));
                stacks.add(new ItemStack(BlockRegister.RAVEN_HARDDRIVE_BLOCK));
                stacks.add(new ItemStack(BlockRegister.RAVEN_FLOPPY_DRIVE_BLOCK));
                stacks.add(new ItemStack(BlockRegister.RAVEN_RIBBON_CABLE_BLOCK));
                /*stacks.add(new ItemStack(BlockRegister.RAVEN_16K_UPGRADE_BLOCK));
                stacks.add(new ItemStack(BlockRegister.RAVEN_32K_UPGRADE_BLOCK));
                stacks.add(new ItemStack(BlockRegister.RAVEN_64K_UPGRADE_BLOCK));*/
                stacks.add(new ItemStack(ItemRegister.RAVENSTONE_TRANSISTOR));
                stacks.add(new ItemStack(ItemRegister.RAVENSTONE_R3000_PROCESSOR));
                stacks.add(new ItemStack(ItemRegister.RAVENSTONE_FLOPPY_DISK_USER));
                stacks.add(new ItemStack(ItemRegister.RAVENSTONE_VIX_FLOPPY_DISK));
                //stacks.add(RAVENSTONE_GUIDE);
            }).build();
}
