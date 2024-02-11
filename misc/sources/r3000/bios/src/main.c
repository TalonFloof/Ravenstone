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
    int i;
    for(int i=0;i<4;i++) {asm volatile("break");}
}

int SendDisketteCommand(int cmd) {
    unsigned char* ptr = (unsigned char*)0xa2000080;
    *ptr = cmd;
    asm volatile("nop");
    while(((*ptr) & 0x1) != 0) {asm volatile("break");}
    return ((int)(*ptr));
}

void SetDisketteTrack(unsigned char num) {
    *((unsigned char*)0xa2000081) = num;
}

void SetDisketteSector(unsigned char num) {
    *((unsigned char*)0xa2000082) = num;
}

void main() {
    int i;
    BindToDevice(1);
    ClearScreen();
    Println("CPU...OK");
    /* Copy Writable Data to RAM */
    memcpy(&__DATA_BEGIN__,&__RODATA_END__,(int)(((unsigned int)&__DATA_END__)-((unsigned int)&__DATA_BEGIN__)));
    Println("RAM...1024K OK");
    Println("SYSTEM IS OK");
    Beep();
    Println("");
    for(;;) {
        for(;;) {
            Println("Loading bootstrapper from Diskette Drive...");
            BindToDevice(2);
            /* Engage Head */
            SetDisketteTrack(0);
            SetDisketteSector(0);
            if((*((unsigned char*)0xa1000001)) & 0x80) {
                Beep();
                BindToDevice(1);
                Println("");
                Println("Diskette Drive Communication Failure");
                Println("To reattempt, press any key.");
                while((*((unsigned char*)0xa2000004)) == 0) {asm volatile("break");}
                ClearScreen();
                continue;
            }
            if((SendDisketteCommand(0x21) & 0x8) != 0) {
                Beep();
                BindToDevice(1);
                Println("");
                Println("No Diskette was inserted into the diskette drive.");
                Println("Insert a bootable disk, then press any key.");
                while((*((unsigned char*)0xa2000004)) == 0) {asm volatile("break");}
                ClearScreen();
            } else {
                break;
            }
        }
        SetDisketteTrack(0);
        SetDisketteSector(0);
        SendDisketteCommand(0x1); /* Seek to Track 0 */
        SendDisketteCommand(0x80); /* Read Sector 0 on Track 0 */
        int hasData = 0;
        for(i=0;i<128;i++) {
            if(*((unsigned char*)(0xa2000000+i))) {
                hasData = 1;
                break;
            }
        }
        if(hasData)
            break;
        SendDisketteCommand(0x20);
        Beep();
        BindToDevice(1);
        Println("");
        Println("This diskette contains no bootable data.");
        Println("Insert a bootable disk, then press any key.");
        while((*((unsigned char*)0xa2000004)) == 0) {asm volatile("break");}
        ClearScreen();
    }
    for(i=0;i < 32;i++) {
        SetDisketteSector(i);
        SendDisketteCommand(0x80);
        memcpy((void*)(0x800fe000+(128*i)),(void*)0xa2000000,128);
    }
    SendDisketteCommand(0x20);
    BindToDevice(0);
}