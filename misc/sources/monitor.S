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
; $10-$11 16-bit Address Subroutine Input
; $12 Zero
; $7F Console Input Position
; $80-$BF Console Input

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
    sta $12
    sta $7F
    ; Search for a Teletype that we can use
    lda #$06
cold_boot.tty_search:
    mmu $0
    pha
    lda #'M'
    cmp $2f8
    bne .flpy_search
    lda #'T'
    cmp $2fc
    bne .flpy_search
    pla
    pha
    sta $0 ; Store Teletype Bus ID in Zero Page
    jmp .miss
cold_boot.flpy_search:
    lda #'F'
    cmp $2f8
    bne .miss
    lda #'D'
    cmp $2fc
    bne .miss
    pla
    pha
    sta $1 ; Store Floppy Drive Bus ID in Zero Page
cold_boot.miss:
    pla
    sec
    sbc #1
    bne .tty_search
    cmp $0
    bne cold_boot.tty_hit
    jmp stop
cold_boot.tty_hit:
    lda $0
    mmu $0
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

; A=Number X=Screen Line Offset
print_nibble:
    pha
    and #$f
    clc
    adc #'0'
    cmp #$3a
    bcc .print
    clc
    adc #7
print_nibble.print:
    sta $210, x
    pla
    rts
print_byte:
    pha
    inx
    jsr print_nibble
    dex
    lsr ; Shift Right 4 bits
    lsr
    lsr
    lsr
    jsr print_nibble
    pla
    rts

print_word:
    pha
    lda $10
    inx
    inx
    jsr print_byte
    dex
    dex
    lda $11
    jsr print_byte
    pla
    rts

; INPUT: X=String, OUTPUT: A=Parsed Value
parse_nibble:
    lda $00, x
    cmp #'A'
    bcs .upper
    sec
    sbc #'0'
    jmp .after
parse_nibble.upper:
    sec
    sbc #'7'
parse_nibble.after:
    clc
    rts
parse_byte:
    jsr parse_nibble
    asl
    asl
    asl
    asl
    sta $FF
    inx
    jsr parse_nibble
    dex
    ora $FF
    rts
parse_word:
    jsr parse_byte
    sta $11
    inx
    inx
    jsr parse_byte
    sta $10
    dex
    dex
    rts

register_dump:
    lda #(registerdump_header & 0xFF)
    sta $10
    lda #(registerdump_header >> 8)
    sta $11
    jsr print_line
    lda #';'
    sta $210

    ldx #2 ; PC
    lda $2
    sta $10
    lda $3
    sta $11
    jsr print_word

    ldx #7 ; Flags
    lda $4
    jsr print_byte

    ldx #10 ; .A
    lda $5
    jsr print_byte

    ldx #13 ; .X
    lda $6
    jsr print_byte

    ldx #16 ; .Y
    lda $7
    jsr print_byte

    ldx #19 ; SP
    lda #1
    jsr print_byte
    ldx #21
    lda $8
    jsr print_byte

    jsr newline
    rts

monitor_prompt:
    lda #'>'
    sta $210
    lda #1
    sta $201
    lda #0
    sta $7F
monitor_prompt.input_loop:
    lda $204
    beq monitor_prompt.input_loop
    ; Check if backspace or enter was pressed
    sta $FF
    lda #$D
    cmp $FF
    beq .after_loop
    lda #8
    cmp $FF
    beq .backspace
    lda $7F
    cmp #64
    bcs .input_loop ; Buffer is full!
    lda $FF
    jmp .after
monitor_prompt.backspace:
    ldx $7F
    cpx $12
    beq monitor_prompt.input_loop
    dex
    dec $201
    lda #' '
    sta $211, x
    stx $7F
    jmp monitor_prompt.input_loop
monitor_prompt.after:
    ldx $7F
    sta $211, x
    inc $201
    sta $80, x
    inx
    stx $7F
    jmp monitor_prompt.input_loop
monitor_prompt.after_loop:
    ldx $7F
    lda #0
    sta $80, x
    jsr newline
    ; Figure out what command the user just put
    lda #0
    cmp $81
    bne .next1
    lda #'R'
    cmp $80
    bne .next1
    jsr register_dump
    jmp monitor_prompt
monitor_prompt.next1:
    lda $7F
    cmp #5
    bcc .next2
    lda $84
    cmp #'G'
    bne .next2
    ldx #$80
    jsr parse_word
    ldx #$FF
    txs
    jmp ($10)
    nop
    nop
    nop
    brk
monitor_prompt.next2:
    lda $7F
    cmp #7
    bcc .next3
    lda $84
    cmp #':'
    bne .next3
    ldx #$80
    jsr parse_word
    ldx #$85
    ldy #0
monitor_prompt.write_loop:
    jsr parse_byte
    sta ($10), y
    inc $10
    bne .write_after_inc
    inc $11
monitor_prompt.write_after_inc:
    inx
    inx
    inx
    lda $7F
    clc
    adc #$80
    sta $FF
    cpx $FF
    bcc .write_loop
    jmp monitor_prompt
monitor_prompt.next3:
    lda $7F
    cmp #9
    bne .unknown
    lda $84
    cmp #'.'
    bne .unknown
    ;;;;;
    ldx #$85
    jsr parse_word
    lda $10
    sta $c0
    lda $11
    sta $c1
    ldx #$80
    jsr parse_word
    ;;;;; Dump Memory
monitor_prompt.memdump_loop_begin:
    jsr newline
    ldx #0
    jsr print_word
    lda #':'
    sta $214
    ldx #$6
    ldy #0
monitor_prompt.memdump_loop:
    lda ($10), y
    jsr print_byte
    lda $10
    cmp $c0
    bne .memdump_inc
    lda $11
    cmp $c1
    bne .memdump_inc
    jmp .memdump_loop_final
monitor_prompt.memdump_inc:
    inc $10
    bne .memdump_after_inc
    inc $11
monitor_prompt.memdump_after_inc:
    inx
    inx
    inx
    cpx #54
    bcc .memdump_loop
    jmp .memdump_loop_begin
monitor_prompt.memdump_loop_final:
    jsr newline
    jmp monitor_prompt

monitor_prompt.unknown:
    lda #(unknown_command & 0xFF)
    sta $10
    lda #(unknown_command >> 8)
    sta $11
    jsr print_line
    jmp monitor_prompt

stop:
    jmp stop

    .byte 0xdb

coldboot_banner: .asciiz "Raven-8t Monitor, Copyright (C) 2022 TalonFox"
registerdump_header: .asciiz "   PC  SR .A .X .Y  SP"
unknown_command: .asciiz "?"

    .fill (0xFC00-*), 0xdb
    .org 0xFC00
floppy_rom_boot:
    cld
    lda $1
    mmu $0
    ; Engage Head
    lda #$21
    sta $200
    jmp stop
    .fill (0xFE00-*), 0xdb
    .org 0xFE00
hard_drive_rom_boot:
    cld
    jmp stop
    .fill (65536-*), 0xdb