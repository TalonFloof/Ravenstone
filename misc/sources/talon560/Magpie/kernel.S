(
Words to implement:
FIND
FORGET x
:
;
CURRENT
HERE
IMMEDIATE
WORD
INTERPRET
ABORT (Done)
QUIT (Done)

IF ... ELSE ... THEN
BEGIN
UNTIL
AGAIN
NEXT
DO
LOOP
[ ... ]
( ... )
EXECUTE (Done)
TICK (Done)
TICKS (Done)

DROP (Done)
DUP (Done)
NIP (Done)
OVER (Done)
ROT (Done)
-ROT (Done)
SWAP (Done)
TUCK (Done)
2DROP (Done)
2DUP (Done)

>R (Done)
R> (Done)
2>R (Done)
2R> (Done)
R@ (Done)
RDROP (Done)

.S (Maybe?)
SCNT (Maybe?)
RCNT (Maybe?)

@ (Done)
! (Done)
, (Done)
+! (Done)
C@ (Done)
C! (Done)
C, (Done)
C+! (Done)
ALLOT
FILL
MEMCPY (Done)
VARIABLE

+ (Done)
- (Done)
* (Done)
/ (Done)
1+ (Done)
1- (Done)
MOD (Done)
/MOD (Done)
AND (Done)
OR (Done)
XOR (Done)
INVERT (Done)
NEGATE (Done)
LSHIFT (Done)
RSHIFT (Done)

< (Done)
= (Done)
> (Done)
<= (Done)
<> (Done)
>= (Done)
0< (Done)
0= (Done)
0> (Done)
0<= (Done)
0<> (Done)
0>= (Done)

x.
.
u.
."
.(

PARSE
EMIT
KEY?
KEY
PAGE
CR
"
TYPE
WORDS

SAVE
DISKNAME"
FLUSH
LIST
LOAD
WIPE
PP
BLOCK
)

( Structure of a FORTH Word:
    +---------------+-------------------+--------+-------+
    |Previous (word)|Flags & Size (byte)|Name....|Code...|
    +---------------+-------------------+--------+-------+
)

@find 0000 04 "find &body jmp2r
@forget :find 06 "forget &body jmp2r
@colon :forget 01 ": &body jmp2r
@semicolon :colon 81 "; &body jmp2r
@current :semicolon 07 "current &body jmp2r
@here :current 04 "here &body jmp2r
@immediate :here 09 "immediate &body jmp2r
@word :immediate 04 "word &body jmp2r
@interpret :word 09 "interpret &body jmp2r
@abort :interpret 05 "abort &body ,quit/body jmp jmp2r
@quit :abort 04 "quit &body pop2r jmp2r

@if :quit 02 "if &body jmp2r
@else :if 04 "else &body jmp2r
@then :else 04 "then &body jmp2r
@begin :then 05 "begin &body jmp2r
@until :begin 05 "until &body jmp2r
@again :until 05 "again &body jmp2r
@next :again 04 "next &body jmp2r
@do :next 02 "do &body jmp2r
@loop :do 04 "loop &body jmp2r
@lbrac :loop 81 "[ &body jmp2r
@rbrac :lbrac 81 "] &body jmp2r
@lpare :rbrac 81 "( &body jmp2r
@rpare :lpare 81 ") &body jmp2r
@execute :rpare 07 "execute &body jmp2
@tick :execute 04 "tick &body wai jmp2r
@ticks :tick 05 "ticks &body jnz ,&loop jmp2r &loop wai #0001 sub2 ,&loop jnz jmp2r

@drop :ticks 04 "drop &body pop2 jmp2r
@DUP :drop 03 "dup &body dup2 jmp2r
@NIP :DUP 03 "nip &body nip2 jmp2r
@over :NIP 04 "over &body ovr2 jmp2r
@ROT :over 03 "rot &body rot2 jmp2r
@nrot :ROT 04 "-rot &body rot2 sth2 swp2 sth2r swp2 jmp2r
@swap :nrot 04 "swap &body swp2 jmp2r
@tuck :swap 04 "tuck &body swp2 ovr2 jmp2r
@twodrop :tuck 05 "2drop &body pop2 pop2 jmp2r
@twodup :twodrop 04 "2dup &body ovr2 ovr2 jmp2r

@tor :twodup 02 ">r &body sth2 jmp2r
@fromr :tor 02 "r> &body sth2r jmp2r
@twotor :fromr 03 "2>r &body sth2 sth2 jmp2r
@twofromr :twotor 03 "2r> &body sth2r sth2r jmp2r
@rfetch :twofromr 02 "r@ &body sth2rk lda2 jmp2r
@rdrop :rfetch 05 "rdrop &body pop2r jmp2r

@peek :rdrop 01 "@ &body lda2 jmp2r
@poke :peek 01 "! &body sta2 jmp2r
@comma :poke 01 ", &body ;here/body jsr2 sta2k nip2 lda2k inc2 inc2 swp2 sta2 jmp2r
@incrpoke :comma 02 "+! &body sth2k lda2 add2 sth2r sta2 jmp2r
@bytepeek :incrpoke 02 "C@ &body lda #00 jmp2r
@bytepoke :bytepeek 02 "C! &body sth2 swp sth2r sta pop jmp2r
@bytecomma :bytepoke 02 "C, &body ;here/body jsr2 sta2k ;bytepoke/body jsr2 jmp2r
@byteincrpoke :bytecomma 03 "C+! &body sth2k ;bytepeek/body jsr2 add2 sth2r ;bytepoke/body jsr2 jmp2r
@allot :byteincrpoke 05 "allot &body jmp2r
@fill :allot 04 "fill &body jmp2r
@memcpy :fill 06 "memcpy &body #fff8 jmp2
@variable :memcpy 08 "variable &body jmp2r

@plus :variable 01 "+ &body add2 jmp2r
@minus :plus 01 "- &body sub2 jmp2r
@mult :minus 01 "* &body mul2 jmp2r
@divi :mult 01 "/ &body div2 jmp2r
@incre :divi 02 "1+ &body inc2 jmp2r
@decr :incre 02 "1- &body #0001 sub2 jmp2r
@mod :decr 03 "mod &body div2k mul2 sub2 jmp2r
@divmod :mod 04 "/mod &body ( : /mod 2dup 2>r / 2r> swap mod ; )
    ;twodup/body jsr2
    ;twotor/body jsr2
    ;divi/body jsr2
    ;twofromr/body jsr2
    swp2
    ;mod/body jsr2
jmp2r
@AND :divmod 03 "and &body and2 jmp2r
@or :AND 02 "or &body ora2 jmp2r
@xor :or 03 "xor &body eor2 jmp2r
@invert :xor 06 "invert &body #ffff eor2 jmp2r
@negate :invert 06 "negate &body #ffff eor2 inc2 jmp2r
@lshift :negate 06 "lshift &body #0040 sft2 sft2 jmp2r
@rshift :lshift 06 "rshift &body #000f and2 sft2 jmp2r

@lessthan :rshift 01 "< &body ltn2 #ff mul dup jmp2r
@equal :lessthan 01 "= &body equ2 #ff mul dup jmp2r
@grtrthan :equal 01 "> &body grt2 #ff mul dup jmp2r
@leeq :grtrthan 02 "<= &body ,grtrthan/body jsr #ffff eor2 jmp2r
@notequal :leeq 02 "<> &body ,equal/body jsr #ffff eor2 jmp2r
@greq :notequal 02 ">= &body ,lessthan/body jsr #ffff eor2 jmp2r

@zlessthan :greq 02 "0< &body #0000 ,lessthan/body jmp
@zequal :zlessthan 02 "0= &body #0000 ,equal/body jmp
@zgrtrthan :zequal 02 "0> &body #0000 ,grtrthan/body jmp
@zleeq :zgrtrthan 03 "0<= &body #0000 ,leeq/body jmp
@znotequal :zleeq 03 "0<> &body #0000 ,notequal/body jmp
@zgreq :znotequal 03 "0>= &body #0000 ,greq/body jmp

@xdot :zgreq 02 "x. &body jmp2r
@dot :xdot 01 ". &body jmp2r
@udot :dot 02 "u. &body jmp2r
@dotquote :udot 02 "." &body jmp2r
@dotparen :zgreq 82 ".( &body jmp2r



@magpie_end