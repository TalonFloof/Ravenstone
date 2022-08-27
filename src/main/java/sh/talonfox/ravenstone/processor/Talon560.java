package sh.talonfox.ravenstone.processor;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import sh.talonfox.ravenstone.Ravenstone;
import sh.talonfox.ravenstone.ResourceRegister;

public class Talon560 implements Processor {
    public ProcessorHost Host = null;
    public short PC = (short)0xff00;
    public short DSP = 0x100;
    public short RSP = 0x200;
    public short KSP = 0;
    public boolean Wait = false;
    private boolean Is16Bit = false;
    private boolean UsingRStack = false;
    private boolean Retain = false;
    public int BusOffset = 0;

    public Talon560() {}

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
        return 100000; // 100 kIPS
    }

    @Override
    public void reset() {
        DSP = 0x100;
        RSP = 0x200;
        KSP = 0;
        PC = (short)0xff00;
        Wait = false;
        BusOffset = 0;
        Is16Bit = false;
        UsingRStack = false;
        Retain = false;
    }

    @Override
    public void next(ProcessorHost host) {
        Host = host;
        Wait = false;
        var insn = pc1();
        Is16Bit = (insn & 0x80) != 0;
        Retain = (insn & 0x40) != 0;
        UsingRStack = (insn & 0x20) != 0;
        if(RSP < 0x200)
            RSP = 0x200;
        if(DSP < 0x100)
            DSP = 0x100;
        if(RSP > 0x300)
            RSP = 0x300;
        if(DSP > 0x200)
            DSP = 0x200;
        KSP = UsingRStack?Short.valueOf(RSP):Short.valueOf(DSP);
        //Ravenstone.LOGGER.info("0x{}: 0x{}", Integer.toHexString(Short.toUnsignedInt(PC)-1), Integer.toHexString(insn));
        switch (insn & 0x1f) {
            case 0x00 -> { // ldi
                if(Retain) {
                    Host.stop();
                    return;
                }
                if(Is16Bit)
                    push(pc2());
                else
                    push(pc1());
            }
            case 0x01 -> // pop
                    pop();
            case 0x02 -> { // nip
                var top = pop();
                pop();
                push(top);
            }
            case 0x03 -> { // swp
                var a = pop();
                var b = pop();
                push(a);
                push(b);
            }
            case 0x04 -> { // rot
                var a = pop();
                var b = pop();
                var c = pop();
                push(b);
                push(a);
                push(c);
            }
            case 0x05 -> { // dup
                var top = pop();
                push(top);
                push(top);
            }
            case 0x06 -> { // ovr
                var a = pop();
                var b = pop();
                push(b);
                push(a);
                push(b);
            }
            case 0x07 -> { // eq
                var a = pop();
                var b = pop();
                Is16Bit = false;
                push(b==a?1:0);
                Is16Bit = (insn & 0x80) != 0;
            }
            case 0x08 -> { // ne
                var a = pop();
                var b = pop();
                Is16Bit = false;
                push(b!=a?1:0);
                Is16Bit = (insn & 0x80) != 0;
            }
            case 0x09 -> { // gt
                var a = pop();
                var b = pop();
                Is16Bit = false;
                push(b>a?1:0);
                Is16Bit = (insn & 0x80) != 0;
            }
            case 0x0a -> { // lt
                var a = pop();
                var b = pop();
                Is16Bit = false;
                push(b<a?1:0);
                Is16Bit = (insn & 0x80) != 0;
            }
            case 0x0b -> { // jmp
                if(Is16Bit)
                    PC = (short)pop();
                else
                    PC = (short)(Short.toUnsignedInt(PC)+((byte)pop()));
            }
            case 0x0c -> { // jsr
                if(UsingRStack)
                    dpush2(Short.toUnsignedInt(PC));
                else
                    rpush2(Short.toUnsignedInt(PC));
                if(Is16Bit) {
                    var val = pop();
                    PC = (short)val;
                } else
                    PC = (short)(Short.toUnsignedInt(PC)+((byte)pop()));
            }
            case 0x0d -> { // jnz
                var a = pop();
                Is16Bit = false;
                var b = pop();
                Is16Bit = (insn & 0x80) != 0;
                if(b != 0)
                    if(Is16Bit)
                        PC = (short)a;
                    else
                        PC = (short)(Short.toUnsignedInt(PC)+((byte)a));
            }
            case 0x0e -> { // sth
                int a = pop();
                if(UsingRStack)
                    dpush(a);
                else
                    rpush(a);
            }
            case 0x0f -> { // ldz
                Is16Bit = false;
                var a = pop();
                Is16Bit = (insn & 0x80) != 0;
                if(Is16Bit)
                    push(peek2(a));
                else
                    push(peek1(a));
            }
            case 0x10 -> { // stz
                Is16Bit = false;
                var a = pop();
                Is16Bit = (insn & 0x80) != 0;
                var b = pop();
                if(Is16Bit)
                    poke2(a,b);
                else
                    poke1(a,b);
            }
            case 0x11 -> { // ldr
                Is16Bit = false;
                var a = Short.toUnsignedInt(PC)+((byte)pop());
                Is16Bit = (insn & 0x80) != 0;
                if(Is16Bit)
                    push(peek2(a));
                else
                    push(peek1(a));
            }
            case 0x12 -> { // str
                Is16Bit = false;
                var a = Short.toUnsignedInt(PC)+((byte)pop());
                Is16Bit = (insn & 0x80) != 0;
                var b = pop();
                if(Is16Bit)
                    poke2(a,b);
                else
                    poke1(a,b);
            }
            case 0x13 -> { // lda
                Is16Bit = true;
                var a = pop();
                Is16Bit = (insn & 0x80) != 0;
                if(Is16Bit)
                    push(peek2(a));
                else
                    push(peek1(a));
            }
            case 0x14 -> { // sta
                Is16Bit = true;
                var a = pop();
                Is16Bit = (insn & 0x80) != 0;
                var b = pop();
                if(Is16Bit)
                    poke2(a,b);
                else
                    poke1(a,b);
            }
            case 0x15 -> { // add
                var a = pop();
                var b = pop();
                push(b+a);
            }
            case 0x16 -> { // sub
                var a = pop();
                var b = pop();
                push(b-a);
            }
            case 0x17 -> { // mul
                var a = pop();
                var b = pop();
                push(b*a);
            }
            case 0x18 -> { // div
                var a = pop();
                var b = pop();
                if(a == 0) {
                    push(0);
                } else {
                    push(b / a);
                }
            }
            case 0x19 -> { // and
                var a = pop();
                var b = pop();
                push(b & a);
            }
            case 0x1a -> { // ora
                var a = pop();
                var b = pop();
                push(b | a);
            }
            case 0x1b -> { // eor
                var a = pop();
                var b = pop();
                push(b ^ a);
            }
            case 0x1c -> { // sft
                var a = pop();
                var b = pop();
                push(b >>> (a & 0x0f) << ((a & 0xf0) >>> 4));
            }
            case 0x1d -> { // inc
                var a = pop();
                push(a+1);
            }
            case 0x1e -> // wai
                    Wait = true;
            case 0x1f -> { // mmu
                Is16Bit = false;
                BusOffset = pop();
                Is16Bit = (insn & 0x80) != 0;
                Host.invalidatePeripheral();
            }
            default -> {
                Ravenstone.LOGGER.error("Invalid Opcode: 0x{}: 0x{}", Integer.toHexString(Short.toUnsignedInt(PC)-1), Integer.toHexString(insn));
            }
        }
    }

    private int pc1() {PC += 1; return peek1(Short.toUnsignedInt(PC) - 1);}
    private int pc2() {PC += 2; return peek1(Short.toUnsignedInt(PC) - 2) | (peek1(Short.toUnsignedInt(PC) - 1) << 8);}

    private int peek1(int addr) {
        var uaddr = addr & 0xFFFF;
        if(uaddr >= 0x300 && uaddr <= 0x3FF) {
            return Byte.toUnsignedInt(Host.busRead((byte)BusOffset,(byte)(uaddr-0x300)));
        } else if(uaddr >= 0xFF00) {
            return Byte.toUnsignedInt(ResourceRegister.ROMS.get("Talon560")[uaddr-0xFF00]);
        } else {
            return Byte.toUnsignedInt(Host.memRead(uaddr));
        }
    }
    private int peek2(int addr) {return peek1(addr) | (peek1(addr + 1) << 8);}

    private void poke1(int addr, int b) {
        var uaddr = addr & 0xFFFF;
        if(uaddr >= 0x300 && uaddr <= 0x3FF) {
            Host.busWrite((byte)BusOffset,(byte)(uaddr-0x300),(byte)(b & 0xFF));
        } else if(uaddr < 0xFF00) {
            Host.memStore(uaddr, (byte)(b & 0xFF));
        }
    }
    private void poke2(int addr, int s) {poke1(addr, s); poke1(addr + 1, s >>> 8);}

    private void dpush1(int b) {poke1(DSP, b); DSP += 1;}
    private void dpush2(int s) {dpush1(s); dpush1(s >>> 8);}
    private void dpush(int s) {if(Is16Bit) {dpush2(s);} else {dpush1(s);}}
    private int dpop1() {
        if(Retain && !UsingRStack) {
            KSP -= 1;
            return peek1(KSP);
        } else {
            DSP -= 1;
            return peek1(DSP);
        }
    }
    private int dpop2() {return (dpop1() << 8) | dpop1();}
    private int dpop() {if(Is16Bit) {return dpop2();} else {return dpop1();}}
    private void rpush1(int b) {poke1(RSP, b); RSP += 1;}
    private void rpush2(int s) {rpush1(s); rpush1(s >>> 8);}
    private void rpush(int s) {if(Is16Bit) {rpush2(s);} else {rpush1(s);}}
    private int rpop1() {
        if(Retain && UsingRStack) {
            KSP -= 1;
            return peek1(KSP);
        } else {
            RSP -= 1;
            return peek1(RSP);
        }
    }
    private int rpop2() {return (rpop1() << 8) | rpop1();}
    private int rpop() {if(Is16Bit) {return rpop2();} else {return rpop1();}}

    private void push(int s) {if(UsingRStack) {rpush(s);} else {dpush(s);}}
    private int pop() {if(UsingRStack) {return rpop();} else {return dpop();}}

    @Override
    public void saveNBT(ItemStack stack) {
        stack.getOrCreateNbt().putBoolean("Wait",Wait);
        stack.getOrCreateNbt().putShort("DSP",DSP);
        stack.getOrCreateNbt().putShort("RSP",RSP);
        stack.getOrCreateNbt().putShort("PC",PC);
        stack.getOrCreateNbt().putInt("BusOffset",BusOffset);
    }

    @Override
    public void loadNBT(ItemStack stack) {
        Wait = stack.getOrCreateNbt().getBoolean("Wait");
        DSP = stack.getOrCreateNbt().getShort("DSP");
        RSP = stack.getOrCreateNbt().getShort("RSP");
        PC = stack.getOrCreateNbt().getShort("PC");
        BusOffset = stack.getOrCreateNbt().getInt("BusOffset");
    }
}
