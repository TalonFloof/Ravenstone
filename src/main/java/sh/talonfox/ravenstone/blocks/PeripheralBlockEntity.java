package sh.talonfox.ravenstone.blocks;

public abstract class PeripheralBlockEntity {
    public byte readData(byte at) {return 0;}
    public void storeData(byte at, byte data) {}
}
