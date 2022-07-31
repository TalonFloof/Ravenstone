package sh.talonfox.ravynstone.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import sh.talonfox.ravynstone.processor.Processor;

public class ComputerBlockEntity extends BlockEntity {

    sh.talonfox.ravynstone.processor.Processor Processor = new Processor();
    byte[] RAM = new byte[16384];
    public ComputerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockRegister.RAVYN_COMPUTER_ENTITY, pos, state);
    }
}
