package sh.talonfox.ravenstone.items;

import net.minecraft.item.Item;
import sh.talonfox.ravenstone.processor.Processor;
import sh.talonfox.ravenstone.processor.ProcessorItem;
import sh.talonfox.ravenstone.processor.Talon560;

public class Talon560Processor extends Item implements ProcessorItem {
    public Talon560Processor(Item.Settings settings) {
        super(settings);
    }

    @Override
    public Processor processorClass() {
        return new Talon560();
    }
}
