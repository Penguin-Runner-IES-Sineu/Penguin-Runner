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
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SoundManager implements Serializable {

    private transient Clip musicClip;
    private String collectedPath = "nyam.wav";
    private String musicPath = "music.wav";
    private Map<String, String> originalMap = generateSoundMap(); //tipo, ruta
    private Map<String, String> soundsMap = generateSoundMap(); //tipo, ruta

    public void playMusic(boolean fromResource) {
        boolean modded = false;
        // stopMusic();
        if (!soundsMap.get("music").equals(originalMap.get("music"))) {
            modded = true;
        }
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
                String path = soundsMap.get("music");
                File soundFile = null;
                if (modded) {
                    soundFile = new File(path);
                } else {
                    soundFile = new File(GamePanel.getFolderPath() + path);
                }
                audioStream = AudioSystem.getAudioInputStream(soundFile);
            }
            musicClip = AudioSystem.getClip();
            if (audioStream != null) {
                musicClip.open(audioStream);
                musicClip.loop(Clip.LOOP_CONTINUOUSLY);
                musicClip.start();
            }

        } catch (LineUnavailableException | UnsupportedAudioFileException | IOException ex) {
            System.out.println("Error obrint l'arxiu d'audio!" + musicPath);
            System.getLogger(SoundManager.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }

    public void stopMusic() {
        if (musicClip != null) {
            musicClip.stop();
            musicClip.flush();
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

    public void playSound(String type, boolean fromResource) {
        boolean modded = false;
        ClassLoader classLoader = PenguinRunner.class.getClassLoader();
        String path = soundsMap.get(type);
        if (!soundsMap.get(type).equals(originalMap.get(type))) {
            modded = true;
            fromResource = false;
        }
        InputStream musicStream = new BufferedInputStream(classLoader.getResourceAsStream("nyam.wav"));
        System.out.print("Caregant audio: ");
        try {
            AudioInputStream audioStream;
            if (fromResource) {
                System.out.println("Carregant Recurs");
                audioStream = AudioSystem.getAudioInputStream(musicStream);
            } else {
                System.out.println("Carregant Fitxer");
                File soundFile = null;
                if (modded) {
                    soundFile = new File(path);
                } else {
                    soundFile = new File(GamePanel.getFolderPath() + path);
                }
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

    public Map<String, String> generateSoundMap() {
        Map<String, String> soundMap = new HashMap();
        soundMap.put("music", musicPath);
        soundMap.put("icecream", collectedPath);
        return soundMap;
    }

    public String getMusicPath() {
        return musicPath;
    }

    public void setMusicPath(String musicPath) {
        this.musicPath = musicPath;
    }

    public String getCollectedPath() {
        return collectedPath;
    }

    public void setCollectedPath(String collectedPath) {
        this.collectedPath = collectedPath;
    }

    public Map<String, String> getSoundsMap() {
        return soundsMap;
    }

    public void setSoundsMap(Map<String, String> soundsMap) {
        this.soundsMap = soundsMap;
    }

    public Map<String, String> getOriginalMap() {
        return originalMap;
    }

    public void setOriginalMap(Map<String, String> originalMap) {
        this.originalMap = originalMap;
    }
}
