.section .text.reset
.global _ResetVector
.type _ResetVector, @function
.set noreorder
_ResetVector:
    .extern kmain
    jal kmain # Reset Vector
    nop
    nop
    nop
    nop
    nop
    nop
    nop

    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop

    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop

    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop

    .extern kcall
    jal kcall # Syscall Vector
    nop
.set reorder