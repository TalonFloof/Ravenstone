package sh.talonfox.ravynstone.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

public class ComputerUI extends Screen {
    private static final Identifier FRONTPLATE_TEXTURE = new Identifier("ravynstone", "textures/gui/ravyn_computer_background.png");
    public ComputerUI(MutableText title) {
        super(title);
    }

    public void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, FRONTPLATE_TEXTURE);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        int x = (width - 340) / 2;
        int y = (height - 217) / 2;
        drawTexture(matrices,x,y,340,217,0,0,227,145,227,145);
        
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        renderBackground(matrices);
        drawBackground(matrices,delta,mouseX,mouseY);
    }

    @Override
    public boolean shouldPause() {return false;}

    @Override
    protected void init() {
        super.init();
    }
}
