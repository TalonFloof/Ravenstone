.section .text.entry
.global BootloaderEntry
.type BootloaderEntry, @function
BootloaderEntry:
    .extern main
    jal main
    li $sp, 0xa00ffffc
    mfc0 $t0, $12
    or $v0, $t0, 0x400000
    mtc0 $v0, $12
    li $t0, 0x80000000
    jr $t0