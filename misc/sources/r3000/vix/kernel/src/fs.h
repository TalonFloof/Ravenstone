#pragma once

typedef struct {
    unsigned char userID;
    char name[8];
    char type[3];
    unsigned short extent;
    unsigned char records;
    unsigned char reserved;
    unsigned char allocation[16];
} File;

typedef struct {
    File f;
    unsigned char curRecord;
    unsigned int randomRecord;
    unsigned int reserved;
} FileControlBlock;

#ifndef FS_IMPL
extern unsigned short disks[8];
extern unsigned char diskTimeout[8];
#endif