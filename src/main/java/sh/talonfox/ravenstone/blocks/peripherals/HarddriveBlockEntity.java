package sh.talonfox.ravenstone.blocks.peripherals;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import sh.talonfox.ravenstone.blocks.BlockRegister;

public class HarddriveBlockEntity extends PeripheralBlockEntity {
    public HarddriveBlockEntity(BlockPos pos, BlockState state) {
        super(BlockRegister.RAVEN_HARDDRIVE_ENTITY, pos, state, 4);
    }
}
