package sh.talonfox.ravenstone.blocks.peripherals;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class HarddriveBlock extends PeripheralBlock {
    public HarddriveBlock(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new HarddriveBlockEntity(pos, state);
    }
}
