    .org $0380

PreviousWord .set $0000

dcode .macro name,lname,len,flags
header_\2_prev:
    .word PreviousWord
PreviousWord .set header_\2_prev
header_\2_lenflags:
    .byte \3+\4
header_\2_name:
    .text \1
\2:
.endmacro

forth_kickstart:
    cld
    ldx #$FF
    txs
    stz $02
    bra *

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
NumberMode .equ $02
AddressToRun .equ $FE
Index .equ AddressToRun-2

next_dex2:
    dex
    dex
; Advances to the next Forth Word
next:
    lda (AddressToRun)
    sta Index
    ldy #1
    lda (AddressToRun), y
    sta Index+1
    clc
    lda AddressToRun
    adc #2
    sta AddressToRun
    bcc .skip
    inc AddressToRun+1
.skip:
    jmp (AddressToRun)

    .include "base.S"
