package sh.talonfox.ravenstone.blocks.upgrades;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class RAM64KBlock extends RAMUpgradeBlock {
    public RAM64KBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {return new RAM64KBlockEntity(pos, state);}
}
