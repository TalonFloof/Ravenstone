#pragma once

typedef struct {
    char name[8];
    char password[8];
} User;

#ifndef USER_IMPL
extern User users[256];
#endif

void UserInit();
void UserSave();