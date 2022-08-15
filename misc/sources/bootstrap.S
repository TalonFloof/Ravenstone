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

    .org $FF00

mmu .macro
    .byte $ef
    .byte \0
.endmacro

cold_boot: ; This function is called when the Raven-8t first starts up
    lda #1
    sta $0
    mmu $0
    lda #2
    sta $1
    ; Clear Display
    stz $200
    stz $201
    stz $202
    stz $20a
    stz $20b
    lda #$20
    sta $208
    lda #80
    sta $20C
    lda #50
    sta $20D
    lda #1
    sta $207
    wai
    lda #(floppy_boot_msg & 0xFF)
    sta $10
    lda #(floppy_boot_msg >> 8)
    sta $11
    jsr print_line
    ;;;;;
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
    stz $F0
    stz $281
    stz $282
    stz $20 ; 0x0300
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
    pha
    plp
    jmp $300
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
    bra *

; Pass pointer to address $10
print_line:
    pha
    phy
    ldy #0
print_line.loop:
    lda ($10), y
    beq .end
    sta $210, y
    iny
    bra .loop
print_line.end:
    inc $202
    inc $200
    ply
    pla
    rts
floppy_no_disk_msg: .asciiz "No disk?"
floppy_boot_msg: .asciiz "Booting"
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    .org 0xFFFD
    jmp floppy_command