package sh.talonfox.ravenstone.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import sh.talonfox.ravenstone.blocks.peripherals.TerminalBlockEntity;
import sh.talonfox.ravenstone.network.TerminalPackets;

public class TerminalScreen extends Screen {
    private static final Identifier CHARSET = new Identifier("ravenstone", "textures/gui/raven_terminal_font.png");
    private static final Identifier FRAME = new Identifier("ravenstone", "textures/gui/raven_terminal_frame.png");
    private TerminalBlockEntity BlockEntity;
    private long Ticks = 0;
    public TerminalScreen(Text title, TerminalBlockEntity blockEntity) {
        super(title);
        BlockEntity = blockEntity;
    }
    private void drawBackground(DrawContext d, int mouseX, int mouseY, float delta) {
        d.fill(0, 0, 640, 400, 0xFF001B13);
    }

    private void drawScreen(DrawContext d, int mouseX, int mouseY, float delta) {
        for(int y=0; y < 50; y++) {
            for(int x=0; x < 80; x++) {
                int val = Byte.toUnsignedInt(BlockEntity.ScreenBuffer[y*80+x]);
                if(val != 0x20) {
                    int highNibble = (val & 0xF0) >> 4;
                    int lowNibble = val & 0xF;
                    d.drawTexture(CHARSET, x * 8, y * 8, 8, 8, (float) (lowNibble * 8), (float) (highNibble * 8), 8, 8, 128, 128);
                }
            }
        }
    }

    private void drawCursor(DrawContext d, int mouseX, int mouseY, float delta) {
        if(Ticks%20<10) {
            int alpha = (int)(((float)Math.min(2,Ticks%20) / 2F) * 255) << 24;
            d.fill(BlockEntity.CursorX * 8, BlockEntity.CursorY * 8, (BlockEntity.CursorX * 8) + 7, (BlockEntity.CursorY * 8) + 7, 0x00d187 | alpha);
        } else {
            int alpha = (int)(((float)Math.max(0,(5-((Ticks%20)-10))) / 5F) * 255) << 24;
            d.fill(BlockEntity.CursorX * 8, BlockEntity.CursorY * 8, (BlockEntity.CursorX * 8) + 7, (BlockEntity.CursorY * 8) + 7, 0x00d187 | alpha);
        }
    }

    @Override
    public void render(DrawContext d, int mouseX, int mouseY, float delta) {
        super.render(d, mouseX, mouseY, delta);
        renderBackground(d);
        d.getMatrices().push();
        d.getMatrices().scale(0.5F, 0.5F, 1F);
        d.getMatrices().translate(((width * 2) - 640) / 2, ((height * 2) - 400) / 2, 0);
        drawBackground(d, mouseX, mouseY, delta);
        d.getMatrices().push();
        d.getMatrices().translate(-24F,-24F,0);
        d.drawTexture(FRAME,0,0,640+48,400+48,0F,0F,350,230,350,230);
        d.getMatrices().pop();
        drawScreen(d, mouseX, mouseY, delta);
        drawCursor(d, mouseX, mouseY, delta);
        d.getMatrices().pop();
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
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(BlockEntity.getPos());
        buf.writeByte(c);
        ClientPlayNetworking.send(TerminalPackets.TERMINAL_KEY, buf);
    }

    @Override
    public void tick() {
        Ticks++;
    }

    @Override
    public boolean shouldPause() {return false;}
}
