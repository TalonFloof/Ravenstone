#include "string.h"

#define NULL ((void*)0)

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

void memset(void *dest, unsigned char c, int count) {
    unsigned int large = c | (c << 8) | (c << 16) | (c << 24);
    unsigned char *dp = (unsigned char *)dest;
    int i;
    for(i = count; i >= 4; i = count) {
        *((unsigned int*)dp) = large;
        count -= 4;
    }
    for (i = count; i > 0; i = count) {
        *(dp++) = c;
        count--;
    }
}

int strlen(const char* s) {
    int i = 0;
    while(s[i] != 0) {
        i++;
    }
    return i;
}

int strcmp(const char *a, const char *b) {
    int i = 0;
    while (1) {
        if (a[i] != b[i]) return 1;
        if (a[i] == '\0') break;
        i++;
    }
    return 0;
}

int strncmp(const char *a, const char *b, int n) {
    int i = 0;
    while (1) {
        if (a[i] != b[i]) return 1;
        if (a[i] == '\0' || i >= (int) n) break;
        i++;
    }
    return 0;
}

int strcnt(const char *str, char c) {
    int i = 0;
    do {
        if (str[i] == c) return 1;

    } while (str[++i] != '\0');

    return 0;
}

char* p = 0;

char *strsplit(char *str) {
    if(str != (char*)0) {
        p = str;
    } else if(p == (char*)0) {
        return p;
    }
    char* s = p;
    while(*p != '\0') {
        if(*p == ' ') {
            *p = '\0';
            p++;
            return s;
        }
        p++;
    }
    p = (char*)0;
    return s;
}

long int strtol(const char *str, char **endptr, int base) {
    long int acum = 0;
    int pos = 0;
    int sign = 1;

    if (str[pos] == '-') {
        sign = -1;
        pos++;
    } else if (str[pos] == '+') {
        pos++;
    }

    while (str[pos] != '\0') {
        int val;
        if (str[pos] >= '0' && str[pos] <= '9') {
            val = str[pos] - '0';
        } else if (str[pos] >= 'a' && str[pos] <= 'z') {
            val = str[pos] - 'a' + 10;
        } else if (str[pos] >= 'A' && str[pos] <= 'Z') {
            val = str[pos] - 'A' + 10;
        } else {
            val = -1;
        }
        if (val < 0 || val >= base) {
            *endptr = (char *) &str[pos];
            return acum;
        }
        acum = acum * base + val;
        pos++;
    }
    *endptr = (char *) &str[pos];

    return acum * sign;
}