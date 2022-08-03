package sh.talonfox.ravynstone.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import sh.talonfox.ravynstone.Ravynstone;
import sh.talonfox.ravynstone.processor.Processor;
import sh.talonfox.ravynstone.processor.ProcessorHost;

import java.util.Objects;

public class ComputerBlockEntity extends BlockEntity implements ProcessorHost {
    public Processor CPU = new Processor(this);
    public byte[] RAM = new byte[16384];
    public Boolean Powered = false;

    public ComputerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockRegister.RAVYN_COMPUTER_ENTITY, pos, state);
    }

    @Override
    public void resetBusState() {

    }

    @Override
    public byte memRead(short at) {
        if(Short.toUnsignedInt(at) < RAM.length) {
            return RAM[Short.toUnsignedInt(at)];
        } else {
            CPU.Error = true;
            return (byte)0xFF;
        }
    }

    @Override
    public void memStore(short at, byte data) {
        if(Short.toUnsignedInt(at) < RAM.length) {
            RAM[Short.toUnsignedInt(at)] = data;
        }
    }
    public static void tick(World world, BlockPos pos, BlockState state, ComputerBlockEntity blockEntity) {
        if(blockEntity.Powered&&!blockEntity.CPU.Stop) {

        }
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        var processor = new NbtCompound();
        processor.putShort("A",CPU.A);
        processor.putShort("B",CPU.B);
        processor.putShort("D",CPU.D);
        processor.putShort("I",CPU.I);
        processor.putShort("X",CPU.X);
        processor.putShort("Y",CPU.Y);
        processor.putShort("PC",CPU.PC);
        processor.putShort("SP",CPU.SP);
        processor.putShort("RP",CPU.RP);
        processor.putShort("BrkAddr",CPU.BrkAddr);
        processor.putShort("ResetAddr",CPU.ResetAddr);
        processor.putInt("BusOffset",CPU.BusOffset);
        processor.putBoolean("Error",CPU.Error);
        processor.putBoolean("Stop",CPU.Stop);
        processor.putBoolean("Wait",CPU.Wait);
        processor.putBoolean("BusEnabled",CPU.BusEnabled);
        processor.putBoolean("FlagC",CPU.FlagC);
        processor.putBoolean("FlagZ",CPU.FlagZ);
        processor.putBoolean("FlagI",CPU.FlagI);
        processor.putBoolean("FlagD",CPU.FlagD);
        processor.putBoolean("FlagX",CPU.FlagX);
        processor.putBoolean("FlagM",CPU.FlagM);
        processor.putBoolean("FlagV",CPU.FlagV);
        processor.putBoolean("FlagN",CPU.FlagN);
        processor.putBoolean("FlagE",CPU.FlagE);

        tag.put("Processor",processor);
        tag.putByteArray("RAM", RAM);
        tag.putBoolean("Powered",Powered);

        super.writeNbt(tag);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        var processor = tag.getCompound("Processor");
        CPU.A = processor.getShort("A");
        CPU.B = processor.getShort("B");
        CPU.D = processor.getShort("D");
        CPU.I = processor.getShort("I");
        CPU.X = processor.getShort("X");
        CPU.Y = processor.getShort("Y");
        CPU.PC = processor.getShort("PC");
        CPU.SP = processor.getShort("SP");
        CPU.RP = processor.getShort("RP");
        CPU.BrkAddr = processor.getShort("BrkAddr");
        CPU.ResetAddr = processor.getShort("ResetAddr");
        CPU.BusOffset = processor.getInt("BusOffset");
        CPU.Error = processor.getBoolean("Error");
        CPU.Stop = processor.getBoolean("Stop");
        CPU.Wait = processor.getBoolean("Wait");
        CPU.BusEnabled = processor.getBoolean("BusEnabled");
        CPU.FlagC = processor.getBoolean("FlagC");
        CPU.FlagZ = processor.getBoolean("FlagZ");
        CPU.FlagI = processor.getBoolean("FlagI");
        CPU.FlagD = processor.getBoolean("FlagD");
        CPU.FlagX = processor.getBoolean("FlagX");
        CPU.FlagM = processor.getBoolean("FlagM");
        CPU.FlagV = processor.getBoolean("FlagV");
        CPU.FlagN = processor.getBoolean("FlagN");
        CPU.FlagE = processor.getBoolean("FlagE");
        Powered = tag.getBoolean("Powered");
        if(tag.getByteArray("RAM").length == RAM.length) {
            RAM = tag.getByteArray("RAM");
        }
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }
}