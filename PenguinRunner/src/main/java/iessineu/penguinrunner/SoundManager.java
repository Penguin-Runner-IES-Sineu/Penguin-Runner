/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner;

/**
 *
 * @author loren
 */
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SoundManager implements Serializable {

    private transient Clip musicClip;

    public void playMusic(String path, boolean fromResource) {
        stopMusic();
        ClassLoader classLoader = PenguinRunner.class.getClassLoader();
        InputStream musicStream = new BufferedInputStream(classLoader.getResourceAsStream("music.wav"));
        System.out.print("Caregant audio: ");
        try {
            AudioInputStream audioStream = null;
            if (fromResource) {
                System.out.println("Carregant Recurs");
                audioStream = AudioSystem.getAudioInputStream(musicStream);
            } else {
                System.out.println("Carregant Fitxer");
                File soundFile = new File(path);
                audioStream = AudioSystem.getAudioInputStream(soundFile);
            }

            musicClip = AudioSystem.getClip();
            if (audioStream != null) {
                musicClip.open(audioStream);
                musicClip.loop(Clip.LOOP_CONTINUOUSLY);
                musicClip.start();
            }

        } catch (LineUnavailableException | UnsupportedAudioFileException | IOException ex) {
            System.out.println("Error obrint l'arxiu d'audio!" + path);
            System.getLogger(SoundManager.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
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
            FloatControl gainControl
                    = (FloatControl) musicClip.getControl(FloatControl.Type.MASTER_GAIN);

            // volume entre 0.0f y 1.0f
            float min = gainControl.getMinimum();
            float max = gainControl.getMaximum();

            float gain = min + (max - min) * volume;
            gainControl.setValue(gain);
        }
    }

    public void playSound(String path, boolean fromResource) {
        ClassLoader classLoader = PenguinRunner.class.getClassLoader();
        InputStream musicStream = new BufferedInputStream(classLoader.getResourceAsStream(path));
        System.out.print("Caregant audio: ");
        try {
            AudioInputStream audioStream;
            if (fromResource) {
                System.out.println("Carregant Recurs");
                audioStream = AudioSystem.getAudioInputStream(musicStream);
            } else {
                System.out.println("Carregant Fitxer");
                File soundFile = new File(GamePanel.getFolderPath() + path);
                audioStream = AudioSystem.getAudioInputStream(soundFile);
            }
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (LineUnavailableException | UnsupportedAudioFileException | IOException ex) {
            System.out.println("Error obrint l'arxiu d'audio!" + path);
            System.getLogger(SoundManager.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
}
