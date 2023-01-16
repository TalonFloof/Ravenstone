package sh.talonfox.ravenstone.blocks.peripherals;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import sh.talonfox.ravenstone.blocks.BlockRegister;
import sh.talonfox.ravenstone.client.HardDriveScreen;

public class HarddriveBlock extends PeripheralBlock {
    public static final BooleanProperty LIGHT = BooleanProperty.of("light");

    public HarddriveBlock(Settings settings) {
        super(settings);
        setDefaultState(this.stateManager.getDefaultState().with(LIGHT,false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        super.appendProperties(stateManager);
        stateManager.add(LIGHT);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new HarddriveBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient() ? null : checkType(type, BlockRegister.RAVEN_HARDDRIVE_ENTITY, HarddriveBlockEntity::tick);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        var result = super.onUse(state,world,pos,player,hand,hit);
        if(result != ActionResult.PASS)
            return result;
        if(world.isClient()) {
            var mc = MinecraftClient.getInstance();
            mc.setScreen(new HardDriveScreen(Text.translatable("block.ravenstone.hard_drive"),(HarddriveBlockEntity)world.getBlockEntity(pos)));
        }
        return ActionResult.SUCCESS;
    }
}
