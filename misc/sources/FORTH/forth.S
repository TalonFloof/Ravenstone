    .org $0380

dcode .macro

.endmacro

forth_kickstart:
    bra forth_kickstart

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
AddressToRun .equ $FE
Index .equ AddressToRun-2

; Advances to the next Forth Word in a indirect-threaded word
next:
    lda (Index)
