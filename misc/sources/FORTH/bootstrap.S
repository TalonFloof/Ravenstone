    .org $0300

mmu .macro
    .byte $ef
    .byte \0
.endmacro

bootstrap:
    lda $01
    mmu $00
    lda #$F4 ; Clear Buffer
    jsr $FFFD
    lda #$21 ; Extend Head
    jsr $FFFD
    lda #$01 ; Seek to Track 0
    jsr floppy_command

bootstrap.sector_loop:


forth_size: .word ((forth_end-forth_begin)/128)

    .fill (128-(*%128)), 0
forth_begin:
    .include "forth.S"
    .fill (128-(*%128)), 0
forth_end: