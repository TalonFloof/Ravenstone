rm --force bootloader.elf
rm --force kernel.elf
rm -r --force *.o
mipsel-unknown-elf-gcc -G0 -msoft-float -Os -mips1 -ffreestanding -c startup.S bootloader.c
mipsel-unknown-elf-ld *.o -G0 -Tlink.ld -o bootloader.elf -nostdlib
rm -r --force *.o
mipsel-unknown-elf-objcopy bootloader.elf --pad-to 0x800ff000 -O binary vix1_temp.bin
dd if=/dev/zero of=vix1.bin count=256
dd conv=notrunc if=vix1_temp.bin of=vix1.bin
rm -r --force vix1_temp.bin
mipsel-unknown-elf-gcc -G0 -msoft-float -Os -mips1 -ffreestanding -c src/*.S src/*.c
mipsel-unknown-elf-ld *.o -G0 -Tklink.ld -o kernel.elf -nostdlib
rm -r --force *.o
mipsel-unknown-elf-objcopy kernel.elf -O binary vix1_temp.bin
dd conv=notrunc if=vix1_temp.bin of=vix1.bin seek=8
rm -r --force vix1_temp.bin