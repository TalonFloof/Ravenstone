static int currentBusID = 0;

static void BindToDevice(unsigned char busID) {
    if(currentBusID != busID) {
        *((unsigned char*)0xa1000000) = busID;
        currentBusID = busID;
    }
}

unsigned char TeletypeRawIn(int busID) {
    BindToDevice(devID);
    *((unsigned char*)0xa2000000)
}