#pragma once

void *memcpy(void *dest, const void *src, int count);
void memset(void *dest, unsigned char c, int count);
int strlen(const char* s);
int strcmp(const char *a, const char *b);
int strncmp(const char *a, const char *b, int n);
int strcnt(const char *str, char c);
char *strsplit(char *str);
long int strtol(const char *str, char **endptr, int base);