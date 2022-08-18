( vim: syntax=uxntal
)

`ff00
|ff00 @coldboot
    #02 mmu ( 3 bytes, Sets Bus to use Floppy Disk Drive )
    #00 #0381 sta ( 6 bytes, Set Track Register to 0 )
    #21 ,floppy_command jsr ( 5 bytes, Engage Head )
    #01 ,floppy_command jsr ( 5 bytes, Seek to Track #0 )
    ( Now we're going to read all of the sectors on Track #0 )
    #00 #0382 sta ( 6 bytes, Set Sector Index )
    #0400
&sector_load
    #80 ,floppy_command jsr ( 5 bytes, Read Sector )
    dup2 #0300 swp2 #0080 ,memcpy jsr #0080 add2 ( 15 bytes, Copy )
    #0382 lda #01 add #0382 stak pop2 ( 12 bytes, Increment Sector )
    #20 ltn ,&sector_load jnz ( 6 bytes, Jump if SectorIndex < 32 )
    #20 ,floppy_command jsr ( 5 bytes, Retract Head )
@halt
    ,halt jmp ( 3 bytes, Infinte Halt Loop )
@floppy_command
    #0380 sta ( 4 bytes, Send Command )
    &loop
         wai #0380 lda #01 and #01 equ ldik 00 ,&loop jnz ( 17 bytes, Wait until command is finished )
jmp2r ( 1 byte, Return from @floppy_command )

@memcpy ( src* dst* len* -- )
    swp2 sth2 ovr2 add2 swp2
    &loop
        ldak sth2kr sta
        ldi2r 0001 add2r
        #0001 add2
        grt2k
        ,&loop jnz
    pop2 pop2 pop2r jmp2r