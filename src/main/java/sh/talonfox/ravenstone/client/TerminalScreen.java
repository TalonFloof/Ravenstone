package sh.talonfox.ravenstone.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import sh.talonfox.ravenstone.blocks.TerminalBlockEntity;

public class TerminalScreen extends Screen {
    private TerminalBlockEntity BlockEntity;
    private long Ticks = 0;
    public TerminalScreen(Text title, TerminalBlockEntity blockEntity) {
        super(title);
        BlockEntity = blockEntity;
    }
    private void drawBackground(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        fill(matrices, 0, 0, 640, 400, 0xFF001B13);
    }

    private void drawScreen(MatrixStack matrices, int mouseX, int mouseY, float delta) {

    }

    private void drawCursor(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if(Ticks%20<10) {
            int alpha = (int)(((float)Math.min(2,Ticks%20) / 2F) * 255) << 24;
            fill(matrices, BlockEntity.CursorX * 8, BlockEntity.CursorY * 8, (BlockEntity.CursorX * 8) + 7, (BlockEntity.CursorY * 8) + 7, 0x00d187 | alpha);
        } else {
            int alpha = (int)(((float)Math.max(0,(5-((Ticks%20)-10))) / 5F) * 255) << 24;
            fill(matrices, BlockEntity.CursorX * 8, BlockEntity.CursorY * 8, (BlockEntity.CursorX * 8) + 7, (BlockEntity.CursorY * 8) + 7, 0x00d187 | alpha);
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        renderBackground(matrices);
        matrices.push();
        matrices.scale(0.5F,0.5F,1F);
        matrices.translate(((width*2) - 640) / 2, ((height*2) - 400) / 2,0);
        drawBackground(matrices, mouseX, mouseY, delta);
        drawScreen(matrices, mouseX, mouseY, delta);
        drawCursor(matrices, mouseX, mouseY, delta);
        matrices.pop();
    }

    @Override
    public void tick() {
        Ticks++;
    }

    @Override
    public boolean shouldPause() {return false;}
}
