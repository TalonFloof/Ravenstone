static int currentBusID = 0;

static void BindToDevice(unsigned char busID) {
    if(currentBusID != busID) {
        *((unsigned char*)0xa1000000) = busID;
        currentBusID = busID;
    }
}

unsigned char TeletypeRawIn(int busID) {
    BindToDevice(devID);
    int c = 0;
    while(c == 0) {
        c = *((unsigned char*)0xa2000004);
    }
    return c;
}