package sh.talonfox.ravenstone;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import sh.talonfox.ravenstone.processor.Processor;

import java.io.InputStream;
import java.util.HashMap;

public class ResourceRegister {
    public static HashMap<String,String> ROM_FILES = new HashMap<>();
    public static HashMap<String,byte[]> ROMS = new HashMap<>();
    public static HashMap<String,String> IMAGE_FILES = new HashMap<>();
    public static HashMap<String,byte[]> IMAGES = new HashMap<>();
    public static void Initialize() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public void reload(ResourceManager manager) {
                Ravenstone.LOGGER.info("Reloading Data!");
                var rom_list = manager.findAllResources("roms",(a) -> a.getNamespace().equals("ravenstone"));
                var floppy_list = manager.findAllResources("disks",(a) -> a.getNamespace().equals("ravenstone"));
                rom_list.keySet().stream().forEach((i) -> {
                    var name = i.getPath().substring(i.getPath().lastIndexOf("/")+1);
                    if(ROM_FILES.containsKey(name)) {
                        ROMS.put(ROM_FILES.get(name),loadImage(manager,"roms/"+name));
                    } else {
                        Ravenstone.LOGGER.warn("Unused ROM image: {}", i);
                    }
                });
                floppy_list.keySet().stream().forEach((i) -> {
                    var name = i.getPath().substring(i.getPath().lastIndexOf("/")+1);
                    if(IMAGE_FILES.containsKey(name)) {
                        IMAGES.put(IMAGE_FILES.get(name),loadImage(manager,"disks/"+name));
                    } else {
                        Ravenstone.LOGGER.warn("Unused Floppy image: {}", i);
                    }
                });
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

    public static void RegisterROM(String archName, String fileName) {
        ROM_FILES.put(fileName, archName);
    }

    public static void RegisterFloppy(String label, String fileName) {
        IMAGE_FILES.put(fileName, label);
    }
}
