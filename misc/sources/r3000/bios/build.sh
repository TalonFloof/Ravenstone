mipsel-elf-gcc -msoft-float -Os -c -mips1 -march=r3000 *.S src/*.c
mipsel-elf-ld -Tlink.ld *.o -o BIOS
rm -r --force *.o