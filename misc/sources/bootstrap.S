( vim: syntax=uxntal
)

`ff00
|ff00 @coldboot
    #01 mmu ( Sets Bus to use Screen )
    #00 #0300 sta
    #00 #0301 sta
    #00 #0302 sta
    #20 #0308 sta
    #00 #030a sta
    #00 #030b sta
    #50 #030c sta
    #32 #030d sta
    #01 #0307 sta wai
    ;BootingMsg #0310 #0007 ;print_line jsr2
    #02 mmu ( Sets Bus to use Floppy Disk Drive )
    #00 #0381 sta ( Set Track Register to 0 )
    #21 ,floppy_command jsr ( Engage Head )
    #0380 lda #08 and ,nodisk jnz ( Check if disk is missing )
    #01 ,floppy_command jsr ( Seek to Track #0 )
    ( Now we're going to read all of the sectors on Track #0 )
    #00 #0382 sta ( Set Sector Index )
    #0400
&sector_load
    #80 ,floppy_command jsr ( Read Sector )
    dup2 #0300 swp2 #0080 ,memcpy jsr #0080 add2 ( Copy )
    #0382 lda inc #0382 stak pop2 ( Increment Sector )
    #20 ltn ,&sector_load jnz ( Retract Head if SectorIndex < 32 )
    pop2 #20 ,floppy_command jsr
    #0400 jmp2 ( Run the code on the floppy disk )
@nodisk
    #01 mmu
    ;NoDiskMsg #0310 #0008 ,print_line jsr
@halt
    ,halt jmp
@floppy_command
    #0380 sta ( Send Command )
    &loop
         wai #0380 lda #01 and ,&loop jnz ( Wait until command is finished )
jmp2r

@memcpy ( src* dst* len* -- )
    swp2 sth2 ovr2 add2 swp2
    &loop
        ldak sth2kr sta inc2r inc2 grt2k ,&loop jnz
    pop2 pop2 pop2r
jmp2r

@print_line
    ,memcpy jsr #0300 lda inc #0300 sta #0302 lda inc #0302 sta
jmp2r

@BootingMsg "Booting
@NoDiskMsg "No 20 "disk?
|fff8
;memcpy jmp2           ( FFF8: memcpy )
;floppy_command jmp2   ( FFFC: floppy_command )