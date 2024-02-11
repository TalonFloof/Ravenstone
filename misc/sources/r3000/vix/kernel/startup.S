.section .text.entry
.global BootloaderEntry
.type BootloaderEntry, @function
BootloaderEntry:
    .extern main
    jal main
    li $sp, 0xa00ffffc
    li $t0, 0x80000000
    jr $t0