void main() {
    for(;;) {}
}

void BindToDevice(unsigned char busID) {
    *((unsigned char*)0xa1000000) = busID;
}

void Printl(const char* c) {

}