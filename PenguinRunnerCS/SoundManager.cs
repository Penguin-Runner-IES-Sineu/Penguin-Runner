using Raylib_cs;

namespace iessineu.penguinrunner;

public class SoundManager
{
    private Music? musicStream = null;
    private bool isMusicPlaying = false;
    private float currentVolume = 0.95f;

    private string buttonSFX = "button.wav";
    private string popIceSFX = "popice.wav";
    private string collectSFX = "collect.wav";
    private string doorSFX = "door.wav";
    private string flameSFX = "flame.wav";
    private string pickupSFX = "pickup.wav";
    private string teleportSFX = "teleport.wav";
    private string breakSFX = "break.wav";
    private string gameOverSFX = "gameover.wav";
    private string deathSFX = "death.wav";
    private string musicPath = "music.wav";

    private Dictionary<string, string> originalMap;
    private Dictionary<string, string> soundsMap;

    private static readonly Dictionary<string, Sound> soundCache = new();

    public SoundManager()
    {
        EnsureAudioInitialized();
        originalMap = generateSoundMap();
        soundsMap = generateSoundMap();
    }

    public static void EnsureAudioInitialized()
    {
        if (!Raylib.IsAudioDeviceReady())
        {
            try
            {
                Raylib.InitAudioDevice();
            }
            catch (Exception ex)
            {
                Console.WriteLine("Could not init audio device: " + ex.Message);
            }
        }
    }

    private string ResolveAudioPath(string filename, bool fromResource, bool modded)
    {
        string path;
        if (fromResource)
        {
            path = Path.Combine("resources", filename);
        }
        else
        {
            if (modded)
            {
                path = filename;
            }
            else
            {
                path = Path.Combine(GamePanel.getFolderPath(), filename);
            }
        }

        if (!File.Exists(path))
        {
            // Try base directory or project directory
            string alt = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, path);
            if (File.Exists(alt)) path = alt;
            else
            {
                alt = Path.Combine(Directory.GetCurrentDirectory(), "PenguinRunnerCS", path);
                if (File.Exists(alt)) path = alt;
            }
        }

        return path;
    }

    public void playMusic(bool fromResource)
    {
        EnsureAudioInitialized();
        stopMusic();

        bool modded = soundsMap.TryGetValue("music", out var soundFile) &&
                      originalMap.TryGetValue("music", out var origFile) &&
                      soundFile != origFile;

        string path = soundsMap.GetValueOrDefault("music", musicPath);
        string resolvedPath = ResolveAudioPath(path, fromResource, modded);

        Console.Write("Carregant audio: ");
        if (fromResource)
        {
            Console.WriteLine("Carregant Recurs (" + resolvedPath + ")");
        }
        else
        {
            Console.WriteLine("Carregant Fitxer (" + resolvedPath + ")");
        }

        if (File.Exists(resolvedPath))
        {
            try
            {
                var mus = Raylib.LoadMusicStream(resolvedPath);
                if (Raylib.IsMusicValid(mus))
                {
                    musicStream = mus;
                    Raylib.PlayMusicStream(mus);
                    Raylib.SetMusicVolume(mus, currentVolume);
                    isMusicPlaying = true;
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine("Error obrint l'arxiu d'audio! " + resolvedPath + ": " + ex.Message);
            }
        }
        else
        {
            Console.WriteLine("No s'ha trobat l'arxiu de música: " + resolvedPath);
        }
    }

    public void PlayMusic(bool fromResource) => playMusic(fromResource);

    public void updateMusic()
    {
        if (musicStream.HasValue && isMusicPlaying)
        {
            Raylib.UpdateMusicStream(musicStream.Value);
        }
    }

    public void UpdateMusic() => updateMusic();

    public void stopMusic()
    {
        if (musicStream.HasValue)
        {
            try
            {
                Raylib.StopMusicStream(musicStream.Value);
                Raylib.UnloadMusicStream(musicStream.Value);
            }
            catch
            {
                // ignore unload errors
            }
            musicStream = null;
            isMusicPlaying = false;
        }
    }

    public void StopMusic() => stopMusic();

    public void setVolume(float volume)
    {
        currentVolume = Math.Clamp(volume, 0f, 1f);
        if (musicStream.HasValue)
        {
            Raylib.SetMusicVolume(musicStream.Value, currentVolume);
        }
    }

    public void SetVolume(float volume) => setVolume(volume);

    public float getVolume() => currentVolume;
    public float GetVolume() => currentVolume;

    public void playSound(string type, bool fromResource)
    {
        EnsureAudioInitialized();

        bool modded = false;
        if (!soundsMap.TryGetValue(type, out string? path))
        {
            path = type + ".wav";
        }

        if (originalMap.TryGetValue(type, out var orig) && path != orig)
        {
            modded = true;
            fromResource = false;
        }

        string resolvedPath = ResolveAudioPath(path, fromResource, modded);

        if (!File.Exists(resolvedPath))
        {
            return;
        }

        try
        {
            if (!soundCache.TryGetValue(resolvedPath, out var sound))
            {
                sound = Raylib.LoadSound(resolvedPath);
                if (sound.FrameCount > 0)
                {
                    soundCache[resolvedPath] = sound;
                }
            }

            if (sound.FrameCount > 0)
            {
                Raylib.SetSoundVolume(sound, currentVolume);
                Raylib.PlaySound(sound);
            }
        }
        catch (Exception ex)
        {
            Console.WriteLine("Error reproduint so " + resolvedPath + ": " + ex.Message);
        }
    }

    public void PlaySound(string type, bool fromResource) => playSound(type, fromResource);

    public Dictionary<string, string> generateSoundMap()
    {
        return new Dictionary<string, string>
        {
            ["music"] = musicPath,
            ["collect"] = collectSFX,
            ["door"] = doorSFX,
            ["flame"] = flameSFX,
            ["pickup"] = pickupSFX,
            ["teleport"] = teleportSFX,
            ["break"] = breakSFX,
            ["death"] = deathSFX,
            ["gameover"] = gameOverSFX,
            ["popice"] = popIceSFX,
            ["button"] = buttonSFX
        };
    }

    public string getMusicPath() => musicPath;
    public void setMusicPath(string musicPath) => this.musicPath = musicPath;

    public string getCollectSFX() => collectSFX;
    public void setCollectedPath(string collectSFX) => this.collectSFX = collectSFX;

    public Dictionary<string, string> getSoundsMap() => soundsMap;
    public void setSoundsMap(Dictionary<string, string> soundsMap) => this.soundsMap = soundsMap;

    public Dictionary<string, string> getOriginalMap() => originalMap;
    public void setOriginalMap(Dictionary<string, string> originalMap) => this.originalMap = originalMap;

    public string getDoorSFX() => doorSFX;
    public void setDoorSFX(string doorSFX) => this.doorSFX = doorSFX;

    public string getFlameSFX() => flameSFX;
}
