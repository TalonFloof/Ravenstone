package sh.talonfox.ravenstone.blocks.peripherals;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import sh.talonfox.ravenstone.blocks.BlockRegister;

public class ModemBlockEntity extends PeripheralBlockEntity {
    public ModemBlockEntity(BlockPos pos, BlockState state) {
        super(BlockRegister.RAVEN_MODEM_ENTITY, pos, state, 4);
    }
}
