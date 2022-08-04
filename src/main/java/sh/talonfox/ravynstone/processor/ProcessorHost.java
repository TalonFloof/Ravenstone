package sh.talonfox.ravynstone.processor;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.world.World;

public interface ProcessorHost {
    byte targetBus = 0;
    Boolean isBusConnected = false;


    void resetBusState();
    byte memRead(short at);
    void memStore(short at, byte data);

    void explode();
}
