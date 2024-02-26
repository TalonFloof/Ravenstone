package sh.talonfox.ravenstone.processor;

import com.codingrodent.microprocessor.IBaseDevice;
import com.codingrodent.microprocessor.IMemory;
import com.codingrodent.microprocessor.Z80.CPUConstants;
import com.codingrodent.microprocessor.Z80.Z80Core;
import net.minecraft.item.ItemStack;
import sh.talonfox.ravenstone.Ravenstone;
import sh.talonfox.ravenstone.ResourceRegister;

public class Z80 implements Processor {
    public boolean wait = false;
    public ProcessorHost host = null;
    public int busOffset = 0;
    public Z80Core core;

    public class Memory implements IMemory {
        public int readByte(int address) {
            int a = address & 0xffff;
            if(a >= 0xff00) {
                return Byte.toUnsignedInt(Z80.this.host.busRead((byte) Z80.this.busOffset, (short) (a - 0xff00L)));
            } else if(a >= 0xf000) {
                return Byte.toUnsignedInt(ResourceRegister.ROMS.get("z80")[a - 0xf000]);
            } else {
                return Byte.toUnsignedInt(Z80.this.host.memRead(a & 0xffff));
            }
        }
        public int readWord(int address) {
            int a = address & 0xffff;
            return this.readByte(a) | (this.readByte(a+1) << 8);
        }
        public void writeByte(int address, int data) {
            int a = address & 0xffff;
            if(a >= 0xff00) {
                Z80.this.host.busWrite((byte)Z80.this.busOffset,(short)(a - 0xff00),(byte)(data & 0xFF));
            } else if(a < 0xf000) {
                Z80.this.host.memStore(a, (byte) (data & 0xFF));
            }
        }
        public void writeWord(int address, int data) {
            int a = address & 0xffff;
            Z80.this.host.memStore(a,(byte)(data & 0xFF));
            Z80.this.host.memStore(a+1,(byte)((data & 0xFF00) >> 8));
        }
    }
    public class IO implements IBaseDevice {
        public int IORead(int address) {
            int a = address & 0xff;
            if(a == 0) {
                return Z80.this.busOffset;
            } else if(a == 1) {
                return Z80.this.host.isPeripheralConnected() ? 0 : 0x80;
            }
            return 0;
        }
        public void IOWrite(int address, int data) {
            int a = address & 0xff;
            if(a == 0) {
                Ravenstone.LOGGER.info("Bus Switch: 0x"+Integer.toHexString(data));
                Z80.this.busOffset = data;
                Z80.this.host.invalidatePeripheral();
            } else if(a == 2) {
                Z80.this.host.beep();
            } else if(a == 3) {
                Z80.this.setWait(true);
            }
        }
    }

    public Z80() {
        core = new Z80Core(new Memory(),new IO());
    }

    @Override
    public boolean isWaiting() {
        return wait;
    }

    @Override
    public void setWait(boolean flag) {
        wait = flag;
    }

    @Override
    public int insnPerSecond() {
        return 1000000;
    }

    @Override
    public void reset() {
        core.setResetAddress(0xf000);
        core.reset();
    }

    @Override
    public void next(ProcessorHost host) {
        this.host = host;
        this.wait = false;
        core.executeOneInstruction();
    }

    @Override
    public void saveNBT(ItemStack stack) {
        stack.getOrCreateNbt().putBoolean("Wait",wait);
        stack.getOrCreateNbt().putInt("BusOffset",busOffset);
        stack.getOrCreateNbt().putInt("A",core.getRegisterValue(CPUConstants.RegisterNames.A));
        stack.getOrCreateNbt().putInt("BC",core.getRegisterValue(CPUConstants.RegisterNames.BC));
        stack.getOrCreateNbt().putInt("DE",core.getRegisterValue(CPUConstants.RegisterNames.DE));
        stack.getOrCreateNbt().putInt("HL",core.getRegisterValue(CPUConstants.RegisterNames.HL));
        stack.getOrCreateNbt().putInt("F",core.getRegisterValue(CPUConstants.RegisterNames.F));
        stack.getOrCreateNbt().putInt("IX",core.getRegisterValue(CPUConstants.RegisterNames.IX));
        stack.getOrCreateNbt().putInt("IY",core.getRegisterValue(CPUConstants.RegisterNames.IY));
        stack.getOrCreateNbt().putInt("I",core.getRegisterValue(CPUConstants.RegisterNames.I));
        stack.getOrCreateNbt().putInt("R",core.getRegisterValue(CPUConstants.RegisterNames.R));
        stack.getOrCreateNbt().putInt("SP",core.getRegisterValue(CPUConstants.RegisterNames.SP));
        stack.getOrCreateNbt().putInt("PC",core.getRegisterValue(CPUConstants.RegisterNames.PC));
        stack.getOrCreateNbt().putInt("AAlt",core.getRegisterValue(CPUConstants.RegisterNames.A_ALT));
        stack.getOrCreateNbt().putInt("BCAlt",core.getRegisterValue(CPUConstants.RegisterNames.BC_ALT));
        stack.getOrCreateNbt().putInt("DEAlt",core.getRegisterValue(CPUConstants.RegisterNames.DE_ALT));
        stack.getOrCreateNbt().putInt("HLAlt",core.getRegisterValue(CPUConstants.RegisterNames.HL_ALT));
        stack.getOrCreateNbt().putInt("FAlt",core.getRegisterValue(CPUConstants.RegisterNames.F_ALT));
    }

    @Override
    public void loadNBT(ItemStack stack) {
        wait = stack.getOrCreateNbt().getBoolean("Wait");
        busOffset = stack.getOrCreateNbt().getInt("BusOffset");
        core.setRegisterValue(CPUConstants.RegisterNames.A,stack.getOrCreateNbt().getInt("A"));
        core.setRegisterValue(CPUConstants.RegisterNames.BC,stack.getOrCreateNbt().getInt("BC"));
        core.setRegisterValue(CPUConstants.RegisterNames.DE,stack.getOrCreateNbt().getInt("DE"));
        core.setRegisterValue(CPUConstants.RegisterNames.HL,stack.getOrCreateNbt().getInt("HL"));
        core.setRegisterValue(CPUConstants.RegisterNames.F,stack.getOrCreateNbt().getInt("F"));
        core.setRegisterValue(CPUConstants.RegisterNames.IX,stack.getOrCreateNbt().getInt("IX"));
        core.setRegisterValue(CPUConstants.RegisterNames.IY,stack.getOrCreateNbt().getInt("IY"));
        core.setRegisterValue(CPUConstants.RegisterNames.I,stack.getOrCreateNbt().getInt("I"));
        core.setRegisterValue(CPUConstants.RegisterNames.R,stack.getOrCreateNbt().getInt("R"));
        core.setRegisterValue(CPUConstants.RegisterNames.SP,stack.getOrCreateNbt().getInt("SP"));
        core.setRegisterValue(CPUConstants.RegisterNames.PC,stack.getOrCreateNbt().getInt("PC"));
        core.setRegisterValue(CPUConstants.RegisterNames.A_ALT,stack.getOrCreateNbt().getInt("AAlt"));
        core.setRegisterValue(CPUConstants.RegisterNames.BC_ALT,stack.getOrCreateNbt().getInt("BCAlt"));
        core.setRegisterValue(CPUConstants.RegisterNames.DE_ALT,stack.getOrCreateNbt().getInt("DEAlt"));
        core.setRegisterValue(CPUConstants.RegisterNames.HL_ALT,stack.getOrCreateNbt().getInt("HLAlt"));
        core.setRegisterValue(CPUConstants.RegisterNames.F_ALT,stack.getOrCreateNbt().getInt("FAlt"));
    }
}
