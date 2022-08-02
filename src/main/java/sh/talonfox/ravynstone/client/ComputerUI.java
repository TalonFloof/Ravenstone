package sh.talonfox.ravynstone.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;
import sh.talonfox.ravynstone.blocks.ComputerBlockEntity;

import java.util.Date;
import java.util.Random;

public class ComputerUI extends Screen {
    private static final Identifier TEXTURE = new Identifier("ravynstone", "textures/gui/ravyn_computer.png");
    private ComputerBlockEntity BlockEntity;
    private short SwitchInput = 0;
    public ComputerUI(MutableText title, ComputerBlockEntity blockEntity) {
        super(title);
        BlockEntity = blockEntity;
    }

    public void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        int x = (width - 340) / 2;
        int y = (height - 217) / 2;
        drawTexture(matrices,x,y,340,217,0.0F,0.0F,227,145,256,256);
        drawTexture(matrices,x+340-32, y+64, 16, 36, 64F+(BlockEntity.Powered?8F:0F), 145.0F, 8, 18, 256, 256);
    }
    public void renderAddress(MatrixStack matrices) {
        int x = (width - 256) / 2;
        int ledX = (width - 192) / 2;
        int y = Math.round((height + 217.0F) / 2.0F)-36-16;
        int pc = Short.toUnsignedInt(BlockEntity.CPU.PC);
        for(int i = 0; i < 16; i++) {
            drawTexture(matrices,ledX + ((15 - i) * 12),y-20,12,10,0.0F,163.0F,12,10,256,256);
            if((pc&(1<<i))!=0&&BlockEntity.Powered)
                drawTexture(matrices,ledX + ((15 - i) * 12) + 2,y-19,8,8,12.0F,163.0F,8,8,256,256);
            drawTexture(matrices,x + ((15 - i) * 16), y, 16, 36, (((i % 6) < 3)?0.0F:16.0F)+((SwitchInput&(1<<i))!=0?8F:0F), 145.0F, 8, 18, 256, 256);
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        renderBackground(matrices);
        //////////////////////////////////////////////////
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        drawBackground(matrices,delta,mouseX,mouseY);
        renderAddress(matrices);
        //////////////////////////////////////////////////
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int bgrX = (width - 340) / 2;
        int bgrY = (height - 217) / 2;
        int addrX = (width - 256) / 2;
        int addrY = Math.round((height + 217.0F) / 2.0F)-36-16;
        if(mouseX >= addrX && mouseX <= addrX+(16*16) && mouseY >= addrY && mouseY <= addrY+36) {
            for(int i = 0; i < 16; i++) {
                if(mouseX >= addrX+((15-i)*16) && mouseX <= addrX+((15-i)*16)+16) {
                    if((SwitchInput&(1<<i))==0)
                        SwitchInput = (short)(((int)SwitchInput)|(1<<i));
                    else
                        SwitchInput = (short)(((int)SwitchInput)&(~(1<<i)));
                }
            }
        } else if(mouseX >= (bgrX+340-32) && mouseX <= (bgrX+340-32)+16 && mouseY >= bgrY+64 && mouseY <= bgrY+64+36) {
            var state = BlockEntity.getCachedState();
            BlockEntity.Powered = !BlockEntity.Powered;
            if(BlockEntity.Powered) {
                var rand = new Random();
                rand.setSeed(new Date().getTime());
                BlockEntity.CPU.A = (short)rand.nextInt();
                BlockEntity.CPU.B = (short)rand.nextInt();
                BlockEntity.CPU.D = (short)rand.nextInt();
                BlockEntity.CPU.I = (short)rand.nextInt();
                BlockEntity.CPU.X = (short)rand.nextInt();
                BlockEntity.CPU.Y = (short)rand.nextInt();
                BlockEntity.CPU.PC = (short)rand.nextInt();
                BlockEntity.CPU.RP = (short)rand.nextInt();
                BlockEntity.CPU.SP = (short)rand.nextInt();
                BlockEntity.CPU.FlagC = rand.nextBoolean();
                BlockEntity.CPU.FlagZ = rand.nextBoolean();
                BlockEntity.CPU.FlagI = rand.nextBoolean();
                BlockEntity.CPU.FlagD = rand.nextBoolean();
                BlockEntity.CPU.FlagX = rand.nextBoolean();
                BlockEntity.CPU.FlagM = rand.nextBoolean();
                BlockEntity.CPU.FlagV = rand.nextBoolean();
                BlockEntity.CPU.FlagN = rand.nextBoolean();
                BlockEntity.CPU.FlagE = rand.nextBoolean();
                BlockEntity.CPU.ResetAddr = (short)rand.nextInt();
                BlockEntity.CPU.BrkAddr = (short)rand.nextInt();
                BlockEntity.CPU.BusOffset = rand.nextInt() & 0xFFFF;
                BlockEntity.CPU.BusEnabled = rand.nextBoolean();
                BlockEntity.CPU.Error = rand.nextBoolean();
            }
            BlockEntity.markDirty();
            BlockEntity.getWorld().updateListeners(BlockEntity.getPos(), state, BlockEntity.getCachedState(), Block.NOTIFY_LISTENERS);
        }
        return true;
    }

    @Override
    public boolean shouldPause() {return false;}

    @Override
    protected void init() {
        super.init();
    }
}
