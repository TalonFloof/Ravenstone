package sh.talonfox.ravynstone.processor;

public class Processor {
    private short A = 0;
    private short B = 0;
    private short D = 0;
    private short I = 0;
    private short X = 0;
    private short Y = 0;
    private short PC = 0x400;
    private short SP = 0x1ff;
    private short RP = 0x2ff;
    private Boolean flagC = false;
    private Boolean flagZ = false;
    private Boolean flagID = false;
    private Boolean flagD = false;
    private Boolean flagBRK = false;
    private Boolean flagO = false;
    private Boolean flagN = false;
    private Boolean flagE = true;
    private Boolean flagM = true;
    private Boolean flagX = true;
    private short resetAddr = 0x0400;
    private short brkAddr = 0x2000;

    public Processor() {

    }
}
