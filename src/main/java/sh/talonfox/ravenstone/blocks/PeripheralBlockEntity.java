package sh.talonfox.ravenstone.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;

public abstract class PeripheralBlockEntity extends BlockEntity {
    public PeripheralBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public String getIdentifier() {return "\0\0\0\0\0\0\0\0";}
    public byte readData(byte at) {return 0;}
    public void storeData(byte at, byte data) {}
}
