package sh.talonfox.ravenstone.items;

import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;

import java.util.UUID;

public interface FloppyDisk {
    String getLabel(ItemStack stack);
    void setLabel(ItemStack stack, String str);
    byte[] readSector(ItemStack stack, ServerWorld world, int index);
    void writeSector(ItemStack stack, ServerWorld world, int index, byte[] data);
}
