package sh.talonfox.ravynstone.processor;

public interface ProcessorHost {
    /*
    var targetBus: Byte
  val isBusConnected: Boolean
  fun bus(): Device?

  fun resetBusState()
  var allowWrite: Boolean

  var writePos: Short

  fun memRead(at: Short): Byte
  fun memStore(at: Short, data: Byte)
     */
    byte targetBus = 0;
    Boolean isBusConnected = false;


    void resetBusState();
    byte memRead(short at);
    void memStore(short at, byte data);
}
