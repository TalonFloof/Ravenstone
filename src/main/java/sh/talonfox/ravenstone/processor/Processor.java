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
            ///// NON-LOGICAL OPERATIONS /////
            ///// LDA /////
            case 0xa9: { // lda #
                byte val = (byte) pc1();
                A = val;
                setZNFlags(val);
                break;
            } case 0xa5: { // lda zp
                byte val = (byte)peek1(pc1());
                A = val;
                setZNFlags(val);
                break;
            } case 0xb5: { // lda zp, x
                byte val = (byte)peek1(pc1()+X);
                A = val;
                setZNFlags(val);
                break;
            } case 0xad: { // lda abs
                byte val = (byte)peek1(pc2());
                A = val;
                setZNFlags(val);
                break;
            } case 0xbd: { // lda abs, x
                byte val = (byte)peek1(pc2()+X);
                A = val;
                setZNFlags(val);
                break;
            } case 0xb9: { // lda abs, y
                byte val = (byte)peek1(pc2()+Y);
                A = val;
                setZNFlags(val);
                break;
            } case 0xa1: { // lda (ind, x)
                byte val = (byte)peek1(peek2(pc1()+X));
                A = val;
                setZNFlags(val);
                break;
            } case 0xb1: { // lda (ind), y
                
                break;
            ///// LDX /////
            } case 0xa2: { // ldx #
                break;
            } case 0xa6: { // ldx zp
                break;
            } case 0xb6: { // ldx zp, y
                break;
            } case 0xae: { // ldx abs
                break;
            } case 0xbe: { // ldx abs, y
                break;
            ///// LDY /////
            } case 0xa0: { // ldy #
                break;
            } case 0xa4: { // ldy zp
                break;
            } case 0xb4: { // ldy zp, x
                break;
            } case 0xac: { // ldy abs
                break;
            } case 0xbc: { // ldy abs, x
                break;
            ///// STA /////
            } case 0x85: { // sta zp
                break;
            } case 0x95: { // sta zp, x
                break;
            } case 0x8d: { // sta abs
                break;
            } case 0x9d: { // sta abs, x
                break;
            } case 0x99: { // sta abs, y
                break;
            } case 0x81: { // sta (ind, x)
                break;
            } case 0x91: { // sta (ind), y
                break;
            ///// STX /////
            } case 0x86: { // stx zp
                break;
            } case 0x96: { // stx zp, y
                break;
            } case 0x8e: { // stx abs
                break;
            ///// STY /////
            } case 0x84: { // sty zp
                break;
            } case 0x94: { // sty zp, x
                break;
            } case 0x8c: { // sty abs
                break;
            ///// MISC. /////
            } case 0xba: { // tsx
                break;
            } case 0x9a: { // txs
                break;
            } case 0x48: { // pha
                break;
            } case 0x68: { // pla
                break;
            } case 0x08: { // php
                break;
            } case 0x28: { // plp
                break;
            } case 0x4c: { // jmp abs
                break;
            } case 0x6c: { // jmp ind
                break;
            } case 0x20: { // jsr abs
                break;
            } case 0x60: { // rts
                break;
            ///// LOGICAL OPERATIONS /////
            ///// AND /////
            } case 0x29: { // and #
                break;
            } case 0x25: { // and zp
                break;
            } case 0x35: { // and zp, x
                break;
            } case 0x2d: { // and abs
                break;
            } case 0x3d: { // and abs, x
                break;
            } case 0x39: { // and abs, y
                break;
            } case 0x21: { // and (ind, x)
                break;
            } case 0x31: { // and (ind), y
                break;
            ///// OR /////
            } case 0x09: { // or #
                break;
            } case 0x05: { // or zp
                break;
            } case 0x15: { // or zp, x
                break;
            } case 0x0d: { // or abs
                break;
            } case 0x1d: { // or abs, x
                break;
            } case 0x19: { // or abs, y
                break;
            } case 0x01: { // or (ind, x)
                break;
            } case 0x11: { // or (ind), y
                break;
            ///// EOR /////
            } case 0x49: { // eor #
                break;
            } case 0x45: { // eor zp
                break;
            } case 0x55: { // eor zp, x
                break;
            } case 0x4d: { // eor abs
                break;
            } case 0x5d: { // eor abs, x
                break;
            } case 0x59: { // eor abs, y
                break;
            } case 0x41: { // eor (ind, x)
                break;
            } case 0x51: { // eor (ind), y
                break;
            ///// BIT /////
            } case 0x24: { // bit zp
                break;
            } case 0x2c: { // bit abs
                break;
            ///// TRANSFER /////
            } case 0xaa: { // tax
                break;
            } case 0xa8: { // tay
                break;
            } case 0x8a: { // txa
                break;
            } case 0x98: { // tya
                break;
            ///// INCREMENT/DECREMENT /////
            } case 0xe8: { // inx
                break;
            } case 0xc8: { // iny
                break;
            } case 0x88: { // dey
                break;
            } case 0xca: { // dex
                break;
            } case 0xc6: { // dec zp
                break;
            } case 0xd6: { // dec zp, x
                break;
            } case 0xce: { // dec abs
                break;
            } case 0xde: { // dec abs, x
                break;
            } case 0xe6: { // inc zp
                break;
            } case 0xf6: { // inc zp, x
                break;
            } case 0xee: { // inc abs
                break;
            } case 0xfe: { // inc abs, x
                break;
            ///// BRANCH /////
            } case 0xf0: { // beq
                break;
            } case 0xd0: { // bne
                break;
            } case 0xb0: { // bcs
                break;
            } case 0x90: { // bcc
                break;
            } case 0x30: { // bmi
                break;
            } case 0x10: { // bpl
                break;
            } case 0x50: { // bvc
                break;
            } case 0x70: { // bvs
                break;
            ///// FLAG MODIFIERS /////
            } case 0x18: { // clc
                break;
            } case 0x38: { // sec
                break;
            } case 0xd8: { // cld
                break;
            } case 0xf8: { // sed
                break;
            } case 0x58: { // cli
                break;
            } case 0x78: { // sei
                break;
            } case 0xb8: { // clv
                break;
            ///// ARITHMETIC /////
            } case 0x69: { // adc #
                break;
            } case 0x65: { // adc zp
                break;
            } case 0x75: { // adc zp, x
                break;
            } case 0x6d: { // adc abs
                break;
            } case 0x7d: { // adc abs, x
                break;
            } case 0x79: { // adc abs, y
                break;
            } case 0x61: { // adc (ind, x)
                break;
            } case 0x71: { // adc (ind), y
                break;

            } case 0xe9: { // sbc #
                break;
            } case 0xed: { // sbc abs
                break;
            } case 0xe5: { // sbc zp
                break;
            } case 0xf5: { // sbc zp, x
                break;
            } case 0xfd: { // sbc abs, x
                break;
            } case 0xf9: { // sbc abs, y
                break;
            } case 0xe1: { // sbc (ind, x)
                break;
            } case 0xf1: { // sbc (ind), y
                break;
            ///// COMPARISON /////
            } case 0xc9: { // cmp #
                break;
            } case 0xc5: { // cmp zp
                break;
            } case 0xd5: { // cmp zp, x
                break;
            } case 0xcd: { // cmp abs
                break;
            } case 0xdd: { // cmp abs, x
                break;
            } case 0xd9: { // cmp abs, y
                break;
            } case 0xc1: { // cmp (ind, x)
                break;
            } case 0xd1: { // cmp (ind), y
                break;

            } case 0xe0: { // cpx
                break;
            } case 0xc0: { // cpy
                break;
            } case 0xe4: { // cpx zp
                break;
            } case 0xc4: { // cpy zp
                break;
            } case 0xec: { // cpx abs
                break;
            } case 0xcc: { // cpy abs
                break;
            ///// SHIFT /////
            } case 0x0a: { // asl #
                break;
            } case 0x06: { // asl zp
                break;
            } case 0x16: { // asl zp, x
                break;
            } case 0x0e: { // asl abs
                break;
            } case 0x1e: { // asl abs, x
                break;

            } case 0x4a: { // lsr #
                break;
            } case 0x46: { // lsr zp
                break;
            } case 0x56: { // lsr zp, x
                break;
            } case 0x4e: { // lsr abs
                break;
            } case 0x5e: { // lsr abs, x
                break;

            } case 0x2a: { // rol #
                break;
            } case 0x26: { // rol zp
                break;
            } case 0x36: { // rol zp, x
                break;
            } case 0x2e: { // rol abs
                break;
            } case 0x3e: { // rol abs, x
                break;

            } case 0x6a: { // ror #
                break;
            } case 0x66: { // ror zp
                break;
            } case 0x76: { // ror zp, x
                break;
            } case 0x6e: { // ror abs
                break;
            } case 0x7e: { // ror abs, x
                break;
            ///// EXTRA INSTRUCTIONS /////
            } case 0xea: { // nop
                break;
            } case 0x00: { // brk
                break;
            } case 0x40: { // rti
                break;
            } case 0xef: { // mmu
                break;
            } case 0xcb: { // wai
                Wait = true;
                break;
            } case 0xdb: { // hlt (Halts. Has unintended side-effects)
                Stop = true;
                Host.explode();
                break;
            } case 0xdf: { // mas (Move Accumulator to Upper 8-bits of Stack Pointer)
                SP = (short) ((Byte.toUnsignedInt(A) << 8) | 0xFF);
                break;
            } default:
                Error = true;
                break;
        }
    }

    private int pc1() {PC += 1; return peek1(Short.toUnsignedInt(PC) - 1);}
    private int pc2() {return pc1() | (pc1() << 8);}
    private int pc2X() {return pc2() + X;}
    private int pc2Y() {return pc2() + Y;}

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

    private void setZNFlags(byte val) {
        FlagZ = (val == 0);
        FlagN = ((val & 0x80) != 0);
    }

    private class InstImpl {

    }
}
