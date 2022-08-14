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
;Licensed under the Apache License, Version 2.0 (the "License");
;you may not use this file except in compliance with the License.
;You may obtain a copy of the License at
;   http://www.apache.org/licenses/LICENSE-2.0
;Unless required by applicable law or agreed to in writing, software
;distributed under the License is distributed on an "AS IS" BASIS,
;WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;See the License for the specific language governing permissions and
;limitations under the License.

; ZEROPAGE MEMORY MAP
; $00 TTY Bus ID
; $01 Disk Drive ID
; $10-$11 16-bit Address Subroutine Input

    .org $F000

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

cold_boot: ; This function is called when the Raven-8t first starts up
    cld      ; Disable BCD Mode
    lda #$00 ; Clear Registers
    ldx #$00
    ldy #$00
cold_boot.display:
    lda #1
    sta $0
    lda #2
    sta $1
    lda $0
    mmu $0
    ; Clear Display
    lda #0
    cmp $FE
    bne .after_clear
    sta $200
    sta $201
    sta $202
    sta $20a
    sta $20b
    lda #$20
    sta $208
    lda #80
    sta $20C
    lda #50
    sta $20D
    lda #1
    sta $207
    wai
.after_clear:
    jmp floppy_rom_boot

; Pass pointer to address $10
print_line:
    pha
    tya
    pha
    ldy #0
print_line.loop:
    lda ($10), y
    beq .end
    sta $210, y
    iny
    jmp .loop
print_line.end:
    jsr newline
    pla
    tay
    pla
    rts

;;;;;;;;;;;;;;;;;;;;;;;
; INPUT: A=Char
print_char:
    pha
    cmp #$D
    beq .return
    cmp #$A
    beq .line
    cmp #$8
    beq .back
    ;;;;;
    ldx $201
    sta $210, x
    inx
    stx $201
    jmp .after
print_char.return:
    lda #0
    sta $201
    jmp .after
print_char.line:
    jsr newline
    jmp .after
print_char.back:
    ldx $201
    dex
    stx $201
print_char.after:
    pla
    rts
read_char:
    lda $204
    beq read_char
    rts

newline:
    pha
    lda $202
    cmp #49
    bcs .scroll
    inc $200
    inc $202
    jmp .after
newline.scroll:
    lda #0
    sta $20a ; BlitXOffset
    sta $20b ; BlitYOffset
    sta $208 ; BlitXStart
    lda #1
    sta $209 ; BlitYStart
    lda #80
    sta $20c ; BlitWidth
    lda #49
    sta $20d ; BlitHeight
    lda #3
    sta $207
    wai
    ;;;;;
    lda #' '
    sta $208
    lda #49
    sta $20b
    lda #80
    sta $20c
    lda #1
    sta $20d
    lda #0
    sta $20a
    lda #1
    sta $207
    wai
newline.after:
    pla
    rts

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
floppy_rom_boot:
    lda $1
    mmu $0
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
    sta $280
    ; Read all of the sectors in the first track
    lda #0
    sta $F0
    sta $281
    sta $282
    sta $20 ; 0x0300
    lda #3
    sta $21
load_sector:
    ldy #0
    lda $F0
    sta $282
    lda #$80
    jsr floppy_command
memcpy:
    lda $200, y
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
    bcs .after
    jmp load_sector
.after:
    ; Retract Head
    lda #$20
    jsr floppy_command
    ; Execute the Bootloader
    lda #0
    ldx #0
    ldy #0
    pha
    plp
    jmp $300
    nop
    nop
    nop
    brk
floppy_command:
    sta $280
floppy_command.wait:
    wai
    lda $280
    sta $2
    and #1
    cmp #1
    beq .wait
    lda $2
    rts
floppy_no_disk:
    lda $0
    mmu $0
    lda #(floppy_no_disk_msg & 0xFF)
    sta $10
    lda #(floppy_no_disk_msg >> 8)
    sta $11
    jsr print_line
    jmp *
floppy_no_disk_msg: .asciiz "No disk?"
    .org $FFF7
    jmp read_char       ; $FFF7
    jmp print_char      ; $FFFA
    jmp floppy_command  ; $FFFD