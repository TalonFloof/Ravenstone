package sh.talonfox.ravenstone.items;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import sh.talonfox.ravenstone.ItemGroupRegister;

public class ItemRegister {
    public static final Item RAVENSTONE_CUSTOM_ICON = new Item(new FabricItemSettings().group(ItemGroupRegister.ITEM_GROUP).maxCount(1));
    public static final Item RAVENSTONE_TRANSISTOR = new Item(new FabricItemSettings().group(ItemGroupRegister.ITEM_GROUP));
    public static final Item RAVENSTONE_FLOPPY_DISK_USER = new FloppyDiskUser(new FabricItemSettings().group(ItemGroupRegister.ITEM_GROUP).maxCount(1));
    public static final Item RAVENSTONE_MAGPIE_FLOPPY_DISK = new FloppyDiskSystem("Magpie",new FabricItemSettings().group(ItemGroupRegister.ITEM_GROUP).maxCount(1));;

    public static void Initialize() {
        Registry.register(Registry.ITEM, new Identifier("ravenstone", "ravenstone"), RAVENSTONE_CUSTOM_ICON);
        Registry.register(Registry.ITEM, new Identifier("ravenstone","transistor"), RAVENSTONE_TRANSISTOR);
        Registry.register(Registry.ITEM, new Identifier("ravenstone","floppy_disk"), RAVENSTONE_FLOPPY_DISK_USER);
        Registry.register(Registry.ITEM, new Identifier("ravenstone","floppy_disk_magpie"), RAVENSTONE_MAGPIE_FLOPPY_DISK);
    }
}
