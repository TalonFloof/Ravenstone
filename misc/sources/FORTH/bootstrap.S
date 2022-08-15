    .org $0300

mmu .macro
    .byte $ef
    .byte \0
.endmacro

bootstrap:
    lda $01
    mmu $00

forth_size: .word ((forth_end-forth_begin)/128)

    .fill (128-(*%128)), 0
forth_begin:
    .include "forth.S"
    .fill (128-(*%128)), 0
forth_end: