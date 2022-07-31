package sh.talonfox.ravynstone.processor;

public class Processor {
    public ProcessorHost Host;
    private short A = 0;
    private short B = 0;
    private short D = 0;
    private short I = 0;
    private short X = 0;
    private short Y = 0;
    private short PC = 0x400;
    private short SP = 0x1ff;
    private short RP = 0x2ff;
    private Boolean FlagC = false;
    private Boolean FlagZ = false;
    private Boolean FlagID = false;
    private Boolean FlagBRK = false;
    private Boolean FlagO = false;
    private Boolean FlagN = false;
    private Boolean FlagE = true;
    private Boolean FlagM = true;
    private Boolean FlagX = true;
    private short ResetAddr = 0x0400;
    private short BrkAddr = 0x2000;

    public Processor(ProcessorHost host) {
        Host = host;
    }
}
