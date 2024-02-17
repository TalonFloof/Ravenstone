#define MPLEX_IMPL
#include "multiplexing.h"
#include "string.h"
#include "backend.h"

TaskBlock tasks[16];
int currentTask;
char curNum;
char drive = 0;

__attribute__((noreturn)) void MultiInit() {
    tasks[0].active = 1;
    tasks[0].terminalID = 1;
    memcpy(&(tasks[0].userInfo.name),"Admin",6);
    tasks[0].userInfo.diskettes[0] = 2;
    tasks[0].userInfo.diskettes[1] = 3;
    tasks[0].programStart = 0;
    tasks[0].programLength = 0;
    for(int i=1; i < 16; i++) {
        tasks[i].active = 0;
    }
    currentTask = 0;
    curNum = 1;
    enterjmp(((void*)&tasks[0].stack)+(1024-4));
}

void MultiYield() {
    if(!setjmp(&tasks[currentTask].context)) {
        MultiSwitch();
    }
}

void MultiSwitch() {
    for(;;) {
        for(int i=1; i <= 16; i++) {
            int v = (currentTask+i)%16;
            if(tasks[v].active != 0 && tasks[v].active == curNum) {
                currentTask = v;
                tasks[v].active = (curNum%2)+1;
                longjmp(&tasks[currentTask].context,1);
                return;
            }
        }
        curNum = (curNum%2)+1;
        asm volatile ("break");
    }
}

unsigned char MultiTTYRawIn() {
    for(;;) {
        setjmp(&tasks[currentTask].context);
        unsigned char c = TeletypeRawIn(tasks[currentTask].terminalID);
        if(c != 0) {
            return c;
        }
        MultiSwitch();
    }
}

void MultiTTYPrompt() {
    tasks[currentTask].prompt[0] = 0;
    for(;;) {
        unsigned char c = MultiTTYRawIn();
        if(c == '\r') {
            TeletypeRawOut(tasks[currentTask].terminalID,'\n');
            return;
        }
        int len = strlen(&tasks[currentTask].prompt);
        if(c != 8) {
            TeletypeRawOut(tasks[currentTask].terminalID,c);
            tasks[currentTask].prompt[len] = c;
            tasks[currentTask].prompt[len+1] = 0;
        } else {
            if(len > 0) {
                TeletypeRawOut(tasks[currentTask].terminalID,c);
                tasks[currentTask].prompt[len-1] = 0;
            }
        }
    }
}

int MultiTTYPromptYN() {
    for(;;) {
        unsigned char c = MultiTTYRawIn();
        if(c == 'y' || c == 'Y') {
            TeletypeRawOut(tasks[currentTask].terminalID,c);
            TeletypeRawOut(tasks[currentTask].terminalID,'\n');
            return 1;
        } else if(c == 'n' || c == 'N') {
            TeletypeRawOut(tasks[currentTask].terminalID,c);
            TeletypeRawOut(tasks[currentTask].terminalID,'\n');
            return 0;
        }
    }
}

void MultiShell() {
    int terminal = tasks[currentTask].terminalID;
    if(currentTask == 0) {
        for(;;) {
            TeletypeStringOut(terminal,"VBC-> ");
            MultiTTYPrompt();
            char* bootType = strsplit((char*)&tasks[currentTask].prompt);
            char* driveIDRaw = strsplit((void*)0);
            drive = strtol(driveIDRaw,(void*)0,10);
            if(strcmp(bootType,"cold") == 0) {
                TeletypeStringOut(terminal,"WARNING! COLD BOOTS WILL DESTROY ALL DISK DATA\nONLY DO THIS IF YOU'RE INITIALIZING A NEW SYSTEM\nProceed? [Y/N] ");
                if(MultiTTYPromptYN()) {
                    TeletypeStringOut(terminal, "File Count-> ");
                    MultiTTYPrompt();
                    int files = strtol((char*)&tasks[currentTask].prompt,(void*)0,10);
                    char sector[128];
                    memset(&sector,0,128);
                    for(int i=0; i < 64; i++) {
                        WriteHDSector(drive,i,(void*)&sector);
                    }
                    break;
                }
            } else {
                break;
            }
        }
    }
    TeletypeStringOut(terminal,"S.");
    TeletypeRawOut(terminal,'0'+currentTask);
    TeletypeRawOut(terminal,'/');
    TeletypeStringOut(terminal,&tasks[currentTask].userInfo.name);
    TeletypeStringOut(terminal," Ready\n");
    for(;;) {
        TeletypeStringOut(terminal,"NO DISK> ");
        MultiTTYPrompt();
        char* command = strsplit((char*)&tasks[currentTask].prompt);
        if(strcmp(command,"ATTACH") == 0) {
            char* a;
            while((a = strsplit((void*)0))) {

            }
        }
    }
}