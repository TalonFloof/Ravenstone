package sh.talonfox.ravenstone;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import sh.talonfox.ravenstone.items.FloppyDiskSystem;
import sh.talonfox.ravenstone.items.ItemRegister;
import sh.talonfox.ravenstone.processor.Processor;

import java.io.InputStream;
import java.util.HashMap;

public class ResourceRegister {
    public static HashMap<String,byte[]> IMAGES = new HashMap<>();
    public static void Initialize() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public void reload(ResourceManager manager) {
                Ravenstone.LOGGER.info("Reloading Data!");
                Processor.MONITOR = loadImage(manager, "monitor.bin");
                IMAGES.put("Magpie",loadImage(manager,"disks/magpie.bin"));
            }

            @Override
            public Identifier getFabricId() {
                return new Identifier("ravenstone", "data");
            }
        });
    }

    private static byte[] loadImage(ResourceManager manager, String name) {
        try(InputStream stream = manager.getResource(new Identifier("ravenstone", name)).orElseThrow().getInputStream()) {
            return stream.readAllBytes();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
