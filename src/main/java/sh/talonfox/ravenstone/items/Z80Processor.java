package sh.talonfox.ravenstone.items;

import net.minecraft.item.Item;
import sh.talonfox.ravenstone.processor.Processor;
import sh.talonfox.ravenstone.processor.ProcessorItem;
import sh.talonfox.ravenstone.processor.Z80;

public class Z80Processor extends Item implements ProcessorItem  {
    public Z80Processor(Item.Settings settings) {
        super(settings);
    }

    @Override
    public Processor processorClass() {
        return new Z80();
    }
}
