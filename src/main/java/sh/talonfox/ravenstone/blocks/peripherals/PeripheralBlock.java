package sh.talonfox.ravenstone.blocks.peripherals;

import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import sh.talonfox.ravenstone.client.BusIDScreen;

public abstract class PeripheralBlock extends BlockWithEntity implements BlockEntityProvider {
    protected PeripheralBlock(Settings settings) {
        super(settings);
        setDefaultState(this.stateManager.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return (BlockState)this.getDefaultState().with(Properties.HORIZONTAL_FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {stateManager.add(Properties.HORIZONTAL_FACING);}

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if(player.isSneaking()) {
            if(!world.isClient())
                return ActionResult.SUCCESS;
            MinecraftClient mc = MinecraftClient.getInstance();
            mc.setScreen(new BusIDScreen(Text.translatable("gui.ravenstone.busid"),(PeripheralBlockEntity)world.getBlockEntity(pos)));
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
}
