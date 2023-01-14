mipsel-elf-gcc -G0 -msoft-float -Os -mips1 -march=r3000 *.S src/*.c -Tlink.ld -o BIOS.elf -nostdlib
mipsel-elf-objcopy BIOS.elf -O binary BIOS
# rm --force BIOS.elf