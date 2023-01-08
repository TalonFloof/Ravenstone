package sh.talonfox.ravenstone.processor;

import net.minecraft.item.ItemStack;
import sh.talonfox.ravenstone.Ravenstone;
import sh.talonfox.ravenstone.ResourceRegister;

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
    public int PC = 0;
    public int BusOffset = 0;
    public int StallCycles = 0;

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
        return 4000000; // 4 MHz
    }

    @Override
    public void reset() {
        Wait = false;
        PC = 0xbfc00000;
        StallCycles = 0;
        BusOffset = 0;
    }

    @Override
    public void next(ProcessorHost host) {
        Host = host;
        if(StallCycles > 0 && Wait) {
            StallCycles = 0;
        }
        Wait = false;
        if(StallCycles > 0) {
            StallCycles -= 1;
            return;
        }
        var insn = Integer.toUnsignedLong(pc4());
        /*case 0x1f -> { // mmu
            Is16Bit = false;
            BusOffset = pop();
            Is16Bit = (insn & 0x80) != 0;
            Host.invalidatePeripheral();
        }*/
    }

    private int pc4() {PC += 4; return peek4(Integer.toUnsignedLong(PC));}

    private int peek1(long addr) {
        var uaddr = addr & 0xFFFFFFFFL;
        if(uaddr >= 0x80000000L && uaddr <= 0x80ffffffL) {
            return Byte.toUnsignedInt(Host.memRead(uaddr - 0x80000000L));
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
    private int peek2(long addr) {return peek1(addr) | (peek1(addr + 1) << 8);}
    private int peek4(long addr) {return peek2(addr) | (peek2(addr + 2) << 16);}

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
        stack.getOrCreateNbt().putInt("BusOffset",BusOffset);
    }

    @Override
    public void loadNBT(ItemStack stack) {
        Wait = stack.getOrCreateNbt().getBoolean("Wait");
        PC = stack.getOrCreateNbt().getInt("PC");
        BusOffset = stack.getOrCreateNbt().getInt("BusOffset");
    }
}
