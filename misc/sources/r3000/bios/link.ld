OUTPUT_FORMAT(elf32-littlemips)
ENTRY(_ResetVector)

SECTIONS {
    . = 0xbfc00000;

    .text.reset : {
    	__TEXT_RESET_BEGIN__ = .;
    	*(.text.reset);
    	__TEXT_RESET_END__ = .;
    }

    /* . = __TEXT_RESET_END__-0x20000000; */

    .text : AT(__TEXT_RESET_END__) {
    	__TEXT_BEGIN__ = .;
        *(.text .text.*);
    	__TEXT_END__ = .;
    }

    .rodata : AT(ADDR(.text) + SIZEOF(.text)) {
        __RODATA_BEGIN__ = .;
        *(.rodata .rodata.* .rdata .rdata.*)
        __RODATA_END__ = .;
    }

    . = 0x80000010;

    .data : AT(__RODATA_END__) {
        *(.data)
        *(.data.*)
        *(.sdata)
    }
    __DATA_BEGIN__ = __RODATA_END__;
    __DATA_END__ = __RODATA_END__ + SIZEOF(.data);

    .bss (NOLOAD) : AT(ADDR(.data) + SIZEOF(.data)) {
        __BSS_BEGIN__ = .;
        *(.bss)
        *(.bss.*)
        *(.sbss)
        __BSS_END__ = .;
    }
}