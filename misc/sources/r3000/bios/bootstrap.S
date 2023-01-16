# RAVENSTONE SYSTEM FIRMWARE FOR MIPS R3000
# COPYRIGHT (C) 2023 TalonFox, LICENSED UNDER THE MIT LICENSE.
# SEE LICENSE IN THE ROOT OF THIS REPOSITORY FOR MORE INFORMATION

.section .text.reset
.global _ResetVector
.type _ResetVector, @function
.set noreorder
_ResetVector:
    j _RvFmInitialize # Reset Vector
    nop
.set reorder

_RvFmInitialize:
    # At the current CPU State, there are a few things which are in an invalid state.
    # For Instance:
    #  - The L1 Instruction and Data Caches are in an unknown state.
    #  - The TLB is in an unknown state.
    # Most CPUs would handle this stuff in hardware when the reset pin is high,
    # but MIPS does not, it requires software to do this.
    #
    # You may be wondering how we are able to execute code if the L1 Caches and the TLB isn't setup yet.
    # The answer is that the address ranges 0xa0000000-0xbfffffff (known as kseg1 in MIPS)
    # doesn't use the L1 Caches or the TLB, it allows you to access the first 512 MiB of physical memory
    # directly (which bypasses the need for the L1 Caches) if you are in kernel mode, which we are at reset.
    # This is why we can execute code, even with this stuff not working.
    # Addresses 0x80000000-0x9fffffff (known as kseg0 in MIPS) is the same as kseg1 but it uses the L1 Caches.

    mtc0 $zero, $13 # Set the Cause Register to Zero

    # We need to reset the L1 Caches, heres how we do this.
    # In the Status register within Coprocessor 0, there are two flags which we must use in order to do this:
    #     SwC & IsC
    # Setting IsC isolates the cache from physical memory allowing you to directly access the cache lines
    # by reading and writing to kseg0.
    # SwC makes it access the Instruction Cache intead of the Data Cache when IsC is set.
    # We can use these to invalidate all of the cache lines, putting the L1 Caches into a safe state.

    mfc0 $t0, $12 # Save the status register to $t0
    or $v0, $t0, 0x10000 # Isolate the Data Cache
    mtc0 $v0, $12

    # Afterwards jump to the entry code
    li $sp, 0xa00ffffc
    .extern main
    jal main
    # The BIOS has finished, enter into the code within the first track
    li $t0, 0x800fe000
    jr $t0