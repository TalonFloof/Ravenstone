package sh.talonfox.ravenstone.processor;

import net.minecraft.item.ItemStack;
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
    public int Cause = 0;
    public int EPC = 0;
    public int StallCycles = 0;
    public int Timer = 0;

    public R3000() {}

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
        return 1000000; // 1 MIPS
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
        Cause = 0;
        EPC = 0;
    }

    public void triggerTrap(int curPC, int cause) {
        Ravenstone.LOGGER.warn("Trap (Type 0x{}) @ 0x{}", Integer.toHexString(cause), Integer.toHexString(curPC));
        if(cause != 256) {
            Cause = cause << 2;
        } else {
            Cause = 0;
        }
        if(Branch != -1) {
            Branch = -1;
            Cause |= 0x80000000;
            EPC = curPC-4;
        } else {
            EPC = curPC;
        }
        Status = ((((Status & 63) << 2) & 63) | 0b10) | (Status & 0xffffffc0);
        if(cause != 256) {
            PC = ((Status & (1 << 22)) != 0) ? 0xbfc00180 : 0x80000080;
        } else {
            PC = ((Status & (1 << 22)) != 0) ? 0xbfc00100 : 0x80000000;
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
        Timer = (Timer + 1) % 100;
        Host = host;
        if (StallCycles > 0 && Wait) {
            StallCycles = 0;
        }
        Wait = false;
        if (StallCycles > 0) {
            StallCycles -= 1;
            return;
        }
        var insnPC = PC;
        var insn = Integer.toUnsignedLong(pc4());
        if (insn == 0) { // No Operation
            return;
        }
        var opcode = insn >>> 26;
        if((opcode >>> 2) == 0x04) { // Coprocessor Related Opcodes
            var code = (insn & 0x3E00000) >> 21;
            var rt = (insn & 0x1F0000) >>> 16;
            var rd = (insn & 0xF800) >>> 11;
            var co = (insn >>> 25) & 1;
            var special = insn & 63;

            if(code == 0x10 && special == 0x10) { // RFE
                Status = (Status & 0xFFFFFFC0) | ((Status & 63) >>> 2);
            } else if(code == 0x00) { // MFC0
                var value = 0;
                switch((int)rd) {
                    case 12 -> value = Status;
                    case 13 -> value = Cause;
                    case 14 -> value = EPC;
                }
                setRegister((int)rt, value);
            } else if(code == 0x04) { // MTC0
                var value = getRegister((int)rt);
                switch((int)rd) {
                    case 12 -> Status = value;
                    case 13 -> Cause = value;
                    case 14 -> EPC = value;
                }
            } else {
                triggerTrap(insnPC,10);
            }
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
                        triggerTrap(insnPC,12);
                    }
                }
                case 0b011011 -> { // DIVU
                    var m1 = Integer.toUnsignedLong(getRegister(rs));
                    var m2 = Integer.toUnsignedLong(getRegister(rt));
                    if (m2 == 0L) {
                        triggerTrap(insnPC,12);
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
                    triggerTrap(insnPC,8);
                }
                case 0b100110 -> { // XOR
                    setRegister(rd, getRegister(rs) ^ getRegister(rt));
                }
                default -> {
                    triggerTrap(insnPC,10);
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
                            triggerTrap(insnPC,10);
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
                    setRegister(rt, (peek1(Integer.toUnsignedLong(getRegister(rs) + immed)) << 24) >> 24);
                }
                case 0b100100 -> { // LBU
                    setRegister(rt, peek1(Integer.toUnsignedLong(getRegister(rs) + immed)));
                }
                case 0b100001 -> { // LH
                    setRegister(rt, (peek2(Integer.toUnsignedLong(getRegister(rs) + immed)) << 16) >> 16);
                }
                case 0b100101 -> { // LHU
                    setRegister(rt, peek2(Integer.toUnsignedLong(getRegister(rs) + immed)));
                }
                case 0b001111 -> { // LUI
                    setRegister(rt, immedU << 16);
                }
                case 0b100011 -> { // LW
                    setRegister(rt, peek4(Integer.toUnsignedLong(getRegister(rs) + immed)));
                }
                case 0b100010 -> { // LWL
                    var addr = Integer.toUnsignedLong(getRegister(rs) + immed);
                    var word = peek4(addr & 0xFFFFFFFCL);
                    switch ((int)(addr & 0x3)) {
                        case 0 -> setRegister(rt, ((word & 0x0000_00FF) << 24) | (getRegister(rt) & 0x00FF_FFFF));
                        case 1 -> setRegister(rt, ((word & 0x0000_FFFF) << 16) | (getRegister(rt) & 0x0000_FFFF));
                        case 2 -> setRegister(rt, ((word & 0x00FF_FFFF) << 8) | (getRegister(rt) & 0x0000_00FF));
                        case 3 -> setRegister(rt, word);
                    }
                }
                case 0b100110 -> { // LWR
                    var addr = Integer.toUnsignedLong(getRegister(rs) + immed);
                    var word = peek4(addr & 0xFFFFFFFCL);
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
                    int word = peek4(alignAddr);
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
                    int word = peek4(alignAddr);
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
                    triggerTrap(insnPC,10);
                }
            }
        }
    }

    private int pc4() {
        var val = peek4(Integer.toUnsignedLong(PC));
        if(Branch != -1) {
            PC = Branch;
            Branch = -1;
        } else {
            PC += 4;
        }
        return val;
    }

    private int peek1(long addr) {
        var uaddr = addr & 0xFFFFFFFFL;
        if(uaddr >= 0x80000000L && uaddr <= 0x80ffffffL) {
            return Byte.toUnsignedInt(Host.memRead(uaddr - 0x80000000L));
        } else if(uaddr >= 0xa0000000L && uaddr <= 0xa0ffffffL) {
            return Byte.toUnsignedInt(Host.memRead(uaddr - 0xa0000000L));
        } else if(uaddr == 0xa1000000L) {
            return BusOffset;
        } else if(uaddr == 0xa1000001L) {
            return Host.isPeripheralConnected() ? 0 : 0x80;
        } else if(uaddr == 0xa1000002L) {
            return Timer;
        } else if(uaddr >= 0xa2000000L && uaddr <= 0xa200ffffL) {
            return Byte.toUnsignedInt(Host.busRead((byte) BusOffset, (short)(uaddr - 0xa2000000L)));
        } else if(uaddr >= 0xbfc00000L && uaddr <= 0xbfffffffL) {
            return Byte.toUnsignedInt(ResourceRegister.ROMS.get("r3000")[(int)(uaddr-0xbfc00000L)]);
        } else {
            return 0;
        }
    }
    private int peek2(long addr) {return peek1(addr) | (peek1(addr + 1) << 8);}
    private int peek4(long addr) {return peek2(addr) | (peek2(addr + 2) << 16);}

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
        stack.getOrCreateNbt().putInt("Cause",Cause);
        stack.getOrCreateNbt().putInt("EPC",EPC);
        stack.getOrCreateNbt().putIntArray("Registers",Registers);
        stack.getOrCreateNbt().putInt("BusOffset",BusOffset);
    }

    @Override
    public void loadNBT(ItemStack stack) {
        Wait = stack.getOrCreateNbt().getBoolean("Wait");
        PC = stack.getOrCreateNbt().getInt("PC");
        LOW = stack.getOrCreateNbt().getInt("LOW");
        HIGH = stack.getOrCreateNbt().getInt("HIGH");
        Branch = stack.getOrCreateNbt().getInt("Branch");
        Status = stack.getOrCreateNbt().getInt("Status");
        Cause = stack.getOrCreateNbt().getInt("Cause");
        EPC = stack.getOrCreateNbt().getInt("EPC");
        Registers = stack.getOrCreateNbt().getIntArray("Registers");
        BusOffset = stack.getOrCreateNbt().getInt("BusOffset");
    }

    public class TLBEntry {
        public TLBEntry() {

        }
    }
}
