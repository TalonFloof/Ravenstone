package sh.talonfox.ravenstone.blocks.upgrades;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class RAM32KBlock extends RAMUpgradeBlock {
    public RAM32KBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {return new RAM32KBlockEntity(pos, state);}
}
