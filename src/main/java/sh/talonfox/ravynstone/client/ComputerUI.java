package sh.talonfox.ravynstone.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;
import sh.talonfox.ravynstone.blocks.ComputerBlockEntity;
import sh.talonfox.ravynstone.network.ComputerPackets;

import java.util.Date;
import java.util.List;
import java.util.Random;

public class ComputerUI extends Screen {
    private static final Identifier TEXTURE = new Identifier("ravynstone", "textures/gui/ravyn_computer.png");
    private ComputerBlockEntity BlockEntity;
    private short SwitchInput = 0;
    private Boolean Reset = false;
    private Boolean SingleStep = false;
    private Boolean Advance = false;
    private Boolean Examine = false;
    private Boolean Deposit = false;
    private byte RAMValue = 0;
    public ComputerUI(MutableText title, ComputerBlockEntity blockEntity) {
        super(title);
        BlockEntity = blockEntity;
        RAMValue = BlockEntity.memRead(BlockEntity.CPU.PC);
    }

    public void drawBackground(MatrixStack matrices, float delta) {
        int x = (width - 340) / 2;
        int y = (height - 217) / 2;
        drawTexture(matrices,x,y,340,217,0.0F,0.0F,227,145,256,256);
        drawTexture(matrices,x+340-32, y+64, 16, 36, 64F+(BlockEntity.Powered?8F:0F), 145.0F, 8, 18, 256, 256);
    }
    public void renderAddress(MatrixStack matrices) {
        int x = (width - 256) / 2;
        int ledX = (width - 192) / 2;
        int valLedX = (width - 96) / 2;
        int y = Math.round((height + 217.0F) / 2.0F)-36-16;
        int pc = Short.toUnsignedInt(BlockEntity.CPU.PC);
        int val = Byte.toUnsignedInt(RAMValue);
        for(int i = 0; i < 16; i++) {
            drawTexture(matrices,ledX + ((15 - i) * 12),y-20,12,10,0.0F,163.0F,12,10,256,256);
            if(i < 8)
                drawTexture(matrices,valLedX + ((7 - i) * 12),y-35,12,10,0.0F,163.0F,12,10,256,256);
            if((((pc&(1<<i))!=0)||Reset)&&BlockEntity.Powered)
                drawTexture(matrices,ledX + ((15 - i) * 12) + 2,y-19,8,8,12.0F,163.0F,8,8,256,256);
            if(i < 8)
                if((((val&(1<<i))!=0)||Reset)&&BlockEntity.Powered)
                    drawTexture(matrices,valLedX + ((7 - i) * 12) + 2,y-34,8,8,12.0F,163.0F,8,8,256,256);
            drawTexture(matrices,x + ((15 - i) * 16), y, 16, 36, (((i % 6) < 3)?0.0F:16.0F)+((SwitchInput&(1<<i))!=0?8F:0F), 145.0F, 8, 18, 256, 256);
        }
    }
    public void renderTopSwitches(MatrixStack matrices, float delta) {
        int x = (width - 340) / 2;
        int y = (height - 217) / 2;
        drawTexture(matrices,x+16,y+16,16,36,48F+(!BlockEntity.CPU.Stop?8F:0F),145.0F,8,18,256,256);
        drawTexture(matrices,x+16+(16),y+16,16,36,32F+(SingleStep?8F:0F),145.0F,8,18,256,256);
        drawTexture(matrices,x+16+(16*2),y+16,16,36,Reset?8F:0F,145.0F,8,18,256,256);
        drawTexture(matrices,x+16+(16*3),y+16,16,36,16+(Advance?8F:0F),145.0F,8,18,256,256);
        drawTexture(matrices,x+16+(16*4),y+16,16,36,16+(Examine?8F:0F),145.0F,8,18,256,256);
        drawTexture(matrices,x+16+(16*5),y+16,16,36,16+(Deposit?8F:0F),145.0F,8,18,256,256);
    }
    public void drawTooltip(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        int x = (width - 340) / 2;
        int y = (height - 217) / 2;
        int addrX = (width - 256) / 2;
        int addrY = Math.round((height + 217.0F) / 2.0F) - 36 - 16;
        int ledX = (width - 192) / 2;
        int valLedX = (width - 96) / 2;
        if (mouseX >= addrX && mouseX <= addrX + (16 * 16) && mouseY >= addrY && mouseY <= addrY + 36) {
            renderTooltip(matrices, List.of(Text.of(Integer.toString(Short.toUnsignedInt(SwitchInput))), Text.of("0x" + Integer.toHexString(Short.toUnsignedInt(SwitchInput))), Text.of("0o" + Integer.toOctalString(Short.toUnsignedInt(SwitchInput)))), mouseX, mouseY);
        } else if (mouseX >= ledX && mouseX <= ledX + (16 * 12) && mouseY >= addrY - 20 && mouseY <= addrY - 10 && BlockEntity.Powered) {
            renderTooltip(matrices, List.of(Text.of("Program Counter"), Text.of(Integer.toString(Short.toUnsignedInt(BlockEntity.CPU.PC))), Text.of("0x" + Integer.toHexString(Short.toUnsignedInt(BlockEntity.CPU.PC))), Text.of("0o" + Integer.toOctalString(Short.toUnsignedInt(BlockEntity.CPU.PC)))), mouseX, mouseY);
        } else if (mouseX >= valLedX && mouseX <= valLedX + (8 * 12) && mouseY >= addrY - 35 && mouseY <= addrY - 25 && BlockEntity.Powered) {
            renderTooltip(matrices, List.of(Text.of("Data"), Text.of(Integer.toString(Byte.toUnsignedInt(RAMValue))), Text.of("0x" + Integer.toHexString(Byte.toUnsignedInt(RAMValue))), Text.of("0o" + Integer.toOctalString(Byte.toUnsignedInt(RAMValue)))), mouseX, mouseY);
        } else if (mouseX >= x + 340 - 32 && mouseX <= x + 340 - 16 && mouseY >= y + 64 && mouseY <= y + 64 + 36) {
            renderTooltip(matrices, Text.of("Power"), mouseX, mouseY);
        } else if (mouseX >= x + 16 && mouseX <= x + 16 + 16 && mouseY >= y + 16 && mouseY <= y + 16 + 36) {
            renderTooltip(matrices, Text.of("Run/Stop"), mouseX, mouseY);
        } else if(mouseX >= x+16+16 && mouseX <= x+16+16+16 && mouseY >= y+16 && mouseY <= y+16+36) {
            renderTooltip(matrices, Text.of("Step"),mouseX,mouseY);
        } else if(mouseX >= x+16+(16*2) && mouseX <= x+16+(16*2)+16 && mouseY >= y+16 && mouseY <= y+16+36) {
            renderTooltip(matrices, Text.of("Reset"),mouseX,mouseY);
        } else if(mouseX >= x+16+(16*3) && mouseX <= x+16+(16*3)+16 && mouseY >= y+16 && mouseY <= y+16+36) {
            renderTooltip(matrices, List.of(Text.of("Increment PC after"),Text.of("Deposit/Examine")),mouseX,mouseY);
        } else if(mouseX >= x+16+(16*4) && mouseX <= x+16+(16*4)+16 && mouseY >= y+16 && mouseY <= y+16+36) {
            renderTooltip(matrices, Text.of("Examine"),mouseX,mouseY);
        } else if(mouseX >= x+16+(16*5) && mouseX <= x+16+(16*5)+16 && mouseY >= y+16 && mouseY <= y+16+36) {
            renderTooltip(matrices, Text.of("Deposit"),mouseX,mouseY);
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
        drawBackground(matrices,delta);
        renderAddress(matrices);
        renderTopSwitches(matrices,delta);
        //////////////////////////////////////////////////
        drawTooltip(matrices, delta, mouseX, mouseY);
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
                BlockEntity.CPU.BusOffset = rand.nextInt() & 0xFF;
                BlockEntity.CPU.BusEnabled = rand.nextBoolean();
                BlockEntity.CPU.Error = rand.nextBoolean();
                RAMValue = (byte)rand.nextInt();
                rand.nextBytes(BlockEntity.RAM);
            }
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(BlockEntity.getPos());
            buf.writeNbt(BlockEntity.createNbt());
            ClientPlayNetworking.send(ComputerPackets.COMPUTER_C2S_SYNC_ID,buf);
        } else if(mouseX >= bgrX+16+(16) && mouseX <= bgrX+16+(16)+16 && mouseY >= bgrY+16 && mouseY <= bgrY+16+36) {
            SingleStep = true;
        } else if(mouseX >= bgrX+16+(16*2) && mouseX <= bgrX+16+(16*2)+16 && mouseY >= bgrY+16 && mouseY <= bgrY+16+36) {
            Reset = true;
        } else if(mouseX >= bgrX+16+(16*3) && mouseX <= bgrX+16+(16*3)+16 && mouseY >= bgrY+16 && mouseY <= bgrY+16+36) {
            Advance = !Advance;
        } else if(mouseX >= bgrX+16+(16*4) && mouseX <= bgrX+16+(16*4)+16 && mouseY >= bgrY+16 && mouseY <= bgrY+16+36) {
            Examine = true;
        } else if(mouseX >= bgrX+16+(16*5) && mouseX <= bgrX+16+(16*5)+16 && mouseY >= bgrY+16 && mouseY <= bgrY+16+36) {
            Deposit = true;
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(Reset) {
            if(BlockEntity.Powered) {
                BlockEntity.CPU.reset();
                RAMValue = BlockEntity.memRead(BlockEntity.CPU.PC);
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeBlockPos(BlockEntity.getPos());
                buf.writeNbt(BlockEntity.createNbt());
                ClientPlayNetworking.send(ComputerPackets.COMPUTER_C2S_SYNC_ID, buf);
            }
            Reset = false;
        }
        if(Examine) {
            if(BlockEntity.Powered) {
                if (Advance) {
                    BlockEntity.CPU.PC += 1;
                } else {
                    BlockEntity.CPU.PC = SwitchInput;
                }
                RAMValue = BlockEntity.memRead(BlockEntity.CPU.PC);
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeBlockPos(BlockEntity.getPos());
                buf.writeNbt(BlockEntity.createNbt());
                ClientPlayNetworking.send(ComputerPackets.COMPUTER_C2S_SYNC_ID, buf);
            }
            Examine = false;
        }
        if(Deposit) {
            if(BlockEntity.Powered) {
                BlockEntity.memStore(BlockEntity.CPU.PC, (byte) SwitchInput);
                RAMValue = BlockEntity.memRead(BlockEntity.CPU.PC);
                if (Advance) {
                    BlockEntity.CPU.PC += 1;
                }
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeBlockPos(BlockEntity.getPos());
                buf.writeNbt(BlockEntity.createNbt());
                ClientPlayNetworking.send(ComputerPackets.COMPUTER_C2S_SYNC_ID, buf);
            }
            Deposit = false;
        }
        if(SingleStep) {
            if(BlockEntity.Powered) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeBlockPos(BlockEntity.getPos());
                ClientPlayNetworking.send(ComputerPackets.COMPUTER_STEP_ID, buf);
            }
            SingleStep = false;
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
