package sh.talonfox.ravenstone.processor;

public interface ProcessorHost {
    byte targetBus = 0;
    Boolean isBusConnected = false;


    void resetBusState();
    byte memRead(short at);
    void memStore(short at, byte data);

    void explode();
}
