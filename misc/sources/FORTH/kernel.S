( vim: syntax=uxntal
)
( This is the FORTH Kernel, all of the forth words and other things can be found here )

;cold/body jmp2

( Structure of a FORTH Word:
    +---------------+-------------------+--------+-------+
    |Previous (word)|Flags & Size (byte)|Name....|Code...|
    +---------------+-------------------+--------+-------+
)

( ======================== BASE ======================== )
@dovar 0000 05 "DOVAR &body jmp2r

@exit :dovar 04 "EXIT &body pop2r jmp2r

@true :exit 04 "TRUE &body #ffff jmp2r
@false :true 05 "FALSE &body #0000 jmp2r

@tick :false 04 "TICK &body wai jmp2r
@ticks :tick 05 "TICKS &body jnz ,&loop jmp2r &loop wai #0001 sub2 ,&loop jnz jmp2r

@cold :ticks 04 "COLD &body ,quit/body jmp ( Temporary )
@abort :cold 05 "ABORT &body ,quit/body jmp ( Temporary )
@quit :abort 04 "QUIT &body ,&body jmp ( Temporary )

( ======================== STACK ======================== )
@depth :quit 05 "DEPTH &body #0001 jmp2r ( -- stack-depth, temporary )
@drop :depth 04 "DROP &body pop2 jmp2r
@swap :drop 04 "SWAP &body swp2 jmp2r
@DUP :swap 03 "DUP &body dup2 jmp2r
@over :DUP 04 "OVER &body ovr2 jmp2r
@NIP :over 03 "NIP &body nip2 jmp2r
@tuck :NIP 04 "TUCK &body swp2 ovr2 jmp2r
@ROT :tuck 03 "ROT &body rot2 jmp2r
@nrot :ROT 04 "-ROT &body rot2 sth2 swp2 sth2r swp2 jmp2r
@twodrop :nrot 05 "2DROP &body pop2 pop2 jmp2r
@twodup :twodrop 04 "2DUP &body ovr2 ovr2 jmp2r
@tor :twodup 02 ">R &body sth2 jmp2r
@fromr :tor 02 "R> &body sth2r jmp2r
@twotor :fromr 03 "2>R &body sth2 sth2 jmp2r
@twofromr :twotor 03 "2R> &body sth2r sth2r jmp2r
@rdrop :twofromr 05 "RDROP &body pop2r jmp2r
( ======================== MATH ======================== )
@incre :rdrop 02 "1+ &body inc2 jmp2r
@decr :incre 02 "1- &body #0001 sub2 jmp2r
@incrtwo :decr 02 "2+ &body inc2 inc2 jmp2r
@decrtwo :incrtwo 02 "2- &body #0002 sub2 jmp2r
@plus :decrtwo 01 "+ &body add2 jmp2r
@minus :plus 01 "- &body sub2 jmp2r
@mult :minus 01 "* &body mul2 jmp2r
@umult :mult 02 "U* &body mul2 jmp2r

@divi :umult 01 "/ &body ( : / ABS SWAP ABS SWAP U/ ; )
;abs/body jsr2
;swap/body jsr2
;abs/body jsr2
;swap/body jsr2
;udivi/body jsr2
jmp2r

@udivi :divi 02 "U/ &body div2 jmp2r

@divmod :udivi 04 "/MOD" &body ( : /MOD 2DUP 2>R / 2R> SWAP MOD ; )
    ;twodup/body jsr2
    ;twotor/body jsr2
    ;divi/body jsr2
    ;twofromr/body jsr2
    swp2
    ;mod/body jsr2
jmp2r

@udivmod :divmod 05 "U/MOD" &body ( : U/MOD 2DUP 2>R U/ 2R> SWAP MOD ; )
    ;twodup/body jsr2
    ;twotor/body jsr2
    div2
    ;twofromr/body jsr2
    swp2
    ;mod/body jsr2
jmp2r

@mod :udivmod 03 "MOD &body div2k mul2 sub2 jmp2r
@abs :mod 03 "ABS &body dup2 #8000 and2 pop ,&neg jnz jmp2r &neg #0000 swp2 sub2 #00 ,divsign str jmp2r @divsign 00
( ======================== LOGIC ======================== )
@equal :abs 01 "= &body equ2 #ff mul dup jmp2r
@lessthan :equal 01 "< &body ltn2 #ff mul dup jmp2r
@grtrthan :lessthan 01 "> &body grt2 #ff mul dup jmp2r
@zequal :grtrthan 02 "0= &body #0000 equ2 #ff mul dup jmp2r
@zlessthan :zequal 02 "0< &body #0000 ltn2 #ff mul dup jmp2r
@zgrtrthan :zlessthan 02 "0> &body #0000 grt2 #ff mul dup jmp2r
@AND :zgrtrthan 03 "AND &body and2 jmp2r
@OR :AND 02 "OR &body ora2 jmp2r
@xor :OR 03 "XOR &body eor2 jmp2r
@invert :xor 06 "INVERT &body #ffff eor2 jmp2r
@negate :invert 06 "NEGATE &body #ffff eor2 inc2 jmp2r
@lshift :negate 06 "LSHIFT &body #0040 sft2 sft2 jmp2r
@rshift :lshift 06 "RSHIFT &body #000f and2 sft2 jmp2r
( ======================== MEMORY ======================== )
@poke :rshift 01 "! &body sta2 jmp2r
@peek :poke 01 "@ &body lda2 jmp2r
@bytepoke :peek 02 "C! &body sth2 swp sth2r sta pop jmp2r
@bytepeek :bytepoke 02 "C@ &body lda #00 jmp2r

@free :bytepeek 04 "FREE &body
    #2000
    #aa #2000 sta #2000 lda #aa neq ,&end jnz ( 16 KiB Test )
    pop2 #4000
    #aa #4000 sta #4000 lda #aa neq ,&end jnz ( 32 KiB Test )
    pop2 #8000
    #aa #8000 sta #8000 lda #aa neq ,&end jnz ( 64 KiB Test )
    pop2 #ff00
&end ;dictionary_ptr lda2 sub2 jmp2r
@allocate :free 05 "ALLOT &body
    dup2
    dup2
    ;zlessthan/body jsr2
    ;invert/body jsr2 pop
    ,&cont jnz
    ;negate/body jsr2
&cont
    ;free/body jsr2
    ;grtrthan/body jsr2 pop
    ,&oom jnz
    ;here/body jsr2
    ;tuck/body jsr2
    add2
    ;dp/body jsr2
    ;poke/body jsr2
jmp2r
&oom
    pop2
    (
    Not yet...
    ;allot_str #0013
    ;type/body jsr2
    )
    ;abort/body jsr2
@memcpy :allocate 06 "MEMCPY &body #fff8 jsr2 jmp2r
@memcmp :memcpy 06 "MEMCMP &body
    ;twodup/body jsr2
    neq ,&equaddr jnz
    #0000
    ;tor/body jsr2
&loop
    ;twodup/body jsr2
    ldar2 sth2r add2
    ;bytepeek/body jsr2
    swp2
    ldar2 sth2r add2
    ;bytepeek/body jsr2
    ;equal/body jsr2 pop
    ,&noaddr jmp
    ;fromr/body jsr2
    inc2
    ;tor/body jsr2
    ,&loop jmp
&noaddr
    ;twodrop/body jsr2
    ;fromr/body jsr2
jmp2r
&equaddr
    ;twodrop/body jsr2
    #ffff
jmp2r
( ======================== DICTIONARY ======================== )
@dictionary_ptr :dictionary_top

@here :memcmp 04 "HERE &body ,dictionary_ptr ldr2 jmp2r
@dp :here 02 "DP &body ;dictionary_ptr jmp2r
@find :dp 04 "FIND &body
( ======================== IO ======================== )

( All memory from this point forward is used for defining new words )
@dictionary_top