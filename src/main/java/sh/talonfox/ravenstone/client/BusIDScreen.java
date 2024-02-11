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
import sh.talonfox.ravenstone.blocks.peripherals.PeripheralBlockEntity;
import sh.talonfox.ravenstone.network.PeripheralPackets;

public class BusIDScreen extends Screen {
    private static final Identifier TEXTURE = new Identifier("ravenstone", "textures/gui/raven_busid_gui.png");
    private PeripheralBlockEntity BlockEntity;
    public BusIDScreen(Text title, PeripheralBlockEntity blockEntity) {
        super(title);
        BlockEntity = blockEntity;
    }
    public void drawBackground(DrawContext d) {
        int busID = BlockEntity.getBusID();
        int x = (width-138)/2;
        int y = (height-89)/2;
        d.drawTexture(TEXTURE,x,y,138,89,0F,0F,138,89,256,256);
        d.drawCenteredTextWithShadow(textRenderer,Integer.toString(busID),(x+69),(y+89-12),0xFFF0F0F0);
        d.getMatrices().push();
        d.getMatrices().scale(0.5F,0.5F,1F);
        d.drawText(textRenderer,"Set Bus ID",(int)((x+4)*2F),(int)((y+4)*2F),0xFFF0F0F0,true);
        d.getMatrices().pop();
        for(int i=0;i<8;i++) {
            int invI = 7-i;
            int switchX = ((width-128)/2)+(invI*16);
            d.drawTexture(TEXTURE,switchX,(height-36)/2,16,36,138F+(i%8<4?0F:8F)+((busID&(1<<i))!=0?16F:0F),0F,8,18,256,256);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int busID = BlockEntity.getBusID();
        for(int i=0;i<8;i++) {
            int invI = 7-i;
            int switchX = ((width-128)/2)+(invI*16);
            if(mouseX >= switchX && mouseX <= switchX+16 && mouseY >= ((height-36)/2) && mouseY <= (((height-36)/2)+36)) {
                if((busID&(1<<i))!=0) {
                    busID &= ~(1<<i);
                } else {
                    busID |= (1<<i);
                }
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeBlockPos(BlockEntity.getPos());
                buf.writeInt(busID);
                ClientPlayNetworking.send(PeripheralPackets.SET_BUS_ID, buf);
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
