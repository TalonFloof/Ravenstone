package sh.talonfox.ravenstone.items;

import net.minecraft.item.Item;
import sh.talonfox.ravenstone.processor.Processor;
import sh.talonfox.ravenstone.processor.ProcessorItem;
import sh.talonfox.ravenstone.processor.R3000;

public class R3000Processor extends Item implements ProcessorItem {
    public R3000Processor(Item.Settings settings) {
        super(settings);
    }

    @Override
    public Processor processorClass() {
        return new R3000();
    }
}
