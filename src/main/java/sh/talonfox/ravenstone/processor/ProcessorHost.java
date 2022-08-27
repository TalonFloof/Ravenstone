package sh.talonfox.ravenstone.processor;

public interface ProcessorHost {
    byte busRead(byte id, byte at);
    void busWrite(byte id, byte at, byte val);
    byte memRead(int at);
    void memStore(int at, byte data);
    void invalidatePeripheral();

    void stop();
}
