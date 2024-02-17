#include "string.h"

static int currentBusID = 0;

static void BindToDevice(unsigned char busID) {
    if(currentBusID != busID) {
        *((unsigned char*)0xa1000000) = busID;
        currentBusID = busID;
    }
}

unsigned char TeletypeRawIn(int busID) {
    BindToDevice(busID);
    return *((unsigned char*)0xa2000004);
}

void TeletypeRawOut(int busID, unsigned char c) {
    BindToDevice(busID);
    if(c == 8) {
        *((unsigned char*)0xa2000001) -= 1;
        *((unsigned char*)0xa2000010+(*((unsigned char*)0xa2000001))) = ' ';
    } else if(c == 0xa) {
        if((*((unsigned char*)0xa2000002)) < 24) {
            (*((unsigned char*)0xa2000000))++;
            (*((unsigned char*)0xa2000002))++;
        } else {
            // Scroll

        }
        *((unsigned char*)0xa2000001) = 0;
    } else {
        *((unsigned char*)0xa2000010+(*((unsigned char*)0xa2000001))) = c;
        *((unsigned char*)0xa2000001) += 1;
    }
}

void TeletypeStringOut(int busID, const char* s) {
    while(*s != 0) {
        TeletypeRawOut(busID, (unsigned char)*s);
        s++;
    }
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

void WriteHDSector(int busID, int sector, void* data) {
    BindToDevice(busID);
    memcpy((void*)0xa2000000,data,128);
    *((int*)0xa2000084) = sector;
    *((volatile unsigned char*)0xa2000080) = 2;
    while(*((volatile unsigned char*)0xa2000080) != 0) {
        MultiYield();
    }
}