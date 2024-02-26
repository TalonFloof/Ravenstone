#define USER_IMPL
#include "user.h"

User users[256];

void UserInit() {
    memcpy((void*)&users[0].name[0],"Admin\0\0",8);
    memset((void*)&users[0].password[0],0,8);
}

void UserSave(int drive) {
    for(int i=0; i < 32; i++) {
        WriteHDSector(drive,i,((void*)&users)+(i*128));
    }
}