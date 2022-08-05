package sh.talonfox.ravenstone.processor;

import sh.talonfox.ravenstone.Ravenstone;

public class Processor {
    public ProcessorHost Host;
    public byte A = 0;
    public byte X = 0;
    public byte Y = 0;
    public short PC = 0x0;
    public short SP = 0x1ff;
    public Boolean FlagC = false;
    public Boolean FlagZ = false;
    public Boolean FlagI = false;
    public Boolean FlagD = false;
    public Boolean FlagV = false;
    public Boolean FlagN = false;
    public short ResetAddr = 0x0400;
    public short BrkAddr = 0x2000;
    public Boolean Wait = false;
    public Boolean Stop = true;
    public int BusOffset = 0;
    public Boolean BusEnabled = false;
    public Boolean Error = false;
    private final InstImpl Impl = new InstImpl();

    public Processor(ProcessorHost host) {
        Host = host;
    }

    public void reset() {
        A = 0;
        X = 0;
        Y = 0;
        FlagC = false;
        FlagZ = false;
        FlagI = false;
        FlagD = false;
        FlagV = false;
        FlagN = false;
        SP = 0x1ff;
        PC = 0x400;
        ResetAddr = 0x400;
        BrkAddr = 0x2000;
        Wait = false;
        Error = false;
        BusEnabled = false;
        BusOffset = 0;
    }
    public void next() {
        Stop = false;
        Wait = false;
        var insn = pc1();
        switch(insn) {
            default:
                Error = true;
                break;
        }
    }

    private int pc1() {PC += 1; return peek1(Short.toUnsignedInt(PC) - 1);}
    private int pc2() {return pc1() | (pc1() << 8);}
    private int pc2X() {return pc2() + X;}
    private int pc2Y() {return pc2() + Y;}
    private int pc1S() {return pc1() + SP;}

    private int peek1(int addr) {
        var uaddr = addr & 0xFFFF;
        if(BusEnabled && uaddr >= BusOffset && uaddr <= BusOffset+0xFF) {
            Ravenstone.LOGGER.warn("Bus Read is currently unimplemented");
            return 0;
        } else {
            return Byte.toUnsignedInt(Host.memRead((short)uaddr));
        }
    }
    private int peek2(int addr) {return peek1(addr) | (peek1(addr + 1) << 8);}

    private void poke1(int addr, int b) {
        var uaddr = addr & 0xFFFF;
        if(BusEnabled && uaddr >= BusOffset && uaddr <= BusOffset+0xFF) {
            Ravenstone.LOGGER.warn("Bus Write is currently unimplemented");
        } else {
            Host.memStore((short)uaddr, (byte)(b & 0xFF));
        }
    }
    private void poke2(int addr, int s) {poke1(addr, s); poke1(addr + 1, s >>> 8);}

    private void push1(int b) {poke1(SP, b); SP -= 1;}
    private void push2(int s) {push1(s >> 8); push1(s);}
    private int pop1() {SP += 1; return peek1(SP);}
    private int pop2() {return pop1() | (pop1() << 8);}

    private int toBCD(int s) {return Short.toUnsignedInt((short)Integer.parseInt(Integer.toString(s),16));}
    private int fromBCD(int s) {
        try {
            return Short.toUnsignedInt((short)Integer.parseInt(Integer.toString(s,16)));
        } catch(NumberFormatException e) {
            Error = true;
            return 0;
        }
    }

    private class InstImpl { }
}
