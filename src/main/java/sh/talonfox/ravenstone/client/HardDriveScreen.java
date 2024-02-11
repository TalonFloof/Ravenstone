package sh.talonfox.ravenstone.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
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

    public void drawBackground(DrawContext d) {
        int x = (width-138)/2;
        int y = (height-89)/2;
        d.drawTexture(TEXTURE,x,y,138,89,0F,0F,138,89,256,256);
        d.getMatrices().push();
        d.getMatrices().scale(0.5F,0.5F,1F);
        d.drawText(textRenderer,"Hard Drive",(int)((x+4)*2F),(int)((y+4)*2F),0xFFF0F0F0,false);
        d.getMatrices().pop();
        RenderSystem.setShaderTexture(0, TEXTURE);
        for(int i=0;i<4;i++) {
            int invI = 3-i;
            int switchX = ((width-64)/2)+(invI*16);
            int switchY = (height+36)/2;
            if(i == 2) {
                d.drawTexture(TEXTURE, switchX, switchY, 16, 16, 138F + (BlockEntity.isReady ? 8F : 0F), 0F, 8, 8, 256, 256);
            } else {
                d.drawTexture(TEXTURE, switchX, switchY, 16, 16, 138F + ((BlockEntity.Flags & (1 << i)) != 0 ? 8F : 0F), 0F, 8, 8, 256, 256);
            }
            switch(i) {
                case 0 -> {
                    d.getMatrices().push();
                    d.getMatrices().scale(0.5F,0.5F,1F);
                    d.drawCenteredTextWithShadow(textRenderer,"FAULT",(switchX*2)+16,(switchY*2)-24,0xFFF0F0F0);
                    d.drawCenteredTextWithShadow(textRenderer,"RESET",(switchX*2)+16,(switchY*2)-16,0xFFF0F0F0);
                    d.getMatrices().pop();
                }
                case 1 -> {
                    d.getMatrices().push();
                    d.getMatrices().scale(0.5F,0.5F,1F);
                    d.drawCenteredTextWithShadow(textRenderer,"WRITE",(switchX*2)+16,(switchY*2)-24,0xFFF0F0F0);
                    d.drawCenteredTextWithShadow(textRenderer,"PROT",(switchX*2)+16,(switchY*2)-16,0xFFF0F0F0);
                    d.getMatrices().pop();
                }
                case 2 -> {
                    d.getMatrices().push();
                    d.getMatrices().scale(0.5F,0.5F,1F);
                    d.drawCenteredTextWithShadow(textRenderer,"READY",(switchX*2)+16,(switchY*2)-16,0xFFF0F0F0);
                    d.getMatrices().pop();
                }
                case 3 -> {
                    d.getMatrices().push();
                    d.getMatrices().scale(0.5F,0.5F,1F);
                    d.drawCenteredTextWithShadow(textRenderer,"START",(switchX*2)+16,(switchY*2)-24,0xFFF0F0F0);
                    d.drawCenteredTextWithShadow(textRenderer,"STOP",(switchX*2)+16,(switchY*2)-16,0xFFF0F0F0);
                    d.getMatrices().pop();
                }
            }
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
    public void render(DrawContext d, int mouseX, int mouseY, float delta) {
        super.render(d, mouseX, mouseY, delta);
        renderBackground(d);
        drawBackground(d);
    }

    @Override
    public boolean shouldPause() {return false;}
}
