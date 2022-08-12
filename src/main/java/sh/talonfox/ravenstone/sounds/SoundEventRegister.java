package sh.talonfox.ravenstone.sounds;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class SoundEventRegister {
    public static final Identifier DISKETTE_INSERT_ID = new Identifier("ravenstone:diskette_insert");
    public static SoundEvent DISKETTE_INSERT_SOUND_EVENT = new SoundEvent(DISKETTE_INSERT_ID);
    public static final Identifier DISKETTE_EJECT_ID = new Identifier("ravenstone:diskette_eject");
    public static SoundEvent DISKETTE_EJECT_SOUND_EVENT = new SoundEvent(DISKETTE_EJECT_ID);
    public static final Identifier DISKETTE_SPIN_ID = new Identifier("ravenstone:diskette_drive_spin");
    public static SoundEvent DISKETTE_SPIN_SOUND_EVENT = new SoundEvent(DISKETTE_SPIN_ID);
    public static final Identifier DISKETTE_START_ID = new Identifier("ravenstone:diskette_drive_start");
    public static SoundEvent DISKETTE_START_SOUND_EVENT = new SoundEvent(DISKETTE_START_ID);
    public static final Identifier DISKETTE_SEEK_TINY0_ID = new Identifier("ravenstone:diskette_drive_seek_tiny0");
    public static SoundEvent DISKETTE_SEEK_TINY0_EVENT = new SoundEvent(DISKETTE_SEEK_TINY0_ID);
    public static final Identifier DISKETTE_SEEK_TINY1_ID = new Identifier("ravenstone:diskette_drive_seek_tiny1");
    public static SoundEvent DISKETTE_SEEK_TINY1_EVENT = new SoundEvent(DISKETTE_SEEK_TINY1_ID);
    public static final Identifier DISKETTE_SEEK_TINY2_ID = new Identifier("ravenstone:diskette_drive_seek_tiny2");
    public static SoundEvent DISKETTE_SEEK_TINY2_EVENT = new SoundEvent(DISKETTE_SEEK_TINY2_ID);
    public static final Identifier DISKETTE_SEEK_TINY3_ID = new Identifier("ravenstone:diskette_drive_seek_tiny3");
    public static SoundEvent DISKETTE_SEEK_TINY3_EVENT = new SoundEvent(DISKETTE_SEEK_TINY3_ID);
    public static final Identifier DISKETTE_SEEK_LARGE_ID = new Identifier("ravenstone:diskette_drive_seek_large");
    public static SoundEvent DISKETTE_SEEK_LARGE_EVENT = new SoundEvent(DISKETTE_SEEK_LARGE_ID);

    public static void Initialize() {
        Registry.register(Registry.SOUND_EVENT, DISKETTE_INSERT_ID, DISKETTE_INSERT_SOUND_EVENT);
        Registry.register(Registry.SOUND_EVENT, DISKETTE_EJECT_ID, DISKETTE_EJECT_SOUND_EVENT);
        Registry.register(Registry.SOUND_EVENT, DISKETTE_SPIN_ID, DISKETTE_SPIN_SOUND_EVENT);
        Registry.register(Registry.SOUND_EVENT, DISKETTE_START_ID, DISKETTE_START_SOUND_EVENT);
        Registry.register(Registry.SOUND_EVENT, DISKETTE_SEEK_TINY0_ID, DISKETTE_SEEK_TINY0_EVENT);
        Registry.register(Registry.SOUND_EVENT, DISKETTE_SEEK_TINY1_ID, DISKETTE_SEEK_TINY1_EVENT);
        Registry.register(Registry.SOUND_EVENT, DISKETTE_SEEK_TINY2_ID, DISKETTE_SEEK_TINY2_EVENT);
        Registry.register(Registry.SOUND_EVENT, DISKETTE_SEEK_TINY3_ID, DISKETTE_SEEK_TINY3_EVENT);
        Registry.register(Registry.SOUND_EVENT, DISKETTE_SEEK_LARGE_ID, DISKETTE_SEEK_LARGE_EVENT);
    }
}
