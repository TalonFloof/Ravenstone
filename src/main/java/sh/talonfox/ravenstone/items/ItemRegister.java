package sh.talonfox.ravenstone.items;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;

public class ItemRegister {
    public static final Item RAVENSTONE_CUSTOM_ICON = new Item(new FabricItemSettings().maxCount(1));
    public static final Item RAVENSTONE_TRANSISTOR = new Item(new FabricItemSettings());
    public static final Item RAVENSTONE_FLOPPY_DISK_USER = new FloppyDiskUser(new FabricItemSettings().maxCount(1));
    public static final Item RAVENSTONE_VIX1_FLOPPY_DISK = new FloppyDiskSystem("Vix 1 (Boot)",new FabricItemSettings().maxCount(1));
    public static final Item RAVENSTONE_VIX2_FLOPPY_DISK = new FloppyDiskSystem("Vix 2 (Core Utilities)",new FabricItemSettings().maxCount(1));
    public static final Item RAVENSTONE_R3000_PROCESSOR = new R3000Processor(new FabricItemSettings().maxCount(1));

    public static void Initialize() {
        Registry.register(Registries.ITEM, new Identifier("ravenstone", "ravenstone"), RAVENSTONE_CUSTOM_ICON);
        Registry.register(Registries.ITEM, new Identifier("ravenstone","transistor"), RAVENSTONE_TRANSISTOR);
        Registry.register(Registries.ITEM, new Identifier("ravenstone","floppy_disk"), RAVENSTONE_FLOPPY_DISK_USER);
        Registry.register(Registries.ITEM, new Identifier("ravenstone","floppy_disk_vix1"), RAVENSTONE_VIX1_FLOPPY_DISK);
        Registry.register(Registries.ITEM, new Identifier("ravenstone","floppy_disk_vix2"), RAVENSTONE_VIX2_FLOPPY_DISK);
        Registry.register(Registries.ITEM, new Identifier("ravenstone","r3000_processor"), RAVENSTONE_R3000_PROCESSOR);
    }
}
