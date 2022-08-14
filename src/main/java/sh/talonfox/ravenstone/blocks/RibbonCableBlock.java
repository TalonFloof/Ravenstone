package sh.talonfox.ravenstone.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class RibbonCableBlock extends Block {
    public static final BooleanProperty UP = BooleanProperty.of("up");
    public static final BooleanProperty DOWN = BooleanProperty.of("down");
    public static final BooleanProperty NORTH = BooleanProperty.of("north");
    public static final BooleanProperty SOUTH = BooleanProperty.of("south");
    public static final BooleanProperty EAST = BooleanProperty.of("east");
    public static final BooleanProperty WEST = BooleanProperty.of("west");

    public RibbonCableBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(UP,false).with(DOWN,false).with(NORTH,false).with(SOUTH,false).with(EAST,false).with(WEST,false));
    }
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(UP);
        stateManager.add(DOWN);
        stateManager.add(NORTH);
        stateManager.add(SOUTH);
        stateManager.add(EAST);
        stateManager.add(WEST);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return updateState(ctx.getWorld(),ctx.getBlockPos());
    }

    private BlockState updateState(World world, BlockPos pos) {
        return this.stateManager.getDefaultState()
                .with(UP, canConnect(world,pos.up()))
                .with(DOWN, canConnect(world,pos.down()))
                .with(NORTH, canConnect(world,pos.north()))
                .with(SOUTH, canConnect(world,pos.south()))
                .with(EAST, canConnect(world,pos.east()))
                .with(WEST, canConnect(world,pos.west()));
    }

    private boolean canConnect(World world, BlockPos pos) {
        var blockState = world.getBlockState(pos);
        if(blockState.isAir())
            return false;
        var block = blockState.getBlock();
        return ((block instanceof ComputerBlock) || (block instanceof PeripheralBlock) || (block instanceof RibbonCableBlock));
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
        BlockState newState = updateState(world, pos);
        if(!newState.getProperties().stream().allMatch(p -> newState.get(p).equals(state.get(p))))
            world.setBlockState(pos, newState);
    }

    public VoxelShape getShape(BlockState state) {
        VoxelShape shape = Block.createCuboidShape(6,6,6,10,10,10);
        if (state.get(UP))
            shape = VoxelShapes.union(shape,Block.createCuboidShape(6,10,6,10,16,10));
        if (state.get(DOWN))
            shape = VoxelShapes.union(shape,Block.createCuboidShape(6,0,6,10,6,10));
        if (state.get(NORTH))
            shape = VoxelShapes.union(shape,Block.createCuboidShape(6,6,0,10,10,6));
        if (state.get(SOUTH))
            shape = VoxelShapes.union(shape,Block.createCuboidShape(6,6,10,10,10,16));
        if (state.get(EAST))
            shape = VoxelShapes.union(shape,Block.createCuboidShape(10,6,6,16,10,10));
        if (state.get(WEST))
            shape = VoxelShapes.union(shape,Block.createCuboidShape(0,6,6,6,10,10));
        return shape;
    }
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext ctx) {return getShape(state);}
}
