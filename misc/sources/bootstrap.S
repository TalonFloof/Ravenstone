;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;             BOOTSTRAP FOR RAVEN-8T           ;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Created by                                   ;
; ▄▄▄▄▄▄ ▄▄▄· ▄▄▌         ▐ ▄ ·▄▄▄      ▐▄• ▄  ;
; ▀•██ ▀▐█ ▀█ ██•   ▄█▀▄ •█▌▐██  · ▄█▀▄  █▌█▌▪ ;
;   ▐█.▪▄█▀▀█ ██ ▪ ▐█▌.▐▌▐█▐▐▌█▀▀▪▐█▌.▐▌ ·██·  ;
;   ▐█▌·▐█▪ ▐▌▐█▌ ▄▐█▌.▐▌██▐█▌██ .▐█▌.▐▌▪▐█·█▌ ;
;   ▀▀▀  ▀  ▀ .▀▀▀  ▀█▄▀▪▀▀ █▪▀▀▀  ▀█▄▀▪•▀▀ ▀▀ ;
;                                              ;
;    Copyright (C) 2022 TalonFox               ;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; ZEROPAGE MEMORY MAP
; $00 TTY Bus ID
; $01 Disk Drive ID
; $10-$11 16-bit Address Subroutine Input

    .org $FF00

cold_boot: ; This function is called when the Raven-8t first starts up
    lda #1
    sta $00
    mmu $00
    lda #2
    sta $01
    ; Clear Display
    stz $0200
    stz $0201
    stz $0202
    stz $020a
    stz $020b
    lda #$20
    sta $0208
    lda #80
    sta $020C
    lda #50
    sta $020D
    lda #1
    sta $0207
    wai
    lda #(floppy_boot_msg & 0xFF)
    sta $10
    lda #(floppy_boot_msg >> 8)
    sta $11
    jsr print_line
    ;;;;;
    lda $01
    mmu $00
    ; Engage Head
    lda #$21
    jsr floppy_command
    ; Seek To Track 0
    lda #$01
    jsr floppy_command
    cmp #0
    bne floppy_no_disk
    ; Clear Buffer
    lda #$F4
    sta $0280
    ; Read all of the sectors in the first track
    stz $F0
    stz $0281
    stz $0282
    stz $20 ; 0x0300
    lda #3
    sta $21
load_sector:
    ldy #0
    lda $F0
    sta $0282
    lda #$80
    jsr floppy_command
memcpy:
    lda $0200, y
    sta ($20), y
    iny
    cpy #128
    bne memcpy
    lda $20
    clc
    adc #128
    sta $20
    lda $21
    adc #0
    sta $21
    clc
    inc $F0
    lda $F0
    cmp #32
    bcs after
    jmp load_sector
after:
    ; Retract Head
    lda #$20
    jsr floppy_command
    ; Execute the Bootloader
    pha
    plp
    jmp $0300
floppy_command:
    sta $0280
    pha
wait$:
    pla
    wai
    lda $0280
    pha
    and #1
    cmp #1
    beq wait$
    pla
    rts
floppy_no_disk:
    lda $00
    mmu $00
    lda #(floppy_no_disk_msg & 0xFF)
    sta $10
    lda #(floppy_no_disk_msg >> 8)
    sta $11
    jsr print_line
    bra *

; Pass pointer to address $10
print_line:
    pha
    phy
    ldy #0
loop$:
    lda ($10), y
    beq end$
    sta $0210, y
    iny
    bra loop$
end$:
    inc $0202
    inc $0200
    ply
    pla
    rts
floppy_no_disk_msg: .asciiz "No disk?"
floppy_boot_msg: .asciiz "Booting"

mmu .macro val
    .byte $ef
    .byte \val
.endmacro

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    .org 0xFFFD
    jmp floppy_command
