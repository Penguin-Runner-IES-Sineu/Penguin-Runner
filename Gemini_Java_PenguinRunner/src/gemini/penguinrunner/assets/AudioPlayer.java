package gemini.penguinrunner.assets;

import javax.sound.sampled.*;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Preloads and manages audio clips and background music with logarithmic decibel volume control.
 */
public class AudioPlayer {
    private final Map<String, byte[]> soundData = new HashMap<>();
    private final Map<String, AudioFormat> soundFormats = new HashMap<>();
    private Clip musicClip = null;
    private float volume = 0.85f;
    private boolean muted = false;

    public void loadSound(String name, File file) {
        if (!file.exists()) return;
        try (AudioInputStream ais = AudioSystem.getAudioInputStream(file)) {
            byte[] data = ais.readAllBytes();
            soundData.put(name, data);
            soundFormats.put(name, ais.getFormat());
        } catch (Exception ignored) {}
    }

    public void playSound(String name) {
        if (muted) return;
        byte[] data = soundData.get(name);
        AudioFormat format = soundFormats.get(name);
        if (data == null || format == null) return;

        try {
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(format, data, 0, data.length);
            applyVolumeToClip(clip, volume);
            clip.start();
        } catch (Exception ignored) {}
    }

    public void playMusic(File file) {
        stopMusic();
        if (!file.exists()) return;
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(file);
            musicClip = AudioSystem.getClip();
            musicClip.open(ais);
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            applyVolumeToClip(musicClip, muted ? 0f : volume);
            musicClip.start();
        } catch (Exception ignored) {}
    }

    public void stopMusic() {
        if (musicClip != null) {
            musicClip.stop();
            musicClip.close();
            musicClip = null;
        }
    }

    public float getVolume() { return volume; }

    public void setVolume(float vol) {
        this.volume = Math.max(0f, Math.min(1f, vol));
        if (musicClip != null && !muted) {
            applyVolumeToClip(musicClip, volume);
        }
    }

    public boolean isMuted() { return muted; }

    public void setMuted(boolean mute) {
        this.muted = mute;
        if (musicClip != null) {
            applyVolumeToClip(musicClip, muted ? 0f : volume);
        }
    }

    private void applyVolumeToClip(Clip clip, float vol) {
        if (clip == null) return;
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                if (vol <= 0.0001f) {
                    gain.setValue(gain.getMinimum());
                } else {
                    float dB = (float) (Math.log10(vol) * 20.0);
                    gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB)));
                }
            }
        } catch (Exception ignored) {}
    }
}

