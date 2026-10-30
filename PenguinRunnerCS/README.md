# Penguin Runner - C# Raylib Port

Reimplementació completa de **Penguin Runner** en C# utilitzant el motor gràfic i d'àudio **Raylib** (a través de `Raylib-cs`).

## Característiques

- **Arquitectura i noms de funcions fidels a l'original en Java**: s'han mantingut els mètodes, classes i estructures de dades (`GameState`, `GamePanel`, `MenuPanel`, `PauseMenuDialog`, `AI`, `IceAutoTileConnector`, `PlayerState`, `SoundManager`, etc.).
- **Motor Raylib**:
  - Renderitzat accelerat per GPU de sprites, fons, HUD i efectes visuals.
  - Menú principal amb fons animat: cel estrellat parpellejant, neu caient, onades d'aigua sinusoïdals i pingüí animat.
  - Menú de mort ("Tornar a intentar") amb fons vermellós.
  - Menú de pausa interactiu amb control de volum mitjançant barra lliscant.
  - Gestió d'àudio nativa amb `Raylib_cs` per música ambiental en bucle i efectes de so (`.wav`).
  - Auto-tiling complet de blocs de gel segons màscara cardinal i diagonal.
  - Editor de mapes ASCII Studio accessible mitjançant el paràmetre `-editor`.

## Com executar

### Requisits
- [.NET SDK 9 o 10](https://dotnet.microsoft.com/)

### Execució bàsica
```bash
dotnet run --project PenguinRunnerCS
```

### Modes addicionals
- **Saltar directament a la partida (sense passar pel menú):**
  ```bash
  dotnet run --project PenguinRunnerCS -- -skip
  ```
- **Obrir l'editor de mapes (ASCII Map Studio):**
  ```bash
  dotnet run --project PenguinRunnerCS -- -editor
  ```
- **Carregar un joc o paquet personalitzat:**
  ```bash
  dotnet run --project PenguinRunnerCS -- -g ices
  ```
- **Executar la suite de proves de verificació:**
  ```bash
  dotnet run --project PenguinRunnerCS -- -test
  ```

## Controls

- **Fletxes / WASD**: Moure el personatge (pujar escales, anar per rails, empènyer pedres).
- **Q**: Rompre bloc de gel a baix a l'esquerra.
- **E**: Rompre bloc de gel a baix a la dreta.
- **F**: Utilitzar ítem seleccionat (Llançaflames, Teleport).
- **Z / X**: Canviar ítem seleccionat a l'inventari (anterior / següent).
- **Espai**: Passar torn.
- **R**: Reiniciar nivell (resta una vida).
- **ESC**: Obrir menú de pausa (Continuar, Volum, Guardar partida, Carregar partida, Sortir).

