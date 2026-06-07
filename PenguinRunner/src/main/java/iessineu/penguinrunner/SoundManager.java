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
    private String collectSFX = "collect.wav";
    private String doorSFX = "door.wav";
    private String flameSFX = "flame.wav";
    private String pickupSFX = "pickup.wav";
    private String teleportSFX = "teleport.wav";
    private String breakSFX = "break.wav";
    private String gameOverSFX = "gameover.wav";
    private String deathSFX = "death.wav";
    private String musicPath = "music.wav";
    private Map<String, String> originalMap = generateSoundMap(); //tipo, ruta
    private Map<String, String> soundsMap = generateSoundMap(); //tipo, ruta

    public void playMusic(boolean fromResource) {
        this.setVolume(0.3f);
        boolean modded = false;
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

    public float getVolume() {
        if (musicClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl
                    = (FloatControl) musicClip.getControl(FloatControl.Type.MASTER_GAIN);

            return gainControl.getValue();
        }
        return 0f;
    }

    public void playSound(String type, boolean fromResource) {
        boolean modded = false;
        ClassLoader classLoader = PenguinRunner.class.getClassLoader();
        String path = soundsMap.get(type);
        if (!soundsMap.get(type).equals(originalMap.get(type))) {
            modded = true;
            fromResource = false;
        }
        InputStream collectStream = new BufferedInputStream(classLoader.getResourceAsStream("collect.wav"));
        InputStream doorStream = new BufferedInputStream(classLoader.getResourceAsStream("door.wav"));
        InputStream flameStream = new BufferedInputStream(classLoader.getResourceAsStream("flame.wav"));
        InputStream pickupStream = new BufferedInputStream(classLoader.getResourceAsStream("pickup.wav"));
        InputStream teleportStream = new BufferedInputStream(classLoader.getResourceAsStream("teleport.wav"));
        InputStream breakStream = new BufferedInputStream(classLoader.getResourceAsStream("break.wav"));
        InputStream deathStream = new BufferedInputStream(classLoader.getResourceAsStream("death.wav"));
        InputStream gameOverStream = new BufferedInputStream(classLoader.getResourceAsStream("gameover.wav"));
        System.out.print("Caregant audio: ");
        try {
            AudioInputStream audioStream = null;
            if (fromResource) {
                System.out.println("Carregant Recurs");
                switch (type) {
                    case "collect" -> {
                        audioStream = AudioSystem.getAudioInputStream(collectStream);
                    }
                    case "door" -> {
                        audioStream = AudioSystem.getAudioInputStream(doorStream);
                    }
                    case "flame" -> {
                        audioStream = AudioSystem.getAudioInputStream(flameStream);
                    }
                    case "pickup" -> {
                        audioStream = AudioSystem.getAudioInputStream(pickupStream);
                    }
                    case "teleport" -> {
                        audioStream = AudioSystem.getAudioInputStream(teleportStream);
                    }
                    case "break" -> {
                        audioStream = AudioSystem.getAudioInputStream(breakStream);
                    }
                    case "death" -> {
                        audioStream = AudioSystem.getAudioInputStream(deathStream);
                    }
                    case "gameover" -> {
                        audioStream = AudioSystem.getAudioInputStream(gameOverStream);
                    }
                }
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
        soundMap.put("collect", collectSFX);
        soundMap.put("door", doorSFX);
        soundMap.put("flame", flameSFX);
        soundMap.put("pickup", pickupSFX);
        soundMap.put("teleport", teleportSFX);
        soundMap.put("break", breakSFX);
        soundMap.put("death", deathSFX);
        soundMap.put("gameover", gameOverSFX);
        // System.out.println(soundMap);
        return soundMap;
    }

    public String getMusicPath() {
        return musicPath;
    }

    public void setMusicPath(String musicPath) {
        this.musicPath = musicPath;
    }

    public String getCollectSFX() {
        return collectSFX;
    }

    public void setCollectedPath(String collectSFX) {
        this.collectSFX = collectSFX;
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

    public String getDoorSFX() {
        return doorSFX;
    }

    public void setDoorSFX(String doorSFX) {
        this.doorSFX = doorSFX;
    }

    public String getFlameSFX() {
        return flameSFX;
    }

    public void setFlameSFX(String flameSFX) {
        this.flameSFX = flameSFX;
    }

    public String getPickupSFX() {
        return pickupSFX;
    }

    public void setPickupSFX(String pickupSFX) {
        this.pickupSFX = pickupSFX;
    }

    public String getTeleportSFX() {
        return teleportSFX;
    }

    public void setTeleportSFX(String teleportSFX) {
        this.teleportSFX = teleportSFX;
    }

    public String getBreakSFX() {
        return breakSFX;
    }

    public void setBreakSFX(String breakSFX) {
        this.breakSFX = breakSFX;
    }

    public String getGameOverSFX() {
        return gameOverSFX;
    }

    public void setGameOverSFX(String gameOverSFX) {
        this.gameOverSFX = gameOverSFX;
    }

    public String getDeathSFX() {
        return deathSFX;
    }

    public void setDeathSFX(String deathSFX) {
        this.deathSFX = deathSFX;
    }
}
