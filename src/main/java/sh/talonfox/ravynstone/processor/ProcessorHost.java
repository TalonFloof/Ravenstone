package sh.talonfox.ravynstone.processor;

public interface ProcessorHost {
    byte targetBus = 0;
    Boolean isBusConnected = false;


    void resetBusState();
    byte memRead(short at);
    void memStore(short at, byte data);
}
