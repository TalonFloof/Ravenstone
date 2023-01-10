package sh.talonfox.ravenstone.processor;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.random.Random;

import java.util.Arrays;

/*
Physical Memory Map:
0x0000_0000-0x7fff_ffff: User Space Virtual Memory (Nothing is mapped here)
0x8000_0000-0x80ff_ffff: Physical Memory (w/ I and D cache used)
0x8100_0000: Mirror of the Peripheral Bus Offset (Do not use!)
0x8100_0000: Mirror of the Peripheral Bus Offset (Do not use!)
0x9fc0_0000: Mirror of ROM
0xa000_0000-0xa0ff_ffff: Physical Memory (No Cache Used)
0xa100_0000: Peripheral Bus Offset
0xa200_0000-0xa200_ffff: Memory Mapped Peripheral Area
0xbfc0_0000: Beginning of ROM (MIPS Processor Reset Vector is Here)
0xc000_0000-0xffff_ffff: Kernel Virtual Memory (Nothing is mapped here)
*/

public class R3000 implements Processor {
    public ProcessorHost Host = null;
    public boolean Wait = false;
    public int[] Registers = new int[32];
    public int PC = 0;
    public int LOW = 0;
    public int HIGH = 0;
    public int Branch = -1; // Used to emulate MIPS delayed branching
    public int BusOffset = 0;
    public int Status = 0;
    public int StallCycles = 0;

    public CacheLine[] ICache = new CacheLine[256];
    public CacheLine[] DCache = new CacheLine[256];

    public R3000() {
        for(int i=0; i < 256; i++) {
            ICache[i] = new CacheLine(true);
            DCache[i] = new CacheLine(false);
        }
    }

    @Override
    public boolean isWaiting() {
        return Wait;
    }

    @Override
    public void setWait(boolean flag) {
        Wait = flag;
    }

    @Override
    public int insnPerSecond() {
        return 1000000; // 1 MHz
    }

    @Override
    public void reset() {
        Wait = false;
        for(int i=0; i < 32; i++)
            Registers[i] = 0;
        PC = 0xbfc00000;
        StallCycles = 0;
        BusOffset = 0;
        LOW = 0;
        HIGH = 0;
        Branch = -1;
        Status = 0x2; // Kernel Mode
        for(int i=0; i < 256; i++) {
            ICache[i] = new CacheLine(true);
            DCache[i] = new CacheLine(false);
        }
    }

    @Override
    public void next(ProcessorHost host) {
        Host = host;
        if (StallCycles > 0 && Wait) {
            StallCycles = 0;
        }
        Wait = false;
        if (StallCycles > 0) {
            StallCycles -= 1;
            return;
        }
        var insn = Integer.toUnsignedLong(pc4());
        if (insn == 0) { // No Operation
            return;
        }
        var opcode = insn >>> 26;
        if(insn == 0xc || opcode == 0x10) { // Exception Related Opcodes

        } else if((opcode >>> 2) == 0x04) { // Coprocessor Related Opcodes

        } else if(opcode == 0) { // R-Type
            var func = insn & 0b111111;
            var shamt = (insn >>> 6) & 0b11111;
            var rd = (insn >>> 11) & 0b11111;
            var rt = (insn >>> 16) & 0b11111;
            var rs = (insn >>> 21) & 0b11111;
            switch((int)func) {
                case 0b100000 -> { // ADD
                }
                case 0b100001 -> { // ADDU
                }
                case 0b100100 -> { // AND
                }
                case 0b001101 -> { // BREAK
                }
                case 0b011010 -> { // DIV
                }
                case 0b011011 -> { // DIVU
                }
                case 0b001001 -> { // JALR
                }
                case 0b001000 -> { // JR
                }
                case 0b010000 -> { // MFHI
                }
                case 0b010010 -> { // MFLO
                }
                case 0b010001 -> { // MTHI
                }
                case 0b010011 -> { // MTLO
                }
                case 0b011000 -> { // MULT
                }
                case 0b011001 -> { // MULTU
                }
                case 0b100111 -> { // NOR
                }
                case 0b100101 -> { // OR

                }
                case 0b000000 -> { // SLL

                }
                case 0b000100 -> { // SLLV

                }
                case 0b101010 -> { // SLT

                }
                case 0b101011 -> { // SLTU

                }
                case 0b000011 -> { // SRA

                }
                case 0b000111 -> { // SRAV

                }
                case 0b000010 -> { // SRL

                }
                case 0b000110 -> { // SRLV

                }
                case 0b100010 -> { // SUB

                }
                case 0b100011 -> { // SUBU

                }
                case 0b001100 -> { // SYSCALL

                }
                case 0b100110 -> { // XOR

                }
                default -> {
                }
            }
        } else if(opcode == 0x2 || opcode == 0x3) { // J-Type

        } else { // I-Type

        }
        /*case 0x1f -> { // mmu
            Is16Bit = false;
            BusOffset = pop();
            Is16Bit = (insn & 0x80) != 0;
            Host.invalidatePeripheral();
        }*/
    }

    private int pc4() {
        PC += 4;
        return peek4(Integer.toUnsignedLong(PC-4),true);
    }

    private int peekICache(long addr) {
        return 0;
    }
    private int peekDCache(long addr) {
        return 0;
    }

    private int peek1(long addr, boolean useICache) {
        var uaddr = addr & 0xFFFFFFFFL;
        if(uaddr >= 0x80000000L && uaddr <= 0x80ffffffL) {
            if(useICache) {
                return peekICache(uaddr & 0x7fffffff);
            } else {
                return peekDCache(uaddr & 0x7fffffff);
            }
        } else if(uaddr >= 0xa0000000L && uaddr <= 0xa0ffffffL) {
            StallCycles = 1;
            return Byte.toUnsignedInt(Host.memRead(uaddr-0xa0000000L));
        } else {
            return 0;
        }
        /*if(uaddr >= 0x300 && uaddr <= 0x3FF) {
            return Byte.toUnsignedInt(Host.busRead((byte)BusOffset,(byte)(uaddr-0x300)));
        } else if(uaddr >= 0xFF00) {
            return Byte.toUnsignedInt(ResourceRegister.ROMS.get("R3000")[(int)uaddr-0xFF00]);
        } else {
            return Byte.toUnsignedInt(Host.memRead(uaddr));
        }*/
    }
    private int peek2(long addr, boolean useICache) {return peek1(addr, useICache) | (peek1(addr + 1, useICache) << 8);}
    private int peek4(long addr, boolean useICache) {return peek2(addr, useICache) | (peek2(addr + 2, useICache) << 16);}

    private void poke1(long addr, int b) {
        var uaddr = addr & 0xFFFFFFFFL;
        if(uaddr >= 0x80000000L && uaddr <= 0x80ffffffL) {
            Host.memStore(uaddr - 0x80000000L, (byte)(b & 0xFF));
        } else if(uaddr >= 0xa0000000L && uaddr <= 0xa0ffffffL) {
            Host.memStore(uaddr - 0xa0000000L, (byte)(b & 0xFF));
            StallCycles = 1;
        }
    }
    private void poke2(long addr, int s) {poke1(addr, s); poke1(addr + 1, s >>> 8);}
    private void poke4(long addr, int s) {poke2(addr, s); poke2(addr + 2, s >>> 16);}

    @Override
    public void saveNBT(ItemStack stack) {
        stack.getOrCreateNbt().putBoolean("Wait",Wait);
        stack.getOrCreateNbt().putInt("PC",PC);
        stack.getOrCreateNbt().putInt("LOW",LOW);
        stack.getOrCreateNbt().putInt("HIGH",HIGH);
        stack.getOrCreateNbt().putInt("Branch",Branch);
        stack.getOrCreateNbt().putInt("Status",Status);
        stack.getOrCreateNbt().putIntArray("Registers",Registers);
        stack.getOrCreateNbt().putInt("BusOffset",BusOffset);
        NbtList icacheList = new NbtList();
        NbtList dcacheList = new NbtList();
        Arrays.stream(ICache).forEach((val) -> icacheList.add(val.serialize()));
        Arrays.stream(DCache).forEach((val) -> dcacheList.add(val.serialize()));
        stack.getOrCreateNbt().put("ICache",icacheList);
        stack.getOrCreateNbt().put("DCache",dcacheList);
    }

    @Override
    public void loadNBT(ItemStack stack) {
        Wait = stack.getOrCreateNbt().getBoolean("Wait");
        PC = stack.getOrCreateNbt().getInt("PC");
        LOW = stack.getOrCreateNbt().getInt("LOW");
        HIGH = stack.getOrCreateNbt().getInt("HIGH");
        Branch = stack.getOrCreateNbt().getInt("Branch");
        Status = stack.getOrCreateNbt().getInt("Status");
        Registers = stack.getOrCreateNbt().getIntArray("Registers");
        BusOffset = stack.getOrCreateNbt().getInt("BusOffset");
        NbtList icacheList = stack.getOrCreateNbt().getList("ICache",NbtList.COMPOUND_TYPE);
        NbtList dcacheList = stack.getOrCreateNbt().getList("DCache",NbtList.COMPOUND_TYPE);
        for(int i=0; i < 256; i++) {
            ICache[i].deserialize(icacheList.getCompound(i));
            DCache[i].deserialize(dcacheList.getCompound(i));
        }
    }

    private class CacheLine {
        public int TagValid;
        public byte[] Line;

        public CacheLine(boolean icache) {
            var rng = Random.createLocal();
            if(icache) {
                Line = new byte[16];
            } else {
                Line = new byte[4];
            }
            for(int i=0; i < Line.length; i++) {
                Line[i] = (byte)(rng.nextInt() % 256);
            }
            TagValid = rng.nextInt();
        }

        public int getTag() {
            return (this.TagValid & 0xfffff000);
        }
        public int getValidIndex() {
            return (this.TagValid >> 2) & 0x7;
        }

        public void setTagValid(int val) {
            this.TagValid = val & 0x7fff_f00c;
        }

        public NbtCompound serialize() {
            NbtCompound nbt = new NbtCompound();
            nbt.putInt("Tag",this.TagValid);
            nbt.putByteArray("Line",Line);
            return nbt;
        }

        public void deserialize(NbtCompound nbt) {
            this.TagValid = nbt.getInt("Tag");
            this.Line = nbt.getByteArray("Line");
        }
    }
}
