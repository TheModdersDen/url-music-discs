package urlmusicdiscs;

import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.floatprovider.ConstantFloatProvider;
import org.jetbrains.annotations.Nullable;

public class FileSound implements SoundInstance {
    public String fileUrl;
    public Vec3d position;

    /**
     * Get the identifier of the sound.
     * @return Identifier the identifier of the sound
     */
    @Override
    public Identifier getId() {
        return new Identifier(URLMusicDiscs.MOD_ID, "customsound/" + fileUrl);
    }

    /**
     * @param soundManager the sound manager
     * @return WeightedSoundSet the sound set
     */
    @Nullable
    @Override
    public WeightedSoundSet getSoundSet(SoundManager soundManager) {
        return new WeightedSoundSet(getId(), null);
    }

    /**
     * Get the sound to be played.
     * @return Sound the sound to be played
     */
    @Override
    public Sound getSound() {
        return new Sound(getId().toString(), ConstantFloatProvider.create(getVolume()), ConstantFloatProvider.create(getPitch()), 1, Sound.RegistrationType.SOUND_EVENT, true, false, 64);
    }

    /**
     * Get the category of the sound.
     * @return SoundCategory the category of the sound
     */
    @Override
    public SoundCategory getCategory() {
        return SoundCategory.RECORDS;
    }

    /**
     * Get if the sound is repeatable.
     * @return boolean if the sound is repeatable
     */
    @Override
    public boolean isRepeatable() {
        return false;
    }

    /**
     * Get if the sound is relative.
     * @return boolean if the sound is relative
     */
    @Override
    public boolean isRelative() {
        return false;
    }

    /**
     * Get the repeat delay of the sound.
     * @return int the repeat delay of the sound
     */
    @Override
    public int getRepeatDelay() {
        return 0;
    }

    /**
     * Get the volume of the sound (as a float).
     * @return float the volume of the sound.
     */
    @Override
    public float getVolume() {
        return 1;
    }

    /**
     * Get the pitch of the sound (as a float).
     * @return float the pitch of the sound.
     */
    @Override
    public float getPitch() {
        return 1;
    }

    /**
     * Get the X coordinate of the sound.
     * @return int the X coordinate of the sound
     */
    @Override
    public double getX() {
        return position.x;
    }

    /**
     * Get the Y coordinate of the sound.
     * @return int the Y coordinate of the sound
     */
    @Override
    public double getY() {
        return position.y;
    }

    /**
     * Get the Z coordinate of the sound.
     * @return int the Z coordinate of the sound
     */
    @Override
    public double getZ() {
        return position.z;
    }

    /**
     * Get the attenuation type of the sound.
     * @return AttenuationType the attenuation type of the sound
     */
    @Override
    public AttenuationType getAttenuationType() {
        return SoundInstance.AttenuationType.LINEAR;
    }
}
