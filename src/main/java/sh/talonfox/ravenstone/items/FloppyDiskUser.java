package sh.talonfox.ravenstone.items;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.List;

public class FloppyDiskUser extends Item implements FloppyDisk {
    public FloppyDiskUser(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        var label = this.getLabel(itemStack);
        if(label != null) {
            tooltip.add(Text.of(this.getLabel(itemStack)).copy().formatted(Formatting.GRAY));
        } else {
            tooltip.add(Text.of("No Name").copy().formatted(Formatting.DARK_GRAY));
        }
    }

    @Override
    public String getLabel(ItemStack stack) {
        return stack.getOrCreateNbt().contains("Label")?stack.getOrCreateNbt().getString("Label"):null;
    }

    @Override
    public void setLabel(ItemStack stack, String str) {
        stack.getOrCreateNbt().putString("Label",str);
    }

    private void initializeSectorNbtData(ItemStack stack) {
        NbtCompound sectors = new NbtCompound();
        for(int i=0;i < 2048;i++) {
            sectors.putByteArray(Integer.toString(i),new byte[0]);
        }
        stack.getOrCreateNbt().put("Sectors",sectors);
    }

    @Override
    public byte[] readSector(ItemStack stack, ServerWorld world, int index) {
        if(stack.getOrCreateNbt().contains("Sectors")) {
            return stack.getOrCreateNbt().getCompound("Sectors").getByteArray(Integer.toString(index));
        } else {
            initializeSectorNbtData(stack);
            return stack.getOrCreateNbt().getCompound("Sectors").getByteArray(Integer.toString(index));
        }
    }
    @Override
    public void writeSector(ItemStack stack, ServerWorld world, int index, byte[] data) {

    }
}
