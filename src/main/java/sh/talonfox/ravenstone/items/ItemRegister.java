package sh.talonfox.ravenstone.items;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import sh.talonfox.ravenstone.ItemGroupRegister;

public class ItemRegister {
    public static final Item RAVENSTONE_CUSTOM_ICON = new Item(new FabricItemSettings().group(ItemGroupRegister.ITEM_GROUP));

    public static void Initialize() {
        Registry.register(Registry.ITEM, new Identifier("ravenstone", "ravenstone"), RAVENSTONE_CUSTOM_ICON);
    }
}
