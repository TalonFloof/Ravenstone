package sh.talonfox.ravenstone.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import sh.talonfox.ravenstone.items.FloppyDisk;
import sh.talonfox.ravenstone.sounds.SoundEventRegister;

/*
Some code used in this file is adapted from 2xsaiko's Retrocomputers mod.
The original code can be found here: https://github.com/2xsaiko/retrocomputers/blob/master/src/main/kotlin/common/block/DiskDrive.kt
Retrocomputers is under the MIT License.
 */

public class FloppyDriveBlockEntity extends PeripheralBlockEntity {
    private ItemStack stack = Items.AIR.getDefaultStack();
    private long spinTicks = 0;

    public FloppyDriveBlockEntity(BlockPos pos, BlockState state) {
        super(BlockRegister.RAVEN_FLOPPY_DRIVE_ENTITY, pos, state);
    }

    public boolean insertDisk(ItemStack stack) {
        var world = this.getWorld();
        if(stack.isEmpty() || !(stack.getItem() instanceof FloppyDisk))
            return false;
        this.stack = stack.split(1);
        assert world != null;
        world.setBlockState(getPos(), this.getCachedState().with(FloppyDriveBlock.HAS_DISK, true));
        if(!world.isClient()) {
            world.playSound(null, getPos(), SoundEventRegister.DISKETTE_INSERT_SOUND_EVENT, SoundCategory.BLOCKS, 1f, 1f);
        }
        return true;
    }

    public boolean ejectDisk(boolean broken) {
        var world = this.getWorld();
        if(world == null)
            return false;
        if(world.isClient())
            return true;
        if(!broken) {
            var block_direction = Vec3d.of(this.getCachedState().get(Properties.HORIZONTAL_FACING).getVector());
            var block_position = Vec3d.ofCenter(pos).add(block_direction.multiply(0.75));
            var item = new ItemEntity(world, block_position.x, block_position.y, block_position.z, stack);
            item.setVelocity(block_direction.multiply(0.1));
            world.spawnEntity(item);
            world.setBlockState(getPos(), this.getCachedState().with(FloppyDriveBlock.HAS_DISK, false));
            world.playSound(null, getPos(), SoundEventRegister.DISKETTE_EJECT_SOUND_EVENT, SoundCategory.BLOCKS, 1f, 1f);
        } else {
            ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), stack);
        }
        stack = Items.AIR.getDefaultStack();
        return true;
    }

    public static void tick(World world, BlockPos pos, BlockState state, FloppyDriveBlockEntity blockEntity) {
        if(world.isClient())
            return;
        if(state.get(FloppyDriveBlock.HAS_DISK) && state.get(FloppyDriveBlock.LIGHT)) {
            if (blockEntity.spinTicks == 0) {
                world.playSound(null, pos, SoundEventRegister.DISKETTE_START_SOUND_EVENT, SoundCategory.BLOCKS, 1f, 1f);
            } else if (blockEntity.spinTicks >= 4) {
                if (((blockEntity.spinTicks - 4) % 24) == 0) {
                    world.playSound(null, pos, SoundEventRegister.DISKETTE_SPIN_SOUND_EVENT, SoundCategory.BLOCKS, 1f, 1f);
                }
            }
            blockEntity.spinTicks += 1;
        } else {
            blockEntity.spinTicks = 0;
        }
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        stack = ItemStack.fromNbt(tag.getCompound("Item"));
    }
    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        tag.put("Item", stack.writeNbt(new NbtCompound()));
    }
}
