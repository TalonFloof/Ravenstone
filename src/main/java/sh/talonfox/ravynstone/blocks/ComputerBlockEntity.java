package sh.talonfox.ravynstone.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sh.talonfox.ravynstone.processor.Processor;
import sh.talonfox.ravynstone.processor.ProcessorHost;

public class ComputerBlockEntity extends BlockEntity implements ProcessorHost {
    Processor CPU = new Processor(this);
    byte[] RAM = new byte[16384];

    public ComputerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockRegister.RAVYN_COMPUTER_ENTITY, pos, state);
    }

    @Override
    public void resetBusState() {

    }

    @Override
    public byte memRead(short at) {
        return RAM[Short.toUnsignedInt(at)];
    }

    @Override
    public void memStore(short at, byte data) {
        RAM[Short.toUnsignedInt(at)] = data;
    }
    public static void tick(World world, BlockPos pos, BlockState state, ComputerBlockEntity blockEntity) {

    }
}