OUTPUT_FORMAT(elf32-littlemips)
ENTRY(_ResetVector)

SECTIONS {
    . = 0x80000000;

    .text.reset : {
        *(.text.reset);
    }

    .text : {
        *(.text .text.*);
    }

    .rodata : {
        *(.rodata .rodata.* .rdata .rdata.*)
    }

    .data : {
        *(.data)
        *(.data.*)
        *(.sdata)
    }

    .bss : {
        *(.bss)
        *(.bss.*)
        *(.sbss)
    }
}