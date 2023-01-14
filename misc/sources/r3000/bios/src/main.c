extern void* __DATA_BEGIN__;
extern void* __DATA_END__;
extern void* __RODATA_END__;
extern void* __BSS_BEGIN__;
extern void* __BSS_END__;

void *memcpy(void *dest, const void *src, int count) {
    const char *sp = (char *)src;
    char *dp = (char *)dest;
    int i;
    for (i = count; i >= 4; i = count) {
        *((unsigned int*)dp) = *((unsigned int*)sp);
        sp = sp + 4;
        dp = dp + 4;
        count -= 4;
    }
    for (i = count; i > 0; i = count) {
        *(dp++) = *(sp++);
        count--;
    }
    return dest;
}

int strlen(const char* s) {
    int i = 0;
    while(s[i] != 0) {
        i++;
    }
    return i;
}

void BindToDevice(unsigned char busID) {
    *((unsigned char*)0xa1000000) = busID;
}

void ClearScreen() {
    *((unsigned char*)0xa2000000) = 0;
    *((unsigned char*)0xa2000001) = 0;
    *((unsigned char*)0xa2000002) = 0;
    *((unsigned char*)0xa2000008) = 32;
    *((unsigned char*)0xa200000a) = 0;
    *((unsigned char*)0xa200000b) = 0;
    *((unsigned char*)0xa200000c) = 80;
    *((unsigned char*)0xa200000d) = 50;
    *((unsigned char*)0xa2000007) = 1;
    asm volatile("break"); /* Wait 1 Tick */
}

void Println(const char* c) {
    memcpy(((unsigned char*)0xa2000010),c,strlen(c));
    (*((unsigned char*)0xa2000000))++;
    *((unsigned char*)0xa2000002) = *((unsigned char*)0xa2000000);
}

void Beep() {
    *((unsigned int*)0xa1000004) = 0;
}

void main() {
    BindToDevice(1);
    ClearScreen();
    Println("CPU...OK");
    Println("CACHE...ABSENT");
    /* Copy Writable Data to RAM */
    memcpy(&__DATA_BEGIN__,&__RODATA_END__,(int)(((unsigned int)&__DATA_END__)-((unsigned int)&__DATA_BEGIN__)));
    for(int i=0; i < 60; i++) {asm volatile("break");}
    Println("RAM...1024K OK");
    Println("SYSTEM IS OK");
    Beep();
    Println("");
    Println("Loading bootstrapper from Diskette Drive...");

    for(;;) {}
}