package sh.talonfox.ravenstone.processor;

public interface ProcessorHost {
    byte memRead(short at);
    void memStore(short at, byte data);
    void explode();
    void invalidatePeripheral();
}
