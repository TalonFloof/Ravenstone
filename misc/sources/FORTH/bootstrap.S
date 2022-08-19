( vim: syntax=uxntal
)

`0400
|0400
@bootstrap
    #02 mmu
    #21 #fffc jsr2
    #01 #fffc jsr2
    ( Load Tracks 1-15 )
    #0000 #0381 sta2 ( Clears both Track Index and Sector Index, BIG BRAIN TIME )
    #1400
&track_loop
    #0381 sth2k lda inc inc2r sth2r sta
    #10 #fffc jsr2 #0382 lda #0381 sta
    #00 #0382 sta
    ( Read all of the sectors on the track )
&sector_loop
    dup2 #feff grt2 ,&enter jnz
    #80 #fffc jsr2
    sth2k #0300 swp2 #0080 #fff8 jsr2 ( Copy )
    sth2r #0080 add2
    #0382 sth2k lda inc sth2r stak pop2
    #20 ltn
    ,&sector_loop jnz
    ,&track_loop jmp
&enter
    pop2 #20 #fffc jsr2
    #0480 jmp2
|047f 00
~kernel.S
|13ff 00