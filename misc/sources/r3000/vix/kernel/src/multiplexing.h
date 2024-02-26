#pragma once
#include "user.h"
#include "setjmp.h"

typedef struct {
    char active;
    unsigned char terminalID;
    unsigned char curDisk;
    User* userInfo;
    jmp_buf context;
    unsigned char prompt[256];
    unsigned char stack[1024];
} TaskBlock;

__attribute__((noreturn)) void MultiInit();
void MultiShell();
void MultiYield();