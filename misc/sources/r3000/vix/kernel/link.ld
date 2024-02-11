OUTPUT_FORMAT(elf32-littlemips)
ENTRY(BootloaderEntry)

SECTIONS {
    . = 0x800fe000;

    .text.entry : {
        *(.text.entry);
    }

    .text : {
    	__TEXT_BEGIN__ = .;
        *(.text .text.*);
    	__TEXT_END__ = .;
    }

    .rodata : AT(ADDR(.text) + SIZEOF(.text)) {
        __RODATA_BEGIN__ = .;
        *(.rodata .rodata.* .rdata .rdata.*)
        __RODATA_END__ = .;
    }

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
        . = ALIGN(4096);
    }
}