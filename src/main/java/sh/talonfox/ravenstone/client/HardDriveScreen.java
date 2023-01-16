package sh.talonfox.ravenstone.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import sh.talonfox.ravenstone.blocks.peripherals.HarddriveBlockEntity;
import sh.talonfox.ravenstone.network.HardDrivePackets;

public class HardDriveScreen extends Screen {
    private HarddriveBlockEntity BlockEntity;
    private static final Identifier TEXTURE = new Identifier("ravenstone", "textures/gui/raven_hard_drive_gui.png");
    public HardDriveScreen(Text title, HarddriveBlockEntity blockEntity) {
        super(title);
        BlockEntity = blockEntity;
    }

    public void drawBackground(MatrixStack matrices) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width-138)/2;
        int y = (height-89)/2;
        drawTexture(matrices,x,y,138,89,0F,0F,138,89,256,256);
        matrices.push();
        matrices.scale(0.5F,0.5F,1F);
        textRenderer.draw(matrices,"Hard Drive",(x+4)*2F,(y+4)*2F,0xFFF0F0F0);
        matrices.pop();
        RenderSystem.setShaderTexture(0, TEXTURE);
        for(int i=0;i<4;i++) {
            int invI = 3-i;
            int switchX = ((width-64)/2)+(invI*16);
            int switchY = (height+36)/2;
            if(i == 2) {
                drawTexture(matrices, switchX, switchY, 16, 16, 138F + (BlockEntity.isReady ? 8F : 0F), 0F, 8, 8, 256, 256);
            } else {
                drawTexture(matrices, switchX, switchY, 16, 16, 138F + ((BlockEntity.Flags & (1 << i)) != 0 ? 8F : 0F), 0F, 8, 8, 256, 256);
            }
            switch(i) {
                case 0 -> {
                    matrices.push();
                    matrices.scale(0.5F,0.5F,1F);
                    drawCenteredText(matrices,textRenderer,"FAULT",(switchX*2)+16,(switchY*2)-24,0xFFF0F0F0);
                    drawCenteredText(matrices,textRenderer,"RESET",(switchX*2)+16,(switchY*2)-16,0xFFF0F0F0);
                    matrices.pop();
                }
                case 1 -> {
                    matrices.push();
                    matrices.scale(0.5F,0.5F,1F);
                    drawCenteredText(matrices,textRenderer,"WRITE",(switchX*2)+16,(switchY*2)-24,0xFFF0F0F0);
                    drawCenteredText(matrices,textRenderer,"PROT",(switchX*2)+16,(switchY*2)-16,0xFFF0F0F0);
                    matrices.pop();
                }
                case 2 -> {
                    matrices.push();
                    matrices.scale(0.5F,0.5F,1F);
                    drawCenteredText(matrices,textRenderer,"READY",(switchX*2)+16,(switchY*2)-16,0xFFF0F0F0);
                    matrices.pop();
                }
                case 3 -> {
                    matrices.push();
                    matrices.scale(0.5F,0.5F,1F);
                    drawCenteredText(matrices,textRenderer,"START",(switchX*2)+16,(switchY*2)-24,0xFFF0F0F0);
                    drawCenteredText(matrices,textRenderer,"STOP",(switchX*2)+16,(switchY*2)-16,0xFFF0F0F0);
                    matrices.pop();
                }
            }
            RenderSystem.setShaderTexture(0, TEXTURE);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for(int i=0;i<4;i++) {
            int invI = 3-i;
            int switchX = ((width-64)/2)+(invI*16);
            if(mouseX >= switchX && mouseX <= switchX+16 && mouseY >= ((height+36)/2) && mouseY <= (((height+36)/2)+16)) {
                if(i == 2)
                    return true;
                if((BlockEntity.Flags&(1<<i))!=0) {
                    BlockEntity.Flags &= ~(1<<i);
                } else {
                    BlockEntity.Flags |= (1<<i);
                }
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeBlockPos(BlockEntity.getPos());
                buf.writeInt(BlockEntity.Flags);
                ClientPlayNetworking.send(HardDrivePackets.SET_HD_FLAG, buf);
                assert MinecraftClient.getInstance().player != null;
                MinecraftClient.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.MASTER, 0.25F, 2.0F);
            }
        }
        return true;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        renderBackground(matrices);
        drawBackground(matrices);
    }

    @Override
    public boolean shouldPause() {return false;}
}
