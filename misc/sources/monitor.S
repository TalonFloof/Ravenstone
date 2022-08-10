;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;             MONITOR FOR RAVEN-8T             ;
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
;;;;;;;;; REGISTER DUMP;;;;;;;;;
; $02-$03 PC
; $04 Flags
; $05 A Register
; $06 X Register
; $07 Y Register
; $08-$09 SP Register
; $10-$11 String Address

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
    sta $00
    sta $01
    sta $02
    sta $03
    sta $04
    sta $05
    sta $06
    sta $07
    sta $08
    sta $09
    ; Search for a Teletype that we can use
    lda #$06
cold_boot.tty_search:
    mmu $0
    pha
    lda #'M'
    cmp $2f8
    bne .tty_miss
    lda #'T'
    cmp $2fc
    bne .tty_miss
    pla
    jmp .tty_hit
cold_boot.tty_miss:
    pla
    sec
    sbc #1
    bne .tty_search
    jmp stop
cold_boot.tty_hit:
    sta 0 ; Store Teletype Bus ID in Zero Page
    ; Clear Display
    lda #0
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
    ; Print Banner
    lda #(coldboot_banner & 0xFF)
    sta $10
    lda #(coldboot_banner >> 8)
    sta $11
    jsr print_line
    jsr register_dump
    jmp monitor_prompt
    nop ; Padding
    nop
    nop
    stp

; Pass pointer to address $10
print_line:
    php
    pha
    tya
    pha

    lda $202
    sta $200

    ldy #0
print_line.loop:
    lda ($10), y
    beq .end
    sta $210, y
    iny
    jmp .loop
print_line.end:
    inc $202
    pla
    tay
    pla
    plp
    rts

register_dump:
    lda #(registerdump_header & 0xFF)
    sta $10
    lda #(registerdump_header >> 8)
    sta $11
    jsr print_line
    lda $202
    sta $200
    lda #';'
    sta $210
    inc $200
    inc $202
    rts

monitor_prompt:
    lda #'>'
    sta $210
    lda #1
    sta $201
    jmp stop

stop:
    jmp stop

    .byte 0xdb

coldboot_banner: .asciiz "Raven-8t Monitor, Copyright (C) 2022 TalonFox"
registerdump_header: .asciiz "   PC  SR .A .X .Y  SP"

    .fill (0xFC00-*), 0xdb
    .org 0xFC00
floppy_rom_boot:
    cld
    jmp stop
    .fill (0xFE00-*), 0xdb
    .org 0xFE00
hard_drive_rom_boot:
    cld
    jmp stop
    .fill (65536-*), 0xdb