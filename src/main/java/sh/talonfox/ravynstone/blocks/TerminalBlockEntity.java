package sh.talonfox.ravynstone.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class TerminalBlockEntity extends BlockEntity {
    public TerminalBlockEntity(BlockPos pos, BlockState state) {
        super(BlockRegister.RAVYN_TERMINAL_ENTITY, pos, state);
    }
}
