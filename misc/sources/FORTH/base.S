    dcode "LIT", LIT, 3, 0
    ; Push 2-byte Literal word onto Rstack
    lda (AddressToRun)
    sta 0,x
    ldy #1
    lda (AddressToRun),y
    sta 1,x
    jmp next_dex2

