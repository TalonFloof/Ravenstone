package sh.talonfox.ravenstone.blocks.upgrades;

import net.minecraft.block.*;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import sh.talonfox.ravenstone.blocks.ComputerBlock;
import sh.talonfox.ravenstone.blocks.ComputerBlockEntity;

public abstract class RAMUpgradeBlock extends BlockWithEntity implements BlockEntityProvider {

    public RAMUpgradeBlock(Settings settings) {
        super(settings);
    }
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        if(world.getBlockEntity(pos.north()) instanceof ComputerBlockEntity) {
            BlockPos upgradePos = pos.north().add(world.getBlockState(pos.north()).get(Properties.HORIZONTAL_FACING).getOpposite().getVector());
            if(upgradePos.equals(pos))
                return super.canPlaceAt(state,world,pos);
        }
        if(world.getBlockEntity(pos.south()) instanceof ComputerBlockEntity) {
            BlockPos upgradePos = pos.south().add(world.getBlockState(pos.south()).get(Properties.HORIZONTAL_FACING).getOpposite().getVector());
            if(upgradePos.equals(pos))
                return super.canPlaceAt(state,world,pos);
        }
        if(world.getBlockEntity(pos.east()) instanceof ComputerBlockEntity) {
            BlockPos upgradePos = pos.east().add(world.getBlockState(pos.east()).get(Properties.HORIZONTAL_FACING).getOpposite().getVector());
            if(upgradePos.equals(pos))
                return super.canPlaceAt(state,world,pos);
        }
        if(world.getBlockEntity(pos.west()) instanceof ComputerBlockEntity) {
            BlockPos upgradePos = pos.west().add(world.getBlockState(pos.west()).get(Properties.HORIZONTAL_FACING).getOpposite().getVector());
            if(upgradePos.equals(pos))
                return super.canPlaceAt(state,world,pos);
        }
        return false;
    }
    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
        if(sourceBlock instanceof ComputerBlock) {
            if ((world.getBlockEntity(pos.north()) instanceof ComputerBlockEntity)) {
                if(pos.north().add(world.getBlockState(pos.north()).get(Properties.HORIZONTAL_FACING).getOpposite().getVector()).equals(pos))
                    return;
            }
            if ((world.getBlockEntity(pos.south()) instanceof ComputerBlockEntity)) {
                if(pos.south().add(world.getBlockState(pos.south()).get(Properties.HORIZONTAL_FACING).getOpposite().getVector()).equals(pos))
                    return;
            }
            if ((world.getBlockEntity(pos.east()) instanceof ComputerBlockEntity)) {
                if(pos.east().add(world.getBlockState(pos.east()).get(Properties.HORIZONTAL_FACING).getOpposite().getVector()).equals(pos))
                    return;
            }
            if ((world.getBlockEntity(pos.west()) instanceof ComputerBlockEntity)) {
                if(pos.west().add(world.getBlockState(pos.west()).get(Properties.HORIZONTAL_FACING).getOpposite().getVector()).equals(pos))
                    return;
            }
            world.breakBlock(pos, true, null);
        }
    }
}
