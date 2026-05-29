/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner;

/**
 *
 * @author loren
 */

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SoundManager implements Serializable {

    private transient Clip musicClip;

    public void playMusic(String path) {
        stopMusic();

        try {
            File musicFile = new File(path);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);

            musicClip = AudioSystem.getClip();
            musicClip.open(audioStream);

            // Repetir infinitamente
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            musicClip.start();

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
            System.out.println("No se pudo reproducir la música: " + path);
            ex.printStackTrace();
        }
    }

    public void stopMusic() {
        if (musicClip != null) {
            musicClip.stop();
            musicClip.close();
            musicClip = null;
        }
    }

    public void setVolume(float volume) {
        if (musicClip == null) {
            return;
        }

        if (musicClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl =
                    (FloatControl) musicClip.getControl(FloatControl.Type.MASTER_GAIN);

            // volume entre 0.0f y 1.0f
            float min = gainControl.getMinimum();
            float max = gainControl.getMaximum();

            float gain = min + (max - min) * volume;
            gainControl.setValue(gain);
        }
    }
    public void playSound(String path) {
    try {
        File soundFile = new File(path);
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);

        Clip clip = AudioSystem.getClip();
        clip.open(audioStream);
        clip.start();

    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
        System.out.println("No se pudo reproducir el sonido: " + path);
        ex.printStackTrace();
    }
}
}