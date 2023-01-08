package sh.talonfox.ravenstone.blocks.upgrades;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import static sh.talonfox.ravenstone.blocks.BlockRegister.RAVEN_16K_UPGRADE_ENTITY;

public class RAM16KBlockEntity extends RAMUpgradeBlockEntity {
    public byte[] RAM = new byte[(16384-8192)];
    public RAM16KBlockEntity(BlockPos pos, BlockState state) {
        super(RAVEN_16K_UPGRADE_ENTITY, pos, state);
    }
    @Override
    public byte readData(int at) {
        if(at < RAM.length) {
            return RAM[at];
        }
        return (byte)0xFF;
    }
    @Override
    public void storeData(int at, byte data) {
        if(at < RAM.length) {
            RAM[at] = data;
        }
    }
    /*@Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        RAM = tag.getByteArray("RAM");
    }
    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        tag.putByteArray("RAM", RAM);
    }*/
}
