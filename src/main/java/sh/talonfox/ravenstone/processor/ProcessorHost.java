package sh.talonfox.ravenstone.processor;

public interface ProcessorHost {
    byte busRead(byte id, short at);
    void busWrite(byte id, short at, byte val);
    byte memRead(long at);
    void memStore(long at, byte data);
    void invalidatePeripheral();

    void beep();

    void stop();
}
