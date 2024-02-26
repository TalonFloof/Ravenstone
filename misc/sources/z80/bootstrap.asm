    org 0xf000
bootstrap:
    ld sp, 256
    ld a, 0x01     ; Clear the Teletype
    out (0x00), a
    ld (0xff81), a
    ld hl, booting ; Print "Booting..."
    call print_string
    call beep      ; Beep
    ;;;; Diskette Boot Procedure ;;;;
    ld a, 0x02
    out (0x00), a
    xor a
    ld (0xff81), a
    ld (0xff82), a
    in a, (0x01)
    or a
    jr z, commok
    ld hl, commfail
    call print_string
    call beep
    jr yield
commok:
    ld a, 0x21 ; Engage Head
    call floppy_command
    and 0x8
    jr z, diskok
    ld hl, nodisk
    call print_string
    call beep
    jr yield
diskok:
    ld a, 0x01
    call floppy_command
    ld de, 0xd400
diskread_loop:
    ld a, 0x80
    call floppy_command
    ld hl, 0xff00
    ld bc, 128
    ldir
diskread_next:
    ld hl, 0xff82
    inc (hl)
    ld a, (hl)
    sub 32
    or a
    jr nz, diskread_loop
    ld a, (0xff81)
    or a
    jr nz, diskread_finish
    inc a
    ld (0xff82), a
    ld a, 0x10
    call floppy_command
    xor a
    ld (0xff82), a
    inc a
    ld (0xff81), a
    jr diskread_loop
diskread_finish:
    ld a, 0x20
    call floppy_command
    jp 0xd400
yield:
    out (0x03), a
    jr yield

print_string: ; IN: HL=Address
    ld a, 0x01    ; Switch to the Teletype
    out (0x00), a
ps_loop:
    ld a, (hl)
    or a
    jr z, ps_after
    ld (0xff80), a
    inc hl
    jr ps_loop
ps_after:
    ret

beep:
    out (0x02), a
    out (0x03), a
    out (0x03), a
    out (0x03), a
    out (0x03), a
    ret

floppy_command:
    ld (0xff80), a
floppy_loop:
    ld a, (0xff80)
    and 0x01
    jr z, floppy_ret
    out (0x03), a ; Wait 1 tick
    jr floppy_loop
floppy_ret:
    ld a, (0xff80)
    ret

booting: db "Booting", 10, 0
nodisk: db "No Disk?", 10, 0
commfail: db "Comm Fail!", 10, 0
ds 0xff00 - $