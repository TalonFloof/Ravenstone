package sh.talonfox.ravenstone.blocks.peripherals;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sh.talonfox.ravenstone.blocks.BlockRegister;
import sh.talonfox.ravenstone.sounds.SoundEventRegister;

public class HarddriveBlockEntity extends PeripheralBlockEntity {
    public int Flags = 0;
    private long spinTicks = 0;
    public boolean isReady = false;

    public HarddriveBlockEntity(BlockPos pos, BlockState state) {
        super(BlockRegister.RAVEN_HARDDRIVE_ENTITY, pos, state, 4);
    }

    public static void tick(World world, BlockPos pos, BlockState state, HarddriveBlockEntity blockEntity) {
        if(world.isClient())
            return;
        if((blockEntity.Flags & 0x8) != 0) {
            if(blockEntity.spinTicks == 0) {
                world.playSound(null, pos, SoundEventRegister.HARD_DRIVE_STARTUP_EVENT, SoundCategory.BLOCKS, 0.5f, 1f);
            } else if(blockEntity.spinTicks >= 600) {
                if((blockEntity.spinTicks % 100) == 0) {
                    world.playSound(null, pos, SoundEventRegister.HARD_DRIVE_IDLE_EVENT, SoundCategory.BLOCKS, 0.5f, 1f);
                }
            }
            if(blockEntity.spinTicks == 600) {
                blockEntity.isReady = true;
                world.updateListeners(pos,state,state,3);
                blockEntity.markDirty();
            }
            blockEntity.spinTicks += 1;
        } else {
            if((blockEntity.spinTicks % 100) != 0 && blockEntity.spinTicks >= 600) {
                blockEntity.spinTicks += 1;
            } else if(blockEntity.spinTicks >= 600) {
                world.playSound(null, pos, SoundEventRegister.HARD_DRIVE_SPINDOWN_EVENT, SoundCategory.BLOCKS, 0.5f, 1f);
                blockEntity.spinTicks = 0;
            }
            blockEntity.isReady = false;
            world.updateListeners(pos,state,state,3);
            blockEntity.markDirty();
        }
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        Flags = tag.getInt("Flags");
        isReady = tag.getBoolean("IsReady");
    }
    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        tag.putInt("Flags", Flags);
        tag.putBoolean("IsReady", isReady);
    }
}
