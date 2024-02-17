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

void reverse(char *str, int length) {
  char *end = str + length - 1;
  int i;

  for (i = 0; i < length / 2; i++) {
    char c = *end;
    *end = *str;
    *str = c;

    str++;
    end--;
  }
}

char *itoa(unsigned int num, char *str, int base) {
  int i = 0;

  if (num == 0) {
    str[i++] = '0';
    str[i] = '\0';
    return str;
  }

  while (num != 0) {
    int rem = num % base;
    str[i++] = (rem > 9) ? (rem - 10) + 'a' : rem + '0';
    num = num / base;
  }

  str[i] = '\0';

  reverse(str, i);

  return str;
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
    BindToDevice(1);
    Println("Vix Bootstrap");
    Println("  KERNEL-4K");
    BindToDevice(2);
    SendDisketteCommand(0x21);
    SendDisketteCommand(0x01);
    int a = 0x80000000;
    for(int i=1; i < 8; i++) {
        SetDisketteTrack(i-1);
        SetDisketteSector(i);
        SendDisketteCommand(0x10);
        for(int j=0; j < 32; j++) {
            SetDisketteTrack(i);
            SetDisketteSector(j);
            SendDisketteCommand(0x80);
            memcpy((void*)a,(void*)0xa2000000,128);
            a += 128;
        }
        BindToDevice(1);
        (*((unsigned char*)0xa2000000))--;
        char c[16];
        memcpy(&c,"  KERNEL-",9);
        itoa((i+1)*4,((char*)&c)+9,10);
        memcpy(((char*)&c)+strlen((char*)&c),"K",2);
        Println((char*)&c);
        BindToDevice(2);
    }
    SendDisketteCommand(0x20);
    BindToDevice(1);
}