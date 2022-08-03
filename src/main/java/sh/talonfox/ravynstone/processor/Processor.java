package sh.talonfox.ravynstone.processor;

import sh.talonfox.ravynstone.Ravynstone;

// The source code for the processor is adapted from 2xsaiko's 65EL02 implementation
// The original implementation can be found here: https://github.com/2xsaiko/RetroComputers-XC8010/blob/1.11/src/main/scala/com/github/mrebhan/retrocomputers/xc8010/Processor.scala

public class Processor {
    public ProcessorHost Host;
    public short A = 0;
    public short B = 0;
    public short D = 0;
    public short I = 0;
    public short X = 0;
    public short Y = 0;
    public short PC = 0x0;
    public short SP = 0x1ff;
    public short RP = 0x2ff;
    public Boolean FlagC = false;
    public Boolean FlagZ = false;
    public Boolean FlagI = false;
    public Boolean FlagD = false;
    public Boolean FlagX = true;
    public Boolean FlagM = true;
    public Boolean FlagV = false;
    public Boolean FlagN = false;
    public Boolean FlagE = true;
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
        B = 0;
        X = 0;
        Y = 0;
        I = 0;
        D = 0;
        FlagC = false;
        FlagZ = false;
        FlagI = false;
        FlagD = false;
        FlagX = true;
        FlagM = true;
        FlagV = false;
        FlagN = false;
        FlagE = true;
        SP = 0x1ff;
        RP = 0x2ff;
        //PC = 0x400;
        PC = 0;
        ResetAddr = 0x400;
        BrkAddr = 0x2000;
        Stop = true;
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
            case 0x00: // brk
                push2(Short.toUnsignedInt(PC));
                push1(packFlags());
                PC = BrkAddr;
                break;
            case 0x01: // ora (ind, x)
                Impl.ora(pc2IX());
                break;
            case 0x02: // nxt
                PC = (short)peek2(Short.toUnsignedInt(I));
                I += 2;
                break;
            case 0x03: // ora r, S
                Impl.ora(peekM(pc1S()));
                break;
            case 0x04: // tsb zp
                Impl.tsb(peekM(pc1()));
                break;
            case 0x05: // ora zp
                Impl.ora(peekM(pc1()));
                break;
            case 0x06: // *asl zp
                Impl.asl(peekM(pc1()));
                break;
            case 0x08: // php
                push1(packFlags());
                break;
            case 0x09: // ora #
                Impl.ora(pcM());
                break;
            case 0x0a: // *asl a
                break;
            case 0x0b: // rhi
                pushr2(pcM());
                break;
            case 0x0d: // *ora abs
                break;
            case 0x0e: // *asl abs
                break;
            case 0x0f: // mul zp
                Impl.mul(peekM(pc1()));
                break;
            case 0x10: // *bpl rel
                break;
            case 0x11: // *ora (ind), y
                break;
            case 0x15: // *ora zp, x
                break;
            case 0x16: // *asl zp, x
                break;
            case 0x18: // clc
                FlagC = false;
                break;
            case 0x19: // *ora abs, y
                break;
            case 0x1a: // inc a
                A += 1;
                sNZ(Short.toUnsignedInt(A));
                break;
            case 0x1d: // *ora abs, x
                break;
            case 0x1e: // *asl abs, x
                break;
            case 0x20: // jsr abs
                var addr = pc2();
                push2(Short.toUnsignedInt(PC));
                PC = (short)addr;
                break;
            case 0x21: // *and (ind,x)
                break;
            case 0x22: // ent
                pushr2(Short.toUnsignedInt(I));
                I = (short)(Short.toUnsignedInt(PC) + 2);
                PC = (short)pc2();
                break;
            case 0x23: // and r, S
                Impl.and(peekM(pc1S()));
                break;
            case 0x24: // *bit zp
                break;
            case 0x25: // *and zp
                break;
            case 0x26: // *rol zp
                break;
            case 0x28: // plp
                setFlags(pop1());
                break;
            case 0x29: // *and #
                break;
            case 0x2a: // rol a
                A = (short)Impl.rol(Short.toUnsignedInt(A));
                break;
            case 0x2b: // rli
                I = (short)popr2();
                sNXZ(Short.toUnsignedInt(I));
                break;
            case 0x2c: // *bit abs
                break;
            case 0x2d: // *and abs
                break;
            case 0x2e: // *rol abs
                break;
            case 0x30: // bmi rel
                Impl.bra((byte)pc1(), FlagN);
                break;
            case 0x35: // *and zp, x
                break;
            case 0x36: // *rol zp, x
                break;
            case 0x38: // sec
                FlagC = true;
                break;
            case 0x39: // *and abs, y
                break;
            case 0x3a: // dec a
                A -= 1;
                sNZ(Short.toUnsignedInt(A));
                break;
            case 0x3d: // and abs, x
                Impl.and(peekM(pc2X()));
                break;
            case 0x3e: // *rol abs, x
                break;
            case 0x3f: // mul abs, x
                Impl.mul(peekM(pc2X()));
                break;
            case 0x40: // *rti
                break;
            case 0x41: // *eor (ind, x)
                break;
            case 0x42: // nxa
                A = (short)peekM(Short.toUnsignedInt(I));
                I += FlagM ? 1 : 2;
                break;
            case 0x43: // eor r, S
                Impl.eor(pc1S());
                break;
            case 0x45: // eor zp
                Impl.eor(peekM(pc1()));
                break;
            case 0x46: // *lsr zp
                break;
            case 0x48: // pha
                pushM(Short.toUnsignedInt(A));
                break;
            case 0x49: // eor #
                Impl.eor(pcM());
                break;
            case 0x4a: // *lsr a
                break;
            case 0x4b: // rha
                pushrM(Short.toUnsignedInt(A));
                break;
            case 0x4c: // jmp abs
                PC = (short)pc2();
                break;
            case 0x4d: // *eor abs
                break;
            case 0x4e: // *lsr abs
                break;
            case 0x50: // bvc rel
                Impl.bra((byte)pc1(), !FlagV);
                break;
            case 0x51: // *eor (ind), y
                break;
            case 0x55: // *eor zp, x
                break;
            case 0x56: // *lsr zp, x
                break;
            case 0x58: // *cli
                break;
            case 0x59: // *eor abs, y
                break;
            case 0x5a: // phy
                pushX(Short.toUnsignedInt(Y));
                break;
            case 0x5b: // rhy
                pushrX(Short.toUnsignedInt(Y));
                break;
            case 0x5c: // txi
                I = X;
                sNXZ(Short.toUnsignedInt(X));
                break;
            case 0x5d: // *eor abs, x
                break;
            case 0x5e: // *lsr abs, x
                break;
            case 0x60: // rts
                PC = (short)pop2();
                break;
            case 0x61: // *adc (ind, x)
                break;
            case 0x63: // adc r, S
                Impl.adc(peekM(pc1S()));
                break;
            case 0x64: // stz zp
                Impl.stz(pc1());
                break;
            case 0x65: // adc zp
                Impl.adc(peekM(pc1()));
                break;
            case 0x66: // *ror zp
                break;
            case 0x68: // pla
                A = (short)popM();
                sNZ(Short.toUnsignedInt(A));
                break;
            case 0x69: // adc #
                Impl.adc(pcM());
                break;
            case 0x6a: // ror a
                A = (short)Impl.ror(Short.toUnsignedInt(A));
                break;
            case 0x6b: // rla
                A = (short)poprM();
                sNZ(Short.toUnsignedInt(A));
                break;
            case 0x6c: // *jmp (ind)
                break;
            case 0x6d: // adc abs
                Impl.adc(peekM(pc2()));
                break;
            case 0x6e: // *ror abs
                break;
            case 0x70: // bvs rel
                Impl.bra(pc1(), FlagV);
                break;
            case 0x71: // *adc (ind), y
                break;
            case 0x74: // stz zp, x
                Impl.stz(pc1X());
                break;
            case 0x75: // *adc zp, x
                break;
            case 0x76: // *ror zp, x
                break;
            case 0x78: // *sei
                break;
            case 0x79: // *adc abs, y
                break;
            case 0x7a: // ply
                Y = (short)popX();
                sNXZ(Short.toUnsignedInt(Y));
                break;
            case 0x7b: // rly
                Y = (short)poprX();
                sNXZ(Short.toUnsignedInt(Y));
                break;
            case 0x7c: // jmp (ind, x)
                PC = (short)pc2IX();
                break;
            case 0x7d: // adc abs, x
                Impl.adc(peekM(pc2X()));
                break;
            case 0x7e: // *ror abs, x
                break;
            case 0x7f: // div abs, x
                Impl.div(peekM(pc2X()));
                break;
            case 0x80: // bra rel
                Impl.bra((byte)pc1(), true);
                break;
            case 0x81: // *sta (ind, x)
                break;
            case 0x84: // sty zp
                Impl.sty(pc1());
                break;
            case 0x85: // sta zp
                Impl.sta(pc1());
                break;
            case 0x86: // stx zp
                Impl.stx(pc1());
                break;
            case 0x88: // dey
                Y -= 1;
                sNXZ(Short.toUnsignedInt(Y));
                break;
            case 0x89: // bit #
                FlagZ = ((A & pcM()) == 0);
                break;
            case 0x8a: // txa
                A = X;
                sNZ(Short.toUnsignedInt(A));
                break;
            case 0x8b: // txr
                RP = X;
                sNZ(Short.toUnsignedInt(X));
                break;
            case 0x8c: // sty abs
                Impl.sty(pc2());
                break;
            case 0x8d: // sta abs
                Impl.sta(pc2());
                break;
            case 0x8e: // stx abs
                Impl.stx(pc2());
                break;
            case 0x90: // bcc rel
                Impl.bra((byte)pc1(), !FlagC);
                break;
            case 0x91: // sta (ind), y
                Impl.sta(pc2IY());
                break;
            case 0x92: // sta (ind)
                Impl.sta(pc2I());
                break;
            case 0x93: // sta (r, S), y
                Impl.sta(pc2ISY());
                break;
            case 0x94: // sty zp, x
                Impl.sty(pc1X());
                break;
            case 0x95: // sta zp, x
                Impl.sta(pc1X());
                break;
            case 0x96: // *stx zp, y
                break;
            case 0x98: // tya
                A = Y;
                break;
            case 0x99: // sta abs, y
                Impl.sta(pc2Y());
                break;
            case 0x9a: // txs
                SP = X;
                break;
            case 0x9c: // stz abs
                Impl.stz(pc2());
                break;
            case 0x9d: // sta abs, x
                Impl.sta(pc2X());
                break;
            case 0x9e: // stz abs, x
                Impl.stz(pc2X());
                break;
            case 0x9f: // sea
                D = 0;
                if((FlagM && ((byte)A < 0)) || (!FlagM && A < 0)) {
                    D = (short)maskM();
                }
                break;
            case 0xa0: // ldy #
                Impl.ldy(pcX());
                break;
            case 0xa1: // *lda (ind,x)
                break;
            case 0xa2: // ldx #
                Impl.ldx(pcX());
                break;
            case 0xa3: // lda r, S
                Impl.lda(peekM(pc1S()));
                break;
            case 0xa4: // *ldy zp
                break;
            case 0xa5: // lda zp
                Impl.lda(peekM(pc1()));
                break;
            case 0xa6: // *ldx zp
                break;
            case 0xa7: // lda r, R
                Impl.lda(peekM(pc1R()));
                break;
            case 0xa8: // tay
                Y = A;
                sNXZ(Short.toUnsignedInt(Y));
                break;
            case 0xa9: // lda #
                Impl.lda(pcM());
                break;
            case 0xaa: // tax
                X = A;
                sNXZ(Short.toUnsignedInt(X));
                break;
            case 0xac: // *ldy abs
                break;
            case 0xad: // lda abs
                Impl.lda(peekM(pc2()));
                break;
            case 0xae: // ldx abs
                Impl.ldx(peekM(pc2()));
                break;
            case 0xb0: // bcs rel
                Impl.bra((byte)pc1(), FlagC);
                break;
            case 0xb1: // lda (ind), y
                Impl.lda(peekM(pc2IY()));
                break;
            case 0xb3: // lda (r, S), y
                Impl.lda(peekM(pc2ISY()));
                break;
            case 0xb4: // *ldy zp, x
                break;
            case 0xb5: // *lda zp, x
                break;
            case 0xb6: // *ldx zp, y
                break;
            case 0xb8: // *clv
                break;
            case 0xb9: // lda abs, y
                Impl.lda(peekM(pc2Y()));
                break;
            case 0xba: // tsx
                X = SP;
                sNXZ(Short.toUnsignedInt(X));
                break;
            case 0xbc: // *ldy abs, x
                break;
            case 0xbd: // lda abs, x
                Impl.lda(peekM(pc2X()));
                break;
            case 0xbe: // *ldx abs, y
                break;
            case 0xc0: // cpy #
                Impl.cmpx(Y, pcX());
                break;
            case 0xc1: // *cmp (ind, x)
                break;
            case 0xc2: // rep #
                Impl.rep(pc1());
                break;
            case 0xc3: // cmp r, S
                Impl.cmp(A, peekM(pc1S()));
                break;
            case 0xc4: // *cpy zp
                break;
            case 0xc5: // *cmp zp
                break;
            case 0xc6: // dec zp
                Impl.dec(pc1());
                break;
            case 0xc8: // iny
                Y += 1;
                sNXZ(Short.toUnsignedInt(Y));
                break;
            case 0xc9: // cmp #
                Impl.cmp(A, pcM());
                break;
            case 0xca: // dex
                X -= 1;
                sNXZ(Short.toUnsignedInt(X));
                break;
            case 0xcb: // wai
                Wait = true;
                break;
            case 0xcc: // *cpy abs
                break;
            case 0xcd: // *cmp abs
                break;
            case 0xce: // *dec abs
                break;
            case 0xcf: // pld
                D = (short)popM();
                break;
            case 0xd0: // bne rel
                Impl.bra((byte)pc1(), !FlagZ);
                break;
            case 0xd1: // *cmp (ind), y
                break;
            case 0xd5: // *cmp zp, x
                break;
            case 0xd6: // *dec zp, x
                break;
            case 0xd8: // *cld
                break;
            case 0xd9: // *cmp abs, y
                break;
            case 0xda: // phx
                pushX(Short.toUnsignedInt(X));
                break;
            case 0xdb: // stp
                Stop = true;
                break;
            case 0xdc: // tix
                X = I;
                sNXZ(Short.toUnsignedInt(X));
                break;
            case 0xdd: // cmp abs, x
                Impl.cmp(A, peekM(pc2X()));
                break;
            case 0xde: // dec abs, x
                Impl.dec(pc2X());
                break;
            case 0xdf: // phd
                pushM(Short.toUnsignedInt(D));
                break;
            case 0xe0: // *cpx #
                break;
            case 0xe1: // *sbc (ind, x)
                break;
            case 0xe2: // sep #
                Impl.sep(pc1());
                break;
            case 0xe3: // sbc s, R
                Impl.sbc(peekM(pc1S()));
                break;
            case 0xe4: // *cpx zp
                break;
            case 0xe5: // *sbc zp
                break;
            case 0xe6: // inc zp
                Impl.inc(pc1());
                break;
            case 0xe8: // *inx
                break;
            case 0xe9: // *sbc #
                break;
            case 0xea: // *nop
                break;
            case 0xeb: // xba
                if(FlagM) {
                    var b = B;
                    B = A;
                    A = b;
                } else {
                    var a = Short.toUnsignedInt(A) << 8;
                    var b = Byte.toUnsignedInt((byte)(Short.toUnsignedInt(A) >> 8));
                    A = (short)(A | B);
                }
                break;
            case 0xec: // *cpx abs
                break;
            case 0xed: // *sbc abs
                break;
            case 0xee: // inc abs
                Impl.inc(pc2());
                break;
            case 0xef: // cop/mmu
                break;
            case 0xf0: // beq rel
                Impl.bra((byte)pc1(), FlagZ);
                break;
            case 0xf1: // *sbc (ind), y
                break;
            case 0xf5: // *sbc zp, x
                break;
            case 0xf6: // *inc zp, x
                break;
            case 0xf8: // *sed
                break;
            case 0xf9: // *sbc abs, y
                break;
            case 0xfa: // plx
                X = (short)popX();
                sNXZ(Short.toUnsignedInt(X));
                break;
            case 0xfb: // xce
                if(FlagC != FlagE) {
                    if(FlagC) {
                        FlagC = false;
                        FlagE = true;
                        FlagX = true;
                        if(!FlagM) {B = (short)(Short.toUnsignedInt(A) >> 8);}
                        FlagM = true;
                    } else {
                        FlagC = true;
                        FlagE = false;
                    }
                }
                break;
            case 0xfd: // *sbc abs, x
                break;
            case 0xfe: // inc abs, x
                Impl.inc(pc2X());
                break;
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
    private int pc1R() {return pc1() + RP;}
    private int pcM() {if(FlagM) {return pc1();} else {return pc2();}}
    private int pcX() {if(FlagX) {return pc1();} else {return pc2();}}
    private int pc1X() {if(FlagX) {return (pc1() + X) & 0xFF;} else {return (pc2() + X) & 0xFFFF;}}
    private int pc1Y() {if(FlagX) {return (pc1() + Y) & 0xFF;} else {return (pc2() + Y) & 0xFFFF;}}
    private int pcMX() {if(FlagX) {return (pcM() + X) & 0xFF;} else {return (pcM() + X) & 0xFFFF;}}
    private int pcXX() {if(FlagX) {return (pcX() + X) & 0xFF;} else {return (pcX() + X) & 0xFFFF;}}
    private int pc2I() {return peek2(pc2());}
    private int pc2IX() {return peek2(pc2() + X);}
    private int pc2IY() {return (peek2(pc2()) + Y) & 0xFFFF;}
    private int pc2SY() {return (peek2(pc1S()) + Y) & 0xFFFF;}
    private int pc2RY() {return (peek2(pc1R()) + Y) & 0xFFFF;}
    private int pc2ISY() {return (peek2(pc1S()) + Y) & 0xFFFF;}

    private int peek1(int addr) {
        var uaddr = addr & 0xFFFF;
        if(BusEnabled && uaddr >= BusOffset && uaddr <= BusOffset+0xFF) {
            Ravynstone.LOGGER.warn("Bus Read is currently unimplemented");
            return 0;
        } else {
            return Byte.toUnsignedInt(Host.memRead((short)uaddr));
        }
    }
    private int peek2(int addr) {return peek1(addr) | (peek1(addr + 1) << 8);}
    private int peekM(int addr) {if(FlagM) {return peek1(addr);} else {return peek2(addr);}}
    private int peekX(int addr) {if(FlagX) {return peek1(addr);} else {return peek2(addr);}}

    private void poke1(int addr, int b) {
        var uaddr = addr & 0xFFFF;
        if(BusEnabled && uaddr >= BusOffset && uaddr <= BusOffset+0xFF) {
            Ravynstone.LOGGER.warn("Bus Write is currently unimplemented");
        } else {
            Host.memStore((short)uaddr, (byte)(b & 0xFF));
        }
    }
    private void poke2(int addr, int s) {poke1(addr, s); poke1(addr + 1, s >>> 8);}
    private void pokeM(int addr, int s) {if(FlagM) {poke1(addr, s);} else {poke2(addr, s);}}
    private void pokeX(int addr, int s) {if(FlagX) {poke1(addr, s);} else {poke2(addr, s);}}

    private void push1(int b) {poke1(SP, b); SP -= 1;}
    private void push2(int s) {push1(s >> 8); push1(s);}
    private void pushM(int s) {if(FlagM) {push1(s);} else {push2(s);}}
    private void pushX(int s) {if(FlagX) {push1(s);} else {push2(s);}}
    private int pop1() {SP += 1; return peek1(SP);}
    private int pop2() {return pop1() | (pop1() << 8);}
    private int popM() {if(FlagM) {return pop1();} else {return pop2();}}
    private int popX() {if(FlagX) {return pop1();} else {return pop2();}}

    private void pushr1(int b) {poke1(RP, b); RP -= 1;}
    private void pushr2(int s) {pushr1(s >>> 8); pushr1(s);}
    private void pushrM(int s) {if(FlagM) {pushr1(s);} else {pushr2(s);}}
    private void pushrX(int s) {if(FlagX) {pushr1(s);} else {pushr2(s);}}

    private int popr1() {RP += 1; return peek1(RP);}
    private int popr2() {return popr1() | (popr1() << 8);}
    private int poprM() {if(FlagM) {return popr1();} else {return popr2();}}
    private int poprX() {if(FlagX) {return popr1();} else {return popr2();}}

    private int packFlags() {
        int c = FlagC ? (1 << 0) : 0;
        int z = FlagZ ? (1 << 1) : 0;
        int i = FlagI ? (1 << 2) : 0;
        int d = FlagD ? (1 << 3) : 0;
        int x = FlagX ? (1 << 4) : 0;
        int m = FlagM ? (1 << 5) : 0;
        int v = FlagV ? (1 << 6) : 0;
        int n = FlagN ? (1 << 7) : 0;
        int e = FlagE ? (1 << 8) : 0;
        return c|z|i|d|x|m|v|n|e;
    }
    private void setFlags(int b) {
        Boolean m = ((b & (1 << 5)) > 0);
        FlagC = (b & (1 << 0)) != 0;
        FlagZ = (b & (1 << 1)) != 0;
        FlagI = (b & (1 << 2)) != 0;
        FlagD = (b & (1 << 3)) != 0;
        FlagX = (b & (1 << 4)) != 0;
        FlagV = (b & (1 << 6)) != 0;
        FlagN = (b & (1 << 7)) != 0;
        if(FlagE) {
            FlagX = false;
            FlagM = false;
        } else {
            FlagX = ((b & (1 << 4)) > 0);
            if(FlagX) {
                X = (short)(X & 0xff);
                Y = (short)(Y & 0xff);
            }
            if(m != FlagM) {
                if(m) {
                    B = (short)(A >> 8);
                    A = (short)(A & 0xFF);
                } else {
                    A = (short)(A | (B << 8));
                }
                FlagM = m;
            }
        }
    }

    private int toBCD(int s) {return Short.toUnsignedInt((short)Integer.parseInt(Integer.toString(s),16));}
    private int fromBCD(int s) {
        try {
            return Short.toUnsignedInt((short)Integer.parseInt(Integer.toString(s,16)));
        } catch(NumberFormatException e) {
            Error = true;
            return 0;
        }
    }
    private int negM() {if(FlagM) {return 0x80;} else {return 0x8000;}}
    private int negX() {if(FlagX) {return 0x80;} else {return 0x8000;}}
    private int maskM() {if(FlagM) {return 0xFF;} else {return 0xFFFF;}}
    private int maskX() {if(FlagX) {return 0xFF;} else {return 0xFFFF;}}

    private void sNZ(int i) {
        int s = (i & maskM());
        FlagZ = (s == 0);
        FlagN = ((s & negM()) != 0);
    }
    private void sNXZ(int i) {
        int s = (i & maskX());
        FlagZ = (s == 0);
        FlagN = ((s & negX()) != 0);
    }
    private void sNZC(int i) {
        FlagC = (int)(FlagM ? (byte)i : (short)i) >= 0;
        sNZ(i);
    }
    private void sNXZC(int i) {
        FlagC = (int)(FlagX ? (byte)i : (short)i) >= 0;
        sNZ(i);
    }

    private class InstImpl {
        void adc(int data) {
            int i;
            if(FlagD) {
                // BCD Stuff
                var a = fromBCD(Short.toUnsignedInt(A));
                var d = fromBCD(data);
                i = toBCD(a+d+(FlagC ? 1 : 0));
            } else {
                i = Short.toUnsignedInt(A) + data + (FlagC ? 1 : 0);
            }
            FlagC = (i & maskM()) != i;
            FlagV = ((Short.toUnsignedInt(A) ^ data) & (Short.toUnsignedInt(A) ^ i) & negM()) != 0;
            A = (short)i;
            sNZ(Short.toUnsignedInt(A));
        }
        void sbc(int data) {
            adc((~data) & maskM());
        }
        void inc(int addr) {
            var data = peekM(addr) + 1;
            pokeM(addr, data);
            sNZ(data);
        }
        void dec(int addr) {
            var data = peekM(addr) - 1;
            pokeM(addr, data);
            sNZ(data);
        }
        void mul(int data) {
            if(FlagC) {
                if(FlagM) {
                    int c = (Short.toUnsignedInt(A) & 0xFF) * (data & 0xFF);
                    A = (short)c;
                    D = (short)(c >> 8);
                    FlagN = false;
                    FlagZ = (c == 0);
                    FlagV = (c & 0xffff0000) != 0;
                } else {
                    long c = (Short.toUnsignedLong(A) & 0xFFFF) * (((long)data) & 0xFFFF);
                    A = (short)(c & 0xFFFF);
                    D = (short)((c >> 16) & 0xFFFF);
                    FlagN = false;
                    FlagZ = (c == 0L);
                    FlagV = (c & 0xffffffff00000000L) != 0L;
                }
            } else {
                if(FlagM) {
                    int c = ((int)((byte)A)) * ((int)((byte)data));
                    A = (short)c;
                    D = (short)(c >> 8);
                    FlagN = (c < 0);
                    FlagZ = (c == 0);
                    FlagV = (c & 0xffff0000) != 0;
                } else {
                    long c = ((long)((short)A)) * ((long)((short)data));
                    A = (short)(c & 0xFFFF);
                    D = (short)(c >> 16);
                    FlagN = (c < 0L);
                    FlagZ = (c == 0L);
                    FlagV = (c & 0xffffffff00000000L) != 0L;
                }
            }
        }
        void div(int data) {
            if(data == 0) {
                FlagV = true;
                A = 0;
                D = 0;
                sNZ(0);
                Error = true;
            } else {
                if(FlagC) {
                    if(FlagM) {
                        int a = Short.toUnsignedInt((short)(A | (D << 8))) & 0xffff;
                        A = (short)(a / data);
                        D = (short)(a % data);
                    } else {
                        long a = (Short.toUnsignedLong(A) | (Short.toUnsignedLong(D) << 16)) & 0xffffffffL;
                        A = (short)(a / data);
                        D = (short)(a % data);
                    }
                } else {
                    if(FlagM) {
                        short a = (short)(A | (D << 8));
                        byte b = (byte)data;
                        A = (short)(a/b);
                        D = (short)(a%b);
                    } else {
                        int a = (short)(A | (D << 16));
                        A = (short)(a / data);
                        D = (short)(a % data);
                    }
                }
            }
            sNZ(Short.toUnsignedInt(A));
            FlagV = (D != 0);
        }
        void and(int data) {
            A = (short)(A & data);
            sNZ(Short.toUnsignedInt(A));
        }
        void ora(int data) {
            A = (short)(A | data);
            sNZ(Short.toUnsignedInt(A));
        }
        void eor(int data) {
            A = (short)(A ^ data);
            sNZ(Short.toUnsignedInt(A));
        }
        int rol(int data) {
            var i = Short.toUnsignedInt((short)( (data << 1) | (FlagC?1:0)) );
            FlagC = (data & negM()) != 0;
            sNZ(i);
            return i;
        }
        int ror(int data) {
            var i = Short.toUnsignedInt((short)( (data >>> 1) | (FlagC?negM():0)) );
            FlagC = (data & 1) != 0;
            sNZ(i);
            return i;
        }
        void tsb(int data) {
            FlagZ = (data & Short.toUnsignedInt(A)) != 0;
            A = (short)(Short.toUnsignedInt(A) | data);
        }
        void bra(int off, Boolean b) {if(b) {PC += (byte)off;}}
        void stz(int addr) {
            poke1(addr, 0);
            if(!FlagM) {poke1(addr + 1,0);}
        }
        void sta(int addr) {pokeM(addr, Short.toUnsignedInt(A));}
        void stx(int addr) {pokeM(addr, Short.toUnsignedInt(X));}
        void sty(int addr) {pokeM(addr, Short.toUnsignedInt(Y));}
        void lda(int data) {
            A = (short)data;
            sNZ(Short.toUnsignedInt(A));
        }
        void ldx(int data) {
            X = (short)data;
            sNXZ(Short.toUnsignedInt(X));
        }
        void ldy(int data) {
            Y = (short)data;
            sNXZ(Short.toUnsignedInt(Y));
        }
        void cmp(int a, int b) {
            sNZC(a - b);
        }
        void cmpx(int a, int b) {
            sNXZC(a - b);
        }
        void sep(int data) {setFlags(packFlags() | data);}
        void rep(int data) {setFlags(packFlags() & (~data));}

        int asl(int s) {
            int i = s;
            FlagC = ((i & negM()) > 0);
            i = (i << 1) & maskM();
            sNZ(i);
            return i;
        }
    }
}
