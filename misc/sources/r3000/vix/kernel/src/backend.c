#include "string.h"
#include "fs.h"

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
    *((unsigned char*)0xa2000080) = c;
}

void TeletypeStringOut(int busID, const char* s) {
    while(*s != 0) {
        TeletypeRawOut(busID, (unsigned char)*s);
        s++;
    }
}

int SendDisketteCommand(int diskID, int track, int sector, int cmd) {
    BindToDevice(disks[diskID]);
    *((unsigned char*)0xa2000081) = track;
    *((unsigned char*)0xa2000082) = sector;
    if(diskTimeout[diskID] == 0 && cmd != 0x20) {
        *((volatile unsigned char*)0xa2000080) = 0x21;
        while(((*((volatile unsigned char*)0xa2000080)) & 0x1) != 0) {MultiYield();}
        diskTimeout[diskID] = 20;
    } else if(cmd != 0x20) {
        diskTimeout[diskID] = 20;
    }
    *((volatile unsigned char*)0xa2000080) = cmd;
    if(cmd != 0x20) {
        while(((*((volatile unsigned char*)0xa2000080)) & 0x1) != 0) {MultiYield();}
    }
    return ((int)(*((volatile unsigned char*)0xa2000080)));
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