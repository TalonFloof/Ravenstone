#pragma once

typedef int jmp_buf[31];
__attribute__((naked, noreturn)) void enterjmp(void* stack);
int setjmp(jmp_buf buf);
void longjmp(jmp_buf buf, int value);