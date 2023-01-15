package sh.talonfox.ravenstone.blocks;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import sh.talonfox.ravenstone.Ravenstone;
import sh.talonfox.ravenstone.blocks.peripherals.PeripheralBlock;

import java.util.ArrayList;
import java.util.Objects;

public class ComputerBlock extends PeripheralBlock implements BlockEntityProvider {
    public static final BooleanProperty RUNNING = BooleanProperty.of("running");
    public static final BooleanProperty HAS_CPU = BooleanProperty.of("has_cpu");
    public static ArrayList<BlockPos> toReset = new ArrayList<>();

    public ComputerBlock(Settings settings) {
        super(settings);
        setDefaultState(this.stateManager.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH).with(RUNNING,false).with(HAS_CPU,false));
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(Properties.HORIZONTAL_FACING);
        stateManager.add(RUNNING);
        stateManager.add(HAS_CPU);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        toReset.add(pos);
        return new ComputerBlockEntity(pos, state.with(RUNNING,false));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(Properties.HORIZONTAL_FACING, ctx.getPlayerFacing().getOpposite()).with(RUNNING,false).with(HAS_CPU,false);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient() ? null : checkType(type, BlockRegister.RAVEN_COMPUTER_ENTITY, ComputerBlockEntity::tick);
    }
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if(hit.getSide() == state.get(Properties.HORIZONTAL_FACING).getOpposite()) {
            ComputerBlockEntity blockEntity = (ComputerBlockEntity) world.getBlockEntity(pos);
            assert blockEntity != null;
            if(!state.get(HAS_CPU)) {
                return blockEntity.insertCPU(player.getStackInHand(hand))?ActionResult.SUCCESS:ActionResult.PASS;
            } else {
                return blockEntity.ejectCPU(false)?ActionResult.SUCCESS:ActionResult.PASS;
            }
        } else {
            var result = super.onUse(state, world, pos, player, hand, hit);
            if (result != ActionResult.PASS)
                return result;
            ComputerBlockEntity blockEntity = (ComputerBlockEntity) world.getBlockEntity(pos);
            if (!world.isClient()) {
                assert blockEntity != null;
                if(!blockEntity.CPUStack.isEmpty()) {
                    world.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.BLOCKS, 0.5F, 2.0F);
                    // Run Code
                    for(int i=0;i < 1024;i++) {
                        blockEntity.RAM.set(i, new NbtByteArray(new byte[0]));
                    }
                    blockEntity.CPU.reset();
                    world.setBlockState(pos, state.with(RUNNING, !state.get(RUNNING)));
                    blockEntity.markDirty();
                } else {
                    player.sendMessage(Text.of("You need to insert a Processor first!"),true);
                }
            }
            return ActionResult.SUCCESS;
        }
    }

    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean a) {
        if (state.getBlock() != newState.getBlock()) {
            if(world != null) {
                ((ComputerBlockEntity) Objects.requireNonNull(world.getBlockEntity(pos))).ejectCPU(true);
            }
        }
        super.onStateReplaced(state, world, pos, newState, a);
    }
}
