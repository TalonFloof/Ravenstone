package sh.talonfox.ravenstone.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import sh.talonfox.ravenstone.client.TerminalScreen;

import java.util.Arrays;

public class TerminalBlock extends PeripheralBlock {

    protected TerminalBlock(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TerminalBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if(world.isClient()) {
            var mc = MinecraftClient.getInstance();
            mc.setScreen(new TerminalScreen(Text.translatable("block.ravenstone.terminal"),(TerminalBlockEntity)world.getBlockEntity(pos)));
        }
        return ActionResult.SUCCESS;
    }
}
