package sh.talonfox.ravenstone.processor;

import net.minecraft.item.ItemStack;

public interface Processor {
    boolean isStopped();
    void setStop(boolean flag);
    boolean isWaiting();
    void setWait(boolean flag);

    int insnPerSecond();

    void reset();
    void next(ProcessorHost host);
    void saveNBT(ItemStack stack);
    void loadNBT(ItemStack stack);
}
