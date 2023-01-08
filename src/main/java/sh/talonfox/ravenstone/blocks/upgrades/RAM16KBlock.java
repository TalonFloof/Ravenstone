package sh.talonfox.ravenstone.blocks.upgrades;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class RAM16KBlock extends RAMUpgradeBlock {
    public RAM16KBlock(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {return new RAM16KBlockEntity(pos, state);}
}
