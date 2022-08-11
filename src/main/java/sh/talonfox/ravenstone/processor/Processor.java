package sh.talonfox.ravenstone.processor;

import sh.talonfox.ravenstone.Ravenstone;

public class Processor {
    public ProcessorHost Host;
    public byte A = 0;
    public byte X = 0;
    public byte Y = 0;
    public short PC = (short)0xf000;
    public short SP = 0x1ff;
    public Boolean FlagC = false;
    public Boolean FlagZ = false;
    public Boolean FlagI = false;
    public Boolean FlagD = false;
    public Boolean FlagV = false;
    public Boolean FlagN = false;
    public short ResetAddr = (short)0xf000;
    public short BrkAddr = (short)0xf000;
    public Boolean Wait = false;
    public Boolean Stop = true;
    public int BusOffset = 0;
    public Boolean Error = false;
    public static byte[] MONITOR = null;

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
        PC = (short)0xf000;
        ResetAddr = (short)0xf000;
        BrkAddr = (short)0xf000;
        Wait = false;
        Error = false;
        BusOffset = 0;
    }
    public void next() {
        Stop = false;
        Wait = false;
        var insn = pc1();
        switch (insn) {
            ///// NON-LOGICAL OPERATIONS /////
            ///// LDA /////
            case 0xa9 -> { // lda #
                A = (byte) pc1();
                setZNFlags(A);

            }
            case 0xa5 -> { // lda zp
                A = (byte) peek1(pc1());
                setZNFlags(A);
            }
            case 0xb5 -> { // lda zp, x
                A = (byte) peek1(pc1() + Byte.toUnsignedInt(X));
                setZNFlags(A);

            }
            case 0xad -> { // lda abs
                A = (byte) peek1(pc2());
                setZNFlags(A);

            }
            case 0xbd -> { // lda abs, x
                A = (byte) peek1(pc2() + Byte.toUnsignedInt(X));
                setZNFlags(A);

            }
            case 0xb9 -> { // lda abs, y
                A = (byte) peek1(pc2() + Byte.toUnsignedInt(Y));
                setZNFlags(A);

            }
            case 0xa1 -> { // lda (ind, x)
                A = (byte) peek1(peek2(pc1() + Byte.toUnsignedInt(X)));
                setZNFlags(A);

            }
            case 0xb1 -> { // lda (ind), y
                A = (byte) peek1(peek2(pc1()) + Byte.toUnsignedInt(Y));
                setZNFlags(A);

                ///// LDX /////
            }
            case 0xa2 -> { // ldx #
                X = (byte) pc1();
                setZNFlags(X);

            }
            case 0xa6 -> { // ldx zp
                X = (byte) peek1(pc1());
                setZNFlags(X);

            }
            case 0xb6 -> { // ldx zp, y
                X = (byte) peek1(pc1() + Byte.toUnsignedInt(Y));
                setZNFlags(X);

            }
            case 0xae -> { // ldx abs
                X = (byte) peek1(pc2());
                setZNFlags(X);

            }
            case 0xbe -> { // ldx abs, y
                X = (byte) peek1(pc2() + Byte.toUnsignedInt(Y));
                setZNFlags(X);

                ///// LDY /////
            }
            case 0xa0 -> { // ldy #
                Y = (byte) pc1();
                setZNFlags(Y);

            }
            case 0xa4 -> { // ldy zp
                Y = (byte) peek1(pc1());
                setZNFlags(Y);

            }
            case 0xb4 -> { // ldy zp, x
                Y = (byte) peek1(pc1() + Byte.toUnsignedInt(X));
                setZNFlags(Y);

            }
            case 0xac -> { // ldy abs
                Y = (byte) peek1(pc2());
                setZNFlags(Y);

            }
            case 0xbc -> { // ldy abs, x
                Y = (byte) peek1(pc2() + Byte.toUnsignedInt(X));
                setZNFlags(Y);

                ///// STA /////
            }
            case 0x85 -> { // sta zp
                poke1(pc1(), Byte.toUnsignedInt(A));

            }
            case 0x95 -> { // sta zp, x
                poke1(pc1() + Byte.toUnsignedInt(X), Byte.toUnsignedInt(A));

            }
            case 0x8d -> { // sta abs
                poke1(pc2(), Byte.toUnsignedInt(A));

            }
            case 0x9d -> { // sta abs, x
                poke1(pc2() + Byte.toUnsignedInt(X), Byte.toUnsignedInt(A));

            }
            case 0x99 -> { // sta abs, y
                poke1(pc2() + Byte.toUnsignedInt(Y), Byte.toUnsignedInt(A));

            }
            case 0x81 -> { // sta (ind, x)
                poke1(peek2(pc1() + Byte.toUnsignedInt(X)), Byte.toUnsignedInt(A));

            }
            case 0x91 -> { // sta (ind), y
                poke1(peek2(pc1()) + Byte.toUnsignedInt(Y), Byte.toUnsignedInt(A));

                ///// STX /////
            }
            case 0x86 -> { // stx zp
                poke1(pc1(), Byte.toUnsignedInt(X));

            }
            case 0x96 -> { // stx zp, y
                poke1(pc1() + Byte.toUnsignedInt(Y), Byte.toUnsignedInt(X));

            }
            case 0x8e -> { // stx abs
                poke1(pc2(), Byte.toUnsignedInt(X));

                ///// STY /////
            }
            case 0x84 -> { // sty zp
                poke1(pc1(), Byte.toUnsignedInt(Y));

            }
            case 0x94 -> { // sty zp, x
                poke1(pc1() + Byte.toUnsignedInt(X), Byte.toUnsignedInt(Y));

            }
            case 0x8c -> { // sty abs
                poke1(pc2(), Byte.toUnsignedInt(Y));

                ///// MISC. /////
            }
            case 0xba -> { // tsx
                X = (byte) (Short.toUnsignedInt(SP) & 0xff);
                setZNFlags(X);

            }
            case 0x9a -> { // txs
                SP &= 0xFF00;
                SP |= Byte.toUnsignedInt(X);
                setZNFlags(X);

            }
            case 0x48 -> { // pha
                push1(Byte.toUnsignedInt(A));

            }
            case 0x68 -> { // pla
                A = (byte) pop1();
                setZNFlags(A);

            }
            case 0x08 -> { // php
                push1(packFlags());

            }
            case 0x28 -> { // plp
                setFlags(pop1());

            }
            case 0x4c -> { // jmp abs
                PC = (short)pc2();

            /*
                =====NOTICE=====
                NMOS 6502's have a hardware bug with indirect jumps where
                jumps would wrap to the beginning of a page if it was in
                the page boundary. ( e.g. $xxFF would jump to $xx00 instead of $xxFF+1 )
                However, the Ravenstone emulator doesn't emulate this bug, similar to the
                CMOS variations of the 6502 ( such as the 65C02 ).
             */
            }
            case 0x6c -> { // jmp ind
                PC = (short)peek2(pc2());
            }
            case 0x20 -> { // jsr abs
                int subAddr = pc2();
                push2(Short.toUnsignedInt(PC) - 1);
                PC = (short)subAddr;
            }
            case 0x60 -> { // rts
                PC = (short)(pop2() + 1);
                ///// LOGICAL OPERATIONS /////
                ///// AND /////
            }
            case 0x29 -> { // and #
                A &= pc1();
                setZNFlags(A);

            }
            case 0x25 -> { // and zp
                A &= peek1(pc1());
                setZNFlags(A);

            }
            case 0x35 -> { // and zp, x
                A &= peek1(pc1() + Byte.toUnsignedInt(X));
                setZNFlags(A);

            }
            case 0x2d -> { // and abs
                A &= peek1(pc2());
                setZNFlags(A);

            }
            case 0x3d -> { // and abs, x
                A &= peek1(pc2() + Byte.toUnsignedInt(X));
                setZNFlags(A);

            }
            case 0x39 -> { // and abs, y
                A &= peek1(pc2() + Byte.toUnsignedInt(Y));
                setZNFlags(A);

            }
            case 0x21 -> { // and (ind, x)
                A &= peek1(peek2(pc1() + Byte.toUnsignedInt(X)));
                setZNFlags(A);

            }
            case 0x31 -> { // and (ind), y
                A &= peek1(peek2(pc1()) + Byte.toUnsignedInt(Y));
                setZNFlags(A);

                ///// OR /////
            }
            case 0x09 -> { // or #
                A |= pc1();
                setZNFlags(A);

            }
            case 0x05 -> { // or zp
                A |= peek1(pc1());
                setZNFlags(A);

            }
            case 0x15 -> { // or zp, x
                A |= peek1(pc1() + Byte.toUnsignedInt(X));
                setZNFlags(A);

            }
            case 0x0d -> { // or abs
                A |= peek1(pc2());
                setZNFlags(A);

            }
            case 0x1d -> { // or abs, x
                A |= peek1(pc2() + Byte.toUnsignedInt(X));
                setZNFlags(A);

            }
            case 0x19 -> { // or abs, y
                A |= peek1(pc2() + Byte.toUnsignedInt(Y));
                setZNFlags(A);

            }
            case 0x01 -> { // or (ind, x)
                A |= peek1(peek2(pc1() + Byte.toUnsignedInt(X)));
                setZNFlags(A);

            }
            case 0x11 -> { // or (ind), y
                A |= peek1(peek2(pc1()) + Byte.toUnsignedInt(Y));
                setZNFlags(A);

                ///// EOR /////
            }
            case 0x49 -> { // eor #
                A ^= pc1();
                setZNFlags(A);

            }
            case 0x45 -> { // eor zp
                A ^= peek1(pc1());
                setZNFlags(A);

            }
            case 0x55 -> { // eor zp, x
                A ^= peek1(pc1() + Byte.toUnsignedInt(X));
                setZNFlags(A);

            }
            case 0x4d -> { // eor abs
                A ^= peek1(pc2());
                setZNFlags(A);

            }
            case 0x5d -> { // eor abs, x
                A ^= peek1(pc2() + Byte.toUnsignedInt(X));
                setZNFlags(A);

            }
            case 0x59 -> { // eor abs, y
                A ^= peek1(pc2() + Byte.toUnsignedInt(Y));
                setZNFlags(A);

            }
            case 0x41 -> { // eor (ind, x)
                A ^= peek1(peek2(pc1() + Byte.toUnsignedInt(X)));
                setZNFlags(A);

            }
            case 0x51 -> { // eor (ind), y
                A ^= peek1(peek2(pc1()) + Byte.toUnsignedInt(Y));
                setZNFlags(A);

                ///// BIT /////
            }
            case 0x24 -> { // bit zp
                int val = peek1(pc1());
                FlagZ = ((Byte.toUnsignedInt(A) & val) == 0);
                FlagN = ((val & 0x80) != 0);
                FlagV = ((val & 0x40) != 0);

            }
            case 0x2c -> { // bit abs
                int val = peek1(pc2());
                FlagZ = ((Byte.toUnsignedInt(A) & val) == 0);
                FlagN = ((val & 0x80) != 0);
                FlagV = ((val & 0x40) != 0);

                ///// TRANSFER /////
            }
            case 0xaa -> { // tax
                X = A;
                setZNFlags(X);

            }
            case 0xa8 -> { // tay
                Y = A;
                setZNFlags(Y);

            }
            case 0x8a -> { // txa
                A = X;
                setZNFlags(A);

            }
            case 0x98 -> { // tya
                A = Y;
                setZNFlags(A);

                ///// INCREMENT/DECREMENT /////
            }
            case 0xe8 -> { // inx
                X++;
                setZNFlags(X);

            }
            case 0xc8 -> { // iny
                Y++;
                setZNFlags(Y);

            }
            case 0x88 -> { // dey
                Y--;
                setZNFlags(Y);

            }
            case 0xca -> { // dex
                X--;
                setZNFlags(X);
            }
            case 0xc6 -> { // dec zp
                int addr = pc1();
                byte value = (byte) peek1(addr);
                value--;
                poke1(addr, Byte.toUnsignedInt(value));
                setZNFlags(value);
            }
            case 0xd6 -> { // dec zp, x
                int addr = pc1() + Byte.toUnsignedInt(X);
                byte value = (byte) peek1(addr);
                value--;
                poke1(addr, Byte.toUnsignedInt(value));
                setZNFlags(value);
            }
            case 0xce -> { // dec abs
                int addr = pc2();
                byte value = (byte) peek1(addr);
                value--;
                poke1(addr, Byte.toUnsignedInt(value));
                setZNFlags(value);
            }
            case 0xde -> { // dec abs, x
                int addr = pc2() + Byte.toUnsignedInt(X);
                byte value = (byte) peek1(addr);
                value--;
                poke1(addr, Byte.toUnsignedInt(value));
                setZNFlags(value);
            }
            case 0xe6 -> { // inc zp
                int addr = pc1();
                byte value = (byte) peek1(addr);
                value++;
                poke1(addr, Byte.toUnsignedInt(value));
                setZNFlags(value);
            }
            case 0xf6 -> { // inc zp, x
                int addr = pc1() + Byte.toUnsignedInt(X);
                byte value = (byte) peek1(addr);
                value++;
                poke1(addr, Byte.toUnsignedInt(value));
                setZNFlags(value);
            }
            case 0xee -> { // inc abs
                int addr = pc2();
                byte value = (byte) peek1(addr);
                value++;
                poke1(addr, Byte.toUnsignedInt(value));
                setZNFlags(value);
            }
            case 0xfe -> { // inc abs, x
                int addr = pc2() + Byte.toUnsignedInt(X);
                byte value = (byte) peek1(addr);
                value++;
                poke1(addr, Byte.toUnsignedInt(value));
                setZNFlags(value);
                ///// BRANCH /////
            }
            case 0xf0 -> { // beq
                if (FlagZ) {
                    byte offset = (byte) pc1();
                    PC += offset;
                } else {
                    pc1();
                }
            }
            case 0xd0 -> { // bne
                if (!FlagZ) {
                    byte offset = (byte)pc1();
                    PC += offset;
                } else {
                    pc1();
                }
            }
            case 0xb0 -> { // bcs
                if (FlagC) {
                    byte offset = (byte) pc1();
                    PC += offset;
                } else {
                    pc1();
                }
            }
            case 0x90 -> { // bcc
                if (!FlagC) {
                    byte offset = (byte) pc1();
                    PC += offset;
                } else {
                    pc1();
                }
            }
            case 0x30 -> { // bmi
                if (FlagN) {
                    byte offset = (byte) pc1();
                    PC += offset;
                } else {
                    pc1();
                }
            }
            case 0x10 -> { // bpl
                if (!FlagN) {
                    byte offset = (byte) pc1();
                    PC += offset;
                } else {
                    pc1();
                }
            }
            case 0x50 -> { // bvc
                if (!FlagV) {
                    byte offset = (byte) pc1();
                    PC += offset;
                } else {
                    pc1();
                }
            }
            case 0x70 -> { // bvs
                if (FlagV) {
                    byte offset = (byte) pc1();
                    PC += offset;
                } else {
                    pc1();
                }
                ///// FLAG MODIFIERS /////
            }
            case 0x18 -> { // clc
                FlagC = false;

            }
            case 0x38 -> { // sec
                FlagC = true;

            }
            case 0xd8 -> { // cld
                FlagD = false;

            }
            case 0xf8 -> { // sed
                FlagD = true;

            }
            case 0x58 -> { // cli
                FlagI = false;

            }
            case 0x78 -> { // sei
                FlagI = true;

            }
            case 0xb8 -> { // clv
                FlagV = false;

                ///// ARITHMETIC /////
            }
            case 0x69 -> { // adc #
                ADC((byte) pc1());

            }
            case 0x65 -> { // adc zp
                ADC((byte) peek1(pc1()));

            }
            case 0x75 -> { // adc zp, x
                ADC((byte) peek1(pc1() + Byte.toUnsignedInt(X)));

            }
            case 0x6d -> { // adc abs
                ADC((byte) peek1(pc2()));

            }
            case 0x7d -> { // adc abs, x
                ADC((byte) peek1(pc2() + Byte.toUnsignedInt(X)));

            }
            case 0x79 -> { // adc abs, y
                ADC((byte) peek1(pc2() + Byte.toUnsignedInt(Y)));

            }
            case 0x61 -> { // adc (ind, x)
                ADC((byte) peek1(peek2(pc1() + Byte.toUnsignedInt(X))));

            }
            case 0x71 -> { // adc (ind), y
                ADC((byte) peek1(peek2(pc1()) + Byte.toUnsignedInt(Y)));

            }
            case 0xe9 -> { // sbc #
                SBC((byte) pc1());

            }
            case 0xed -> { // sbc abs
                SBC((byte) peek1(pc2()));

            }
            case 0xe5 -> { // sbc zp
                SBC((byte) peek1(pc1()));

            }
            case 0xf5 -> { // sbc zp, x
                SBC((byte) peek1(pc1() + Byte.toUnsignedInt(X)));

            }
            case 0xfd -> { // sbc abs, x
                SBC((byte) peek1(pc2() + Byte.toUnsignedInt(X)));

            }
            case 0xf9 -> { // sbc abs, y
                SBC((byte) peek1(pc2() + Byte.toUnsignedInt(Y)));

            }
            case 0xe1 -> { // sbc (ind, x)
                SBC((byte) peek1(peek2(pc1() + Byte.toUnsignedInt(X))));

            }
            case 0xf1 -> { // sbc (ind), y
                SBC((byte) peek1(peek2(pc1()) + Byte.toUnsignedInt(Y)));

                ///// COMPARISON /////
            }
            case 0xc9 -> { // cmp #
                CMP(A, (byte) pc1());

            }
            case 0xc5 -> { // cmp zp
                CMP(A, (byte) peek1(pc1()));

            }
            case 0xd5 -> { // cmp zp, x
                CMP(A, (byte) peek1(pc1() + Byte.toUnsignedInt(X)));

            }
            case 0xcd -> { // cmp abs
                CMP(A, (byte) peek1(pc2()));

            }
            case 0xdd -> { // cmp abs, x
                CMP(A, (byte) peek1(pc2() + Byte.toUnsignedInt(X)));

            }
            case 0xd9 -> { // cmp abs, y
                CMP(A, (byte) peek1(pc2() + Byte.toUnsignedInt(Y)));

            }
            case 0xc1 -> { // cmp (ind, x)
                CMP(A, (byte) peek1(peek2(pc1() + Byte.toUnsignedInt(X))));

            }
            case 0xd1 -> { // cmp (ind), y
                CMP(A, (byte) peek1(peek2(pc1()) + Byte.toUnsignedInt(Y)));


            }
            case 0xe0 -> { // cpx #
                CMP(X, (byte) pc1());

            }
            case 0xc0 -> { // cpy #
                CMP(Y, (byte) pc1());

            }
            case 0xe4 -> { // cpx zp
                CMP(X, (byte) peek1(pc1()));

            }
            case 0xc4 -> { // cpy zp
                CMP(Y, (byte) peek1(pc1()));

            }
            case 0xec -> { // cpx abs
                CMP(X, (byte) peek1(pc2()));

            }
            case 0xcc -> { // cpy abs
                CMP(Y, (byte) peek1(pc2()));

                ///// SHIFT /////
            }
            case 0x0a -> { // asl a
                A = ASL(A);

            }
            case 0x06 -> { // asl zp
                A = ASL((byte) peek1(pc1()));

            }
            case 0x16 -> { // asl zp, x
                A = ASL((byte) peek1(pc1() + Byte.toUnsignedInt(X)));

            }
            case 0x0e -> { // asl abs
                A = ASL((byte) peek1(pc2()));

            }
            case 0x1e -> { // asl abs, x
                A = ASL((byte) peek1(pc2() + Byte.toUnsignedInt(X)));
            }
            case 0x4a -> { // lsr a
                A = LSR(A);
            }
            case 0x46 -> { // lsr zp
                A = LSR((byte) peek1(pc1()));
            }
            case 0x56 -> { // lsr zp, x
                A = LSR((byte) peek1(pc1() + Byte.toUnsignedInt(X)));
            }
            case 0x4e -> { // lsr abs
                A = LSR((byte) peek1(pc2()));
            }
            case 0x5e -> { // lsr abs, x
                A = LSR((byte) peek1(pc2() + Byte.toUnsignedInt(X)));
            }
            case 0x2a -> { // rol a
                A = ROL(A);
            }
            case 0x26 -> { // rol zp
                A = ROL((byte) peek1(pc1()));
            }
            case 0x36 -> { // rol zp, x
                A = ROL((byte) peek1(pc1() + Byte.toUnsignedInt(X)));
            }
            case 0x2e -> { // rol abs
                A = ROL((byte) peek1(pc2()));
            }
            case 0x3e -> { // rol abs, x
                A = ROL((byte) peek1(pc2() + Byte.toUnsignedInt(X)));
            }
            case 0x6a -> { // ror a
                A = ROR(A);
            }
            case 0x66 -> { // ror zp
                A = ROR((byte) peek1(pc1()));
            }
            case 0x76 -> { // ror zp, x
                A = ROR((byte) peek1(pc1() + Byte.toUnsignedInt(X)));
            }
            case 0x6e -> { // ror abs
                A = ROR((byte) peek1(pc2()));
            }
            case 0x7e -> { // ror abs, x
                A = ROR((byte) peek1(pc2() + Byte.toUnsignedInt(X)));
                ///// EXTRA INSTRUCTIONS /////
            }
            case 0xea -> {} // nop
            case 0x00 -> { // brk
                push2(Short.toUnsignedInt(PC));
                push1(packFlags());
                PC = BrkAddr;
                FlagI = true;
            }
            case 0x40 -> { // rti
                setFlags(pop1());
                PC = (short) pop2();
            }

            /*
                =====NOTICE=====
                These instructions did not exist on the original 6502.
                The wai & stp instructions come from the 65C02.
                The mmu & mas instructions are for Ravenstone and never existed on any real 6502.
             */
            case 0xef -> { // *mmu
                var data = pc1();
                switch (data) {
                    case 0x00 -> { // Switch Bus ID
                        BusOffset = Byte.toUnsignedInt(A);
                    }
                    case 0x80 -> { // Get Bus ID
                        A = (byte) BusOffset;
                    }
                    case 0x01 -> { // Set Break Vector Address
                        BrkAddr = (short) peek2(Byte.toUnsignedInt(A));
                    }
                    case 0x81 -> { // Get Break Vector Address
                        poke1(Byte.toUnsignedInt(A), BrkAddr & 0xFF);
                        poke1(Byte.toUnsignedInt(A) + 1, ((BrkAddr & 0xFF00) >>> 8));
                    }
                    default -> {
                        Ravenstone.LOGGER.error("MMU Error!");
                    }
                }
                break;
            }
            case 0xcb -> { // *wai
                Wait = true;
            }
            case 0xdb -> { // *stp (Halts. Has unintended side-effects)
                Stop = true;
                Host.explode();
            }
            case 0xdf -> { // *mas (Move Accumulator to Upper 8-bits of Stack Pointer)
                SP &= 0x00FF;
                SP |= (short) ((Byte.toUnsignedInt(A) << 8));
            }
            default -> {
                Stop = true;
                Ravenstone.LOGGER.error("Invalid Opcode: 0x{}: 0x{}", Integer.toHexString(Short.toUnsignedInt(PC)-1), Integer.toHexString(insn));
            }
        }
    }

    private int pc1() {PC += 1; return peek1(Short.toUnsignedInt(PC) - 1);}
    private int pc2() {PC += 2; return peek1(Short.toUnsignedInt(PC) - 2) | (peek1(Short.toUnsignedInt(PC) - 1) << 8);}

    private int peek1(int addr) {
        var uaddr = addr & 0xFFFF;
        return Byte.toUnsignedInt(Host.memRead((short)uaddr));
    }
    private int peek2(int addr) {return peek1(addr) | (peek1(addr + 1) << 8);}

    private void poke1(int addr, int b) {
        var uaddr = addr & 0xFFFF;
        Host.memStore((short)uaddr, (byte)(b & 0xFF));
    }
    //private void poke2(int addr, int s) {poke1(addr, s); poke1(addr + 1, s >>> 8);}

    private void push1(int b) {poke1(SP, b); SP -= 1;}
    private void push2(int s) {push1(s >> 8); push1(s);}
    private int pop1() {SP += 1; return peek1(SP);}
    private int pop2() {return pop1() | (pop1() << 8);}

    private int toBCD(int s) {return Byte.toUnsignedInt((byte)Integer.parseInt(Integer.toString(s),16));}
    private int fromBCD(int s) {
        try {
            return Byte.toUnsignedInt((byte)Integer.parseInt(Integer.toString(s,16)));
        } catch(NumberFormatException e) {
            Ravenstone.LOGGER.error("Number Format Exception!");
            return 0;
        }
    }

    private void setZNFlags(byte val) {
        FlagZ = (Byte.toUnsignedInt(val) == 0);
        FlagN = ((Byte.toUnsignedInt(val) & 0x80) != 0);
    }

    private int packFlags() {
        int c = FlagC ? (1 << 0) : 0;
        int z = FlagZ ? (1 << 1) : 0;
        int i = FlagI ? (1 << 2) : 0;
        int d = FlagD ? (1 << 3) : 0;
        int r1 = (1 << 4);
        int r2 = (1 << 5);
        int v = FlagV ? (1 << 6) : 0;
        int n = FlagN ? (1 << 7) : 0;
        return c|z|i|d|r1|r2|v|n;
    }

    private void setFlags(int b) {
        FlagC = (b & (1 << 0)) != 0;
        FlagZ = (b & (1 << 1)) != 0;
        FlagI = (b & (1 << 2)) != 0;
        FlagD = (b & (1 << 3)) != 0;
        FlagV = (b & (1 << 6)) != 0;
        FlagN = (b & (1 << 7)) != 0;
    }

    private void ADC(byte op) {
        boolean areSignBitsSame = (((Byte.toUnsignedInt(A) ^ Byte.toUnsignedInt(op)) & 0x80) != 0);
        int sum = (FlagD?fromBCD(Byte.toUnsignedInt(A)):Byte.toUnsignedInt(A));
        sum += (FlagD?fromBCD(Byte.toUnsignedInt(op)):Byte.toUnsignedInt(op));
        sum += (FlagC?1:0);
        A = (FlagD?((byte)toBCD(sum & 0xFF)):((byte)(sum & 0xFF)));
        setZNFlags(A);
        FlagC = (sum > 0xFF);
        FlagV = (areSignBitsSame && (((A ^ op) & 0x80) != 0));
    }
    private void SBC(byte op) {
        ADC((byte)(~op));
    }
    private void CMP(byte a, byte b) {
        FlagN = ((byte)(a - b) < 0);
        FlagZ = (a == b);
        FlagC = (a >= b);
    }
    private byte ASL(byte a) {
        FlagC = (a < 0);
        int result = Byte.toUnsignedInt(a) << 1;
        setZNFlags((byte)result);
        return (byte)result;
    }
    private byte LSR(byte a) {
        FlagC = ((Byte.toUnsignedInt(a) & 1) > 0);
        int result = Byte.toUnsignedInt(a) >>> 1;
        setZNFlags((byte)result);
        return (byte)result;
    }
    private byte ROL(byte a) {
        byte newbitZ = (byte)(FlagC?1:0);
        FlagC = (a < 0);
        int result = Byte.toUnsignedInt(a) << 1;
        result |= Byte.toUnsignedInt(newbitZ);
        setZNFlags((byte)result);
        return (byte)result;
    }
    private byte ROR(byte a) {
        boolean oldbitZ = ((Byte.toUnsignedInt(a) & 1) > 0);
        int result = Byte.toUnsignedInt(a) >> 1;
        result |= (FlagC?0x80:0);
        FlagC = oldbitZ;
        setZNFlags((byte)result);
        return (byte)result;
    }
}
