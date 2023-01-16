package sh.talonfox.ravenstone.sounds;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;

public class SoundEventRegister {
    public static final Identifier COMPUTER_BEEP_ID = new Identifier("ravenstone:computer_beep");
    public static SoundEvent COMPUTER_BEEP_EVENT = SoundEvent.of(COMPUTER_BEEP_ID);
    public static final Identifier DISKETTE_INSERT_ID = new Identifier("ravenstone:diskette_insert");
    public static SoundEvent DISKETTE_INSERT_SOUND_EVENT = SoundEvent.of(DISKETTE_INSERT_ID);
    public static final Identifier DISKETTE_EJECT_ID = new Identifier("ravenstone:diskette_eject");
    public static SoundEvent DISKETTE_EJECT_SOUND_EVENT = SoundEvent.of(DISKETTE_EJECT_ID);
    public static final Identifier DISKETTE_SPIN_ID = new Identifier("ravenstone:diskette_drive_spin");
    public static SoundEvent DISKETTE_SPIN_SOUND_EVENT = SoundEvent.of(DISKETTE_SPIN_ID);
    public static final Identifier DISKETTE_START_ID = new Identifier("ravenstone:diskette_drive_start");
    public static SoundEvent DISKETTE_START_SOUND_EVENT = SoundEvent.of(DISKETTE_START_ID);
    public static final Identifier DISKETTE_SEEK_TINY0_ID = new Identifier("ravenstone:diskette_drive_seek_tiny0");
    public static SoundEvent DISKETTE_SEEK_TINY0_EVENT = SoundEvent.of(DISKETTE_SEEK_TINY0_ID);
    public static final Identifier DISKETTE_SEEK_TINY1_ID = new Identifier("ravenstone:diskette_drive_seek_tiny1");
    public static SoundEvent DISKETTE_SEEK_TINY1_EVENT = SoundEvent.of(DISKETTE_SEEK_TINY1_ID);
    public static final Identifier DISKETTE_SEEK_TINY2_ID = new Identifier("ravenstone:diskette_drive_seek_tiny2");
    public static SoundEvent DISKETTE_SEEK_TINY2_EVENT = SoundEvent.of(DISKETTE_SEEK_TINY2_ID);
    public static final Identifier DISKETTE_SEEK_TINY3_ID = new Identifier("ravenstone:diskette_drive_seek_tiny3");
    public static SoundEvent DISKETTE_SEEK_TINY3_EVENT = SoundEvent.of(DISKETTE_SEEK_TINY3_ID);
    public static final Identifier DISKETTE_SEEK_LARGE_ID = new Identifier("ravenstone:diskette_drive_seek_large");
    public static SoundEvent DISKETTE_SEEK_LARGE_EVENT = SoundEvent.of(DISKETTE_SEEK_LARGE_ID);

    public static final Identifier HARD_DRIVE_IDLE_ID = new Identifier("ravenstone:hard_drive_idle");
    public static SoundEvent HARD_DRIVE_IDLE_EVENT = SoundEvent.of(HARD_DRIVE_IDLE_ID);
    public static final Identifier HARD_DRIVE_SEEK_ID = new Identifier("ravenstone:hard_drive_seek");
    public static SoundEvent HARD_DRIVE_SEEK_EVENT = SoundEvent.of(HARD_DRIVE_SEEK_ID);
    public static final Identifier HARD_DRIVE_SEEK_SHORT_ID = new Identifier("ravenstone:hard_drive_seek_short");
    public static SoundEvent HARD_DRIVE_SEEK_SHORT_EVENT = SoundEvent.of(HARD_DRIVE_SEEK_SHORT_ID);
    public static final Identifier HARD_DRIVE_STARTUP_ID = new Identifier("ravenstone:hard_drive_startup");
    public static SoundEvent HARD_DRIVE_STARTUP_EVENT = SoundEvent.of(HARD_DRIVE_STARTUP_ID);
    public static final Identifier HARD_DRIVE_SPINDOWN_ID = new Identifier("ravenstone:hard_drive_spindown");
    public static SoundEvent HARD_DRIVE_SPINDOWN_EVENT = SoundEvent.of(HARD_DRIVE_SPINDOWN_ID);

    public static void Initialize() {
        Registry.register(Registries.SOUND_EVENT, COMPUTER_BEEP_ID, COMPUTER_BEEP_EVENT);
        Registry.register(Registries.SOUND_EVENT, DISKETTE_INSERT_ID, DISKETTE_INSERT_SOUND_EVENT);
        Registry.register(Registries.SOUND_EVENT, DISKETTE_EJECT_ID, DISKETTE_EJECT_SOUND_EVENT);
        Registry.register(Registries.SOUND_EVENT, DISKETTE_SPIN_ID, DISKETTE_SPIN_SOUND_EVENT);
        Registry.register(Registries.SOUND_EVENT, DISKETTE_START_ID, DISKETTE_START_SOUND_EVENT);
        Registry.register(Registries.SOUND_EVENT, DISKETTE_SEEK_TINY0_ID, DISKETTE_SEEK_TINY0_EVENT);
        Registry.register(Registries.SOUND_EVENT, DISKETTE_SEEK_TINY1_ID, DISKETTE_SEEK_TINY1_EVENT);
        Registry.register(Registries.SOUND_EVENT, DISKETTE_SEEK_TINY2_ID, DISKETTE_SEEK_TINY2_EVENT);
        Registry.register(Registries.SOUND_EVENT, DISKETTE_SEEK_TINY3_ID, DISKETTE_SEEK_TINY3_EVENT);
        Registry.register(Registries.SOUND_EVENT, DISKETTE_SEEK_LARGE_ID, DISKETTE_SEEK_LARGE_EVENT);
    }
}
