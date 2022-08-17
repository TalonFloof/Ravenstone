    .org $0300

mmu .macro val
    .byte $ef
    .byte \1
.endmacro

BootTrackIndex .equ $281
BootSectorIndex .equ $282
BootIndirectMemory .equ $04

bootstrap:
    lda $01
    mmu $00
    lda #$21 ; Extend Head
    jsr $FFFD
    lda #$01 ; Seek to Track 0
    jsr $FFFD
    ; Load Tracks 1-15
    stz BootTrackIndex
    stz BootIndirectMemory ; Move $1300 to BootIndirectMemory
    lda #$13
    sta BootIndirectMemory+1
track_loop$:
    ; Seek to next track
    lda BootTrackIndex
    inc
    sta BootSectorIndex
    lda #$10
    jsr $FFFD
    lda BootSectorIndex
    sta BootTrackIndex
    stz BootSectorIndex
    ; Read all of the sectors on the track
sector_loop$:
    lda #$80
    jsr $FFFD
    ldy #0
memcpy$:
    lda $200, y
    sta (BootIndirectMemory)
    iny
    inc BootIndirectMemory
    bne skip$
    inc BootIndirectMemory+1
skip$:
    lda BootIndirectMemory+1
    cmp #$FF
    beq enter$
    cpy #128
    bne memcpy$
    ;;;;;
    inc BootSectorIndex
    lda BootSectorIndex
    cmp #32
    bne sector_loop$
    ;;;;;
    bra track_loop$
enter$:
    lda #$20 ; Retract Head
    jsr $FFFD
    jmp forth_kickstart

    .include "forth.S"
    .fill (4096-((*-$0300)%4096)), 0