package sh.talonfox.ravenstone.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedList;

public abstract class PeripheralBlockEntity extends BlockEntity {
    private int BusID = 0;
    public PeripheralBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int defaultBusID) {
        super(type, pos, state);
        BusID = defaultBusID;
    }

    public String getIdentifier() {return "\0\0\0\0\0\0\0\0";}
    public byte readData(byte at) {return 0;}
    public void storeData(byte at, byte data) {}
    public int getBusID() {return BusID;}
    public void storeBusID(int newID) {BusID = newID;}

    public static PeripheralBlockEntity findPeripheral(World world, BlockPos source, int busID) {
        HashSet<BlockPos> checkedPoses = new HashSet<>();
        LinkedList<BlockPos> posQueue = new LinkedList<>();

        posQueue.addLast(source.up());
        posQueue.addLast(source.down());
        posQueue.addLast(source.north());
        posQueue.addLast(source.south());
        posQueue.addLast(source.east());
        posQueue.addLast(source.west());
        while(!posQueue.isEmpty()) {
            BlockPos currentBlock = posQueue.removeFirst();
            checkedPoses.add(currentBlock);
            if(!world.isAir(currentBlock)) {
                BlockState state = world.getBlockState(currentBlock);
                BlockEntity blockEntity = world.getBlockEntity(currentBlock);
                if(state.getBlock() instanceof RibbonCableBlock) {
                    if(state.get(RibbonCableBlock.UP))
                        if (!checkedPoses.contains(currentBlock.up()))
                            posQueue.addLast(currentBlock.up());
                    if(state.get(RibbonCableBlock.DOWN))
                        if (!checkedPoses.contains(currentBlock.down()))
                            posQueue.addLast(currentBlock.down());
                    if(state.get(RibbonCableBlock.NORTH))
                        if (!checkedPoses.contains(currentBlock.north()))
                            posQueue.addLast(currentBlock.north());
                    if(state.get(RibbonCableBlock.SOUTH))
                        if (!checkedPoses.contains(currentBlock.south()))
                            posQueue.addLast(currentBlock.south());
                    if(state.get(RibbonCableBlock.EAST))
                        if (!checkedPoses.contains(currentBlock.east()))
                            posQueue.addLast(currentBlock.east());
                    if(state.get(RibbonCableBlock.WEST))
                        if (!checkedPoses.contains(currentBlock.west()))
                            posQueue.addLast(currentBlock.west());
                } else {
                    if(!currentBlock.equals(source) && blockEntity instanceof PeripheralBlockEntity peripheral) {
                        if(peripheral.getBusID() == busID)
                            return peripheral;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        BusID = tag.getInt("BusID");
    }
    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        tag.putInt("BusID", BusID);
    }
    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }
}
