package sh.talonfox.ravenstone.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import sh.talonfox.ravenstone.blocks.TerminalBlockEntity;

import java.util.Objects;

public class TerminalScreen extends Screen {
    private static final Identifier CHARSET = new Identifier("ravenstone", "textures/gui/raven_terminal_font.png");
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
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, CHARSET);
        for(int y=0; y < 50; y++) {
            for(int x=0; x < 80; x++) {
                int val = Byte.toUnsignedInt(BlockEntity.ScreenBuffer[y*80+x]);
                if(val != 0x20) {
                    int highNibble = (val & 0xF0) >> 4;
                    int lowNibble = val & 0xF;
                    drawTexture(matrices, x * 8, y * 8, 8, 8, (float) (lowNibble * 8), (float) (highNibble * 8), 8, 8, 128, 128);
                }
            }
        }
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
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if(super.keyPressed(key, scanCode, modifiers))
            return true;
        byte result = 0;
        switch(key) {
            case GLFW.GLFW_KEY_BACKSPACE: {
                result = 0x08;
                break;
            }
            case GLFW.GLFW_KEY_ENTER: {
                result = 0x0d;
                break;
            }
            case GLFW.GLFW_KEY_HOME: {
                result = (byte)0x80;
                break;
            }
            case GLFW.GLFW_KEY_END: {
                result = (byte)0x81;
                break;
            }
            case GLFW.GLFW_KEY_UP: {
                result = (byte)0x82;
                break;
            }
            case GLFW.GLFW_KEY_DOWN: {
                result = (byte)0x83;
                break;
            }
            case GLFW.GLFW_KEY_LEFT: {
                result = (byte)0x84;
                break;
            }
            case GLFW.GLFW_KEY_RIGHT: {
                result = (byte)0x85;
                break;
            }
        }
        if (result != 0)
            pushKey(result);
        return result != 0;
    }
    @Override
    public boolean charTyped(char c, int modifiers) {
        if(super.charTyped(c, modifiers))
            return true;
        byte result = ((c>=1&&c<=0x7f)?(byte)c:0);
        if(result != 0)
            pushKey(result);
        return result != 0;
    }

    private void pushKey(byte c) {

    }

    @Override
    public void tick() {
        Ticks++;
    }

    @Override
    public boolean shouldPause() {return false;}
}
