package sh.talonfox.ravenstone.items;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import sh.talonfox.ravenstone.ResourceRegister;

import java.util.Arrays;
import java.util.List;

public class FloppyDiskSystem extends Item implements FloppyDisk {
    private String Label;

    public FloppyDiskSystem(String name, Settings settings) {
        super(settings);
        Label = name;
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
        return Label;
    }

    @Override
    public void setLabel(ItemStack stack, String str) {}

    @Override
    public byte[] readSector(ItemStack stack, ServerWorld world, int index) {
        byte[] sectors = ResourceRegister.IMAGES.get(Label);
        if(sectors == null) return new byte[0];
        if((index*128) > sectors.length) return new byte[0];
        else return Arrays.copyOfRange(sectors, (index * 128), (index * 128) + 128);
    }
    @Override
    public void writeSector(ItemStack stack, ServerWorld world, int index, byte[] data) {}
}
