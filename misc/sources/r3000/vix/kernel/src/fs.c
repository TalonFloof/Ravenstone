#define FS_IMPL
#include "fs.h"
#include "backend.h"

unsigned short disks[8] = {0,0,0,0,0,0,0,0};
unsigned char diskTimeout[8] = {0,0,0,0,0,0,0,0};

void FSGetDirectory(int disk, void* buf, int record) {
    if(disks[disk] & 0x100) {
    } else {

    }
}