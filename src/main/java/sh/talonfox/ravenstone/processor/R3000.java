package sh.talonfox.ravenstone.processor;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.random.Random;
import sh.talonfox.ravenstone.Ravenstone;
import sh.talonfox.ravenstone.ResourceRegister;

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

    private void setRegister(int id, int val) {
        if(id == 0) // Register 0 is hardwired to be set to zero, you can't change it.
            return;
        Registers[id] = val;
    }

    private int getRegister(int id) {
        if(id == 0)
            return 0;
        return Registers[id];
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
            var rd = (int)((insn >>> 11) & 0b11111);
            var rt = (int)((insn >>> 16) & 0b11111);
            var rs = (int)((insn >>> 21) & 0b11111);
            switch((int)func) {
                case 0b100000, 0b100001 -> { // ADD, ADDU
                    setRegister(rd, getRegister(rt) + getRegister(rs));
                }
                case 0b100100 -> { // AND
                    setRegister(rd, getRegister(rs) & getRegister(rt));
                }
                case 0b001101 -> { // BREAK
                    // This acts as wait on this CPU
                    setWait(true);
                }
                case 0b011010 -> { // DIV
                    if (getRegister(rt) != 0) {
                        LOW = getRegister(rs) / getRegister(rt);
                        HIGH = getRegister(rs) % getRegister(rt);
                    } else {
                        // Arithmetic Exception
                    }
                }
                case 0b011011 -> { // DIVU
                    var m1 = Integer.toUnsignedLong(getRegister(rs));
                    var m2 = Integer.toUnsignedLong(getRegister(rt));
                    if (m2 == 0L) {
                        // Arithmetic Exception
                    } else {
                        LOW = (int)(m1 / m2);
                        HIGH = (int)(m1 % m2);
                    }
                }
                case 0b001001 -> { // JALR
                    if (rt == 0) {
                        setRegister(31, PC);
                    } else {
                        setRegister(rt, PC);
                    }
                    Branch = getRegister(rs);
                }
                case 0b001000 -> { // JR
                    Branch = getRegister(rs);
                }
                case 0b010000 -> { // MFHI
                    setRegister(rd, HIGH);
                }
                case 0b010010 -> { // MFLO
                    setRegister(rd, LOW);
                }
                case 0b010001 -> { // MTHI
                    HIGH = getRegister(rd);
                }
                case 0b010011 -> { // MTLO
                    LOW = getRegister(rd);
                }
                case 0b011000 -> { // MULT
                    var m1 = (long)getRegister(rs);
                    var m2 = (long)getRegister(rt);
                    var mt = m1 * m2;
                    LOW = (int)mt;
                    HIGH = (int)(mt >> 32);
                }
                case 0b011001 -> { // MULTU
                    var m1 = Integer.toUnsignedLong(getRegister(rs));
                    var m2 = Integer.toUnsignedLong(getRegister(rt));
                    var mt = m1 * m2;
                    LOW = (int)mt;
                    HIGH = (int)(mt >> 32);
                }
                case 0b100111 -> { // NOR
                    setRegister(rd, ~(getRegister(rs) | getRegister(rt)));
                }
                case 0b100101 -> { // OR
                    setRegister(rd, getRegister(rs) | getRegister(rt));
                }
                case 0b000000 -> { // SLL
                    setRegister(rd, getRegister(rt) << shamt);
                }
                case 0b000100 -> { // SLLV
                    setRegister(rd, getRegister(rt) << getRegister(rs));
                }
                case 0b101010 -> { // SLT
                    setRegister(rd, (getRegister(rs) < getRegister(rt))?1:0);
                }
                case 0b101011 -> { // SLTU
                    var m1 = Integer.toUnsignedLong(getRegister(rs));
                    var m2 = Integer.toUnsignedLong(getRegister(rt));
                    setRegister(rd, (m1 < m2)?1:0);
                }
                case 0b000011 -> { // SRA
                    setRegister(rd, getRegister(rt) >> shamt);
                }
                case 0b000111 -> { // SRAV
                    setRegister(rd, getRegister(rt) >> getRegister(rs));
                }
                case 0b000010 -> { // SRL
                    setRegister(rd, getRegister(rt) >>> shamt);
                }
                case 0b000110 -> { // SRLV
                    setRegister(rd, getRegister(rt) >>> getRegister(rs));
                }
                case 0b100010, 0b100011 -> { // SUB, SUBU
                    setRegister(rd, getRegister(rs) - getRegister(rt));
                }
                case 0b001100 -> { // SYSCALL
                    // Not Implemented Yet
                }
                case 0b100110 -> { // XOR
                    setRegister(rd, getRegister(rs) ^ getRegister(rt));
                }
                default -> {
                    Host.stop();
                }
            }
        } else if(opcode == 0x2 || opcode == 0x3) { // J-Type
            if (opcode == 0x3)
                setRegister(31,PC+4);
            Branch = PC;
            Branch &= ((int)(0xF0000000L));
            Branch |= (((int)(insn & 0x3FFFFFF)) << 2);
        } else { // I-Type
            int rs = (int)((insn >>> 21) & 31);
            int rt = (int)((insn >>> 16) & 31);
            //long rd = (insn >>> 11) & 31;
            short immed = ((short)(insn & 0xFFFF));
            int immedU = (int)(insn & 0xFFFF);

            switch((int)opcode) {
                case 0b001000, 0b001001 -> { // ADDI, ADDIU
                    setRegister(rt, getRegister(rs) + immed);
                }
                case 0b001100 -> { // ANDI
                    setRegister(rt, getRegister(rs) & immedU);
                }
                case 0b000100 -> { // BEQ
                    if(getRegister(rs) == getRegister(rt))
                        Branch = PC + (immed << 2);
                }
                case 0b000001 -> { // REGIMM
                    switch(rt) {
                        case 0b00001 -> { // BGEZ
                            if(getRegister(rs) >= 0)
                                Branch = PC + (immed << 2);
                        }
                        case 0b10001 -> { // BGEZAL
                            setRegister(31,PC+4);
                            if(getRegister(rs) >= 0)
                                Branch = PC + (immed << 2);
                        }
                        case 0b00000 -> { // BLTZ
                            if(getRegister(rs) < 0)
                                Branch = PC + (immed << 2);
                        }
                        case 0b10000 -> { // BLTZAL
                            setRegister(31,PC+4);
                            if(getRegister(rs) < 0)
                                Branch = PC + (immed << 2);
                        }
                        default -> {

                        }
                    }
                }
                case 0b000111 -> { // BGTZ
                    if(getRegister(rs) > 0)
                        Branch = PC + (immed << 2);
                }
                case 0b000110 -> { // BLEZ
                    if(getRegister(rs) <= 0)
                        Branch = PC + (immed << 2);
                }
                case 0b000101 -> { // BNE
                    if(getRegister(rs) != getRegister(rt))
                        Branch = PC + (immed << 2);
                }
                case 0b100000 -> { // LB
                    setRegister(rt, (peek1(Integer.toUnsignedLong(getRegister(rs) + immed),false) << 24) >> 24);
                }
                case 0b100100 -> { // LBU
                    setRegister(rt, peek1(Integer.toUnsignedLong(getRegister(rs) + immed),false));
                }
                case 0b100001 -> { // LH
                    setRegister(rt, (peek2(Integer.toUnsignedLong(getRegister(rs) + immed),false) << 16) >> 16);
                }
                case 0b100101 -> { // LHU
                    setRegister(rt, peek2(Integer.toUnsignedLong(getRegister(rs) + immed),false));
                }
                case 0b001111 -> { // LUI
                    setRegister(rt, immedU << 16);
                }
                case 0b100011 -> { // LW
                    setRegister(rt, peek4(Integer.toUnsignedLong(getRegister(rs) + immed),false));
                }
                case 0b100010 -> { // LWL
                    var addr = Integer.toUnsignedLong(getRegister(rs) + immed);
                    var word = peek4(addr & 0xFFFFFFFCL,false);
                    switch ((int)(addr & 0x3)) {
                        case 0 -> setRegister(rt, ((word & 0x0000_00FF) << 24) | (getRegister(rt) & 0x00FF_FFFF));
                        case 1 -> setRegister(rt, ((word & 0x0000_FFFF) << 16) | (getRegister(rt) & 0x0000_FFFF));
                        case 2 -> setRegister(rt, ((word & 0x00FF_FFFF) << 8) | (getRegister(rt) & 0x0000_00FF));
                        case 3 -> setRegister(rt, word);
                    }
                }
                case 0b100110 -> { // LWR
                    var addr = Integer.toUnsignedLong(getRegister(rs) + immed);
                    var word = peek4(addr & 0xFFFFFFFCL,false);
                    switch ((int)(addr & 0x3)) {
                        case 0 -> setRegister(rt, word);
                        case 1 -> setRegister(rt, (word >>> 8) | (getRegister(rt) & 0xFF000000));
                        case 2 -> setRegister(rt, (word >>> 16) | (getRegister(rt) & 0xFFFF0000));
                        case 3 -> setRegister(rt, (word >>> 24) | (getRegister(rt) & 0xFFFFFF00));
                    }
                }
                case 0b001101 -> { // ORI
                    setRegister(rt, getRegister(rs) | immedU);
                }
                case 0b101000 -> { // SB
                    poke1(Integer.toUnsignedLong(getRegister(rs) + immed), (getRegister(rt) & 0xFF));
                }
                case 0b101001 -> { // SH
                    poke2(Integer.toUnsignedLong(getRegister(rs) + immed), (getRegister(rt) & 0xFFFF));
                }
                case 0b001010 -> { // SLTI
                    setRegister(rt, (getRegister(rs) < immed)?1:0);
                }
                case 0b001011 -> { // SLTIU
                    var m1 = Integer.toUnsignedLong(getRegister(rs));
                    var m2 = Integer.toUnsignedLong(immedU);
                    setRegister(rt, (m1 < m2)?1:0);
                }
                case 0b101011 -> { // SW
                    poke4(Integer.toUnsignedLong(getRegister(rs) + immed), getRegister(rt));
                }
                case 0b101010 -> { // SWL
                    long addr = Integer.toUnsignedLong(getRegister(rs) + immed);
                    long alignAddr = addr & 0xFFFFFFFCL;
                    int word = peek4(alignAddr,false);
                    switch((int)(addr & 0x3)) {
                        case 0 -> poke4(alignAddr, (getRegister(rt) >>> 24) | (word & 0xFFFFFF00));
                        case 1 -> poke4(alignAddr, (getRegister(rt) >>> 16) | (word & 0xFFFF0000));
                        case 2 -> poke4(alignAddr, (getRegister(rt) >>> 8) | (word & 0xFF000000));
                        case 3 -> poke4(alignAddr, getRegister(rt));
                    }
                }
                case 0b101110 -> { // SWR
                    long addr = Integer.toUnsignedLong(getRegister(rs) + immed);
                    long alignAddr = addr & 0xFFFFFFFCL;
                    int word = peek4(alignAddr,false);
                    switch((int)(addr & 0x3)) {
                        case 0 -> poke4(alignAddr, getRegister(rt));
                        case 1 -> poke4(alignAddr, (getRegister(rt) >>> 8) | (word & 0x0000_00FF));
                        case 2 -> poke4(alignAddr, (getRegister(rt) >>> 16) | (word & 0x0000_FFFF));
                        case 3 -> poke4(alignAddr, (getRegister(rt) >>> 24) | (word & 0x00FF_FFFF));
                    }
                }
                case 0b001110 -> { // XORI
                    setRegister(rt, getRegister(rs) ^ immedU);
                }
                default -> {
                    Host.stop();
                }
            }
        }
    }

    private int pc4() {
        var val = peek4(Integer.toUnsignedLong(PC),true);
        if(Branch != -1) {
            PC = Branch;
            Branch = -1;
        } else {
            PC += 4;
        }
        return val;
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
            /*if(useICache) {
                return peekICache(uaddr & 0x7fffffff);
            } else {
                return peekDCache(uaddr & 0x7fffffff);
            }*/
            return Byte.toUnsignedInt(Host.memRead(uaddr - 0x80000000L));
        } else if(uaddr >= 0xa0000000L && uaddr <= 0xa0ffffffL) {
            //StallCycles = 1;
            return Byte.toUnsignedInt(Host.memRead(uaddr - 0xa0000000L));
        } else if(uaddr == 0xa1000000L) {
            return BusOffset;
        } else if(uaddr >= 0xa2000000L && uaddr <= 0xa200ffffL) {
            return Byte.toUnsignedInt(Host.busRead((byte) BusOffset, (short)(uaddr - 0xa2000000L)));
        } else if(uaddr >= 0xbfc00000L && uaddr <= 0xbfffffffL) {
            return Byte.toUnsignedInt(ResourceRegister.ROMS.get("r3000")[(int)(uaddr-0xbfc00000L)]);
        } else {
            return 0;
        }
    }
    private int peek2(long addr, boolean useICache) {return peek1(addr, useICache) | (peek1(addr + 1, useICache) << 8);}
    private int peek4(long addr, boolean useICache) {return peek2(addr, useICache) | (peek2(addr + 2, useICache) << 16);}

    private void poke1(long addr, int b) {
        var uaddr = addr & 0xFFFFFFFFL;
        if(uaddr >= 0x80000000L && uaddr <= 0x80ffffffL) {
            Host.memStore(uaddr - 0x80000000L, (byte)(b & 0xFF));
        } else if(uaddr >= 0xa0000000L && uaddr <= 0xa0ffffffL) {
            Host.memStore(uaddr - 0xa0000000L, (byte)(b & 0xFF));
            //StallCycles = 1;
        } else if(uaddr == 0xa1000000L) { // Peripheral Bus ID Register
            BusOffset = b;
            Host.invalidatePeripheral();
        } else if(uaddr == 0xa1000004L) { // Beep Register
            Host.beep();
        } else if(uaddr >= 0xa2000000L && uaddr <= 0xa200ffffL) {
            Host.busWrite((byte)BusOffset,(short)(uaddr-0xa2000000L),(byte)(b & 0xFF));
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
