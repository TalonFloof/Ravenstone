;This program is located on Track 0 on the Floppy Disk containing the operating system
;This contains a BIOS (Basic Input/Output System) and it also loads the kernel containing the full OS

; These Macros implement opcodes which are exclusive to Ravenstone
mmu .macro
    .byte $ef
    .byte \0
.endmacro
mas .macro
    .byte $df
.endmacro
; These Macros implement opcodes which are available on the WDC 65C02
wai .macro
    .byte $cb
.endmacro
stp .macro
    .byte $db
.endmacro

    .org $0300
    jmp magpie_cold_boot ; COLDBOOT $300
    jmp $FFF7            ; TTYIN    $303
    jmp $FFFA            ; TTYOUT   $306
    jmp magpie_select    ; DSKSEL   $309
    jmp magpie_home      ; DSKHOME  $30c
    jmp magpie_track     ; DSKTRACK $30f
    jmp magpie_sector    ; DSKSECT  $312
    jmp magpie_read      ; DSKREAD  $315
    jmp magpie_write     ; DSKWRITE $315
    jmp $0               ; DSKLBLRD $318 (Optional)
    jmp $0               ; DSKLBLWR $31b (Optional)
magpie_cold_boot:
    lda $00
    sta magpie_tty_id
    mmu $0
    lda $01
    sta magpie_floppy_id
    ; Clear Terminal
    lda #$20
    sta $208
    lda #0
    sta $200
    sta $201
    sta $202
    sta $20A
    sta $20B
    lda #80
    sta $20C
    lda #50
    sta $20D
    lda #1
    sta $207
    wai
    lda #(magpie_bios_banner & 0xFF)
    sta $80
    lda #(magpie_bios_banner >> 8)
    sta $81
    jsr magpie_bios_write_string
    jmp *
magpie_select:
    cmp #0
    beq .zero
    cmp #128
    beq .device
    clc
    adc #1
    mmu $0
    lda #21 ; Extend Head
    jsr $FFFD
    rts
magpie_select.zero:
    lda #20 ; Retract Head
    jsr $FFFD
    lda #0
    mmu $0
    rts
magpie_select.device:
    clc
    sbc #$80
    mmu $0
    rts
magpie_home:
    lda #01
    jsr $FFFD
    rts
magpie_track:
    sta $282
    pha
    lda #$10
    jsr $FFFD
    pla
    sta $281
    rts
magpie_sector:
    sta $281
    rts
magpie_read: ; X=Pointer to put Sector at
    lda #$80
    jsr $FFFD
    ldy #0
magpie_read.memcpy:
    lda $200, y
    sta ($0, x)
    inc $0, x
    bne *+4
    inc $1, x
    iny
    cpy #128
    bne .memcpy
    rts
magpie_write: ; X=Pointer to Sector
    ldy #0
magpie_write.memcpy:
    lda ($0, x)
    sta $200, y
    inc $0, x
    bne *+4
    inc $1, x
    iny
    cpy #128
    bne .memcpy
    lda #$A0
    jsr $FFFD
    rts

; 16-bit Address located at $80
magpie_bios_write_string:
    ldy #0
magpie_bios_write_string.loop:
    lda ($80), y
    cmp #0
    beq .after
    jsr $306
    iny
    jmp .loop
magpie_bios_write_string.after:
    rts

magpie_floppy_id: .byte 0
magpie_tty_id: .byte 0
magpie_bios_banner: .asciiz "Raven-8t Bios for Magpie v1", $D, $A
    .fill (128-(*%128)), 0