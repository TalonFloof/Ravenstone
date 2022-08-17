    .org $0380

PreviousWord .set $0000

dcode .macro name,lname,len,flags
header_\2_prev:
    .word PreviousWord
PreviousWord .set header_\2_prev
header_\2_lenflags:
    .byte \3+\4
header_\2_name:
    .byte \1
\2:
.endmacro
dword .macro name,lname,len,flags
    dcode \1,\2,\3,\4
    jmp COLON
.endmacro

forth_kickstart:
    cld
    ldx #$FF
    txs
    ldx #StackTop
    bra *

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
StackBottom .equ $10
StackTop .equ $CF
General .equ XSave-2
XSave .equ RunMode-1
RunMode .equ NumberMode-1
NumberMode .equ IP-1
IP .equ W-2
W .equ $FE
; X Index Register is the R Stack

; Advances to the next Forth Word
next:
    lda (IP)
    sta W
    ldy #1
    lda (IP), y
    sta W+1
    clc
    lda IP
    adc #2
    sta IP
    bcc skip$
    inc IP+1
skip$:
    jmp (IP)

;;;;;;;;;;;;;;;;;;;;;;;;;;;; WORDS
