# trainingGround — Technical Documentation

**Artifact:** `FlatLand:trainingGround:0.0.1-SNAPSHOT`  
**Build System:** Maven, Java 21  
**Primary Package Root:** multiple top-level packages under `src/main/java/`  
**Total Source:** ~62,000 lines, 298 Java files  
**Project Theme:** E><3 — a recovery-focused, exploration-first game/interpreter hybrid  
**Target Release Date:** October 8, 2026

---

## Table of Contents

1. [High-Level Purpose](#1-high-level-purpose)
2. [Repository Layout](#2-repository-layout)
3. [Architecture Overview](#3-architecture-overview)
4. [Startup & Bootstrap](#4-startup--bootstrap)
5. [FlatLand Game Engine](#5-flatland-game-engine)
   - 5.1 [FlatLander Entity Model](#51-flatlander-entity-model)
   - 5.2 [Physics System](#52-physics-system)
   - 5.3 [Sprite System](#53-sprite-system)
   - 5.4 [View & Rendering](#54-view--rendering)
   - 5.5 [Input Handling](#55-input-handling)
   - 5.6 [Level Loading (XMLLEVELLOADER)](#56-level-loading-xmllevelloader)
6. [PCB — Pocket/Cup/Box Language System](#6-pcb--platformcontainerbox-language-system)
   - 6.1 [What PCB Is](#61-what-pcb-is)
   - 6.2 [Token System](#62-token-system)
   - 6.3 [Scanner (Lexical Analysis)](#63-scanner-lexical-analysis)
   - 6.4 [Grouper (Token Grouping — Second Pass)](#64-grouper-token-grouping--second-pass)
   - 6.5 [Parser & AST](#65-parser--ast)
   - 6.6 [Resolver (Static Analysis)](#66-resolver-static-analysis)
   - 6.7 [Interpreter (Execution Engine)](#67-interpreter-execution-engine)
   - 6.8 [Environment (Variable Scoping)](#68-environment-variable-scoping)
   - 6.9 [Runtime Types & Instances](#69-runtime-types--instances)
     - Pocket Lifetime System
     - Pocket Cascading Death
     - TkpInstance Lifecycle
   - 6.10 [PCB Math Sub-Language](#610-pcb-math-sub-language)
     - Scalar Operations
     - Vector and Matrix Operations
   - 6.11 [Box Orchestrator Class](#611-box-orchestrator-class)
   - 6.12 [SandBox (Restricted Runtime)](#612-sandbox-restricted-runtime)
   - 6.13 [PCBServer (HTTP Interface)](#613-pcbserver-http-interface)
   - 6.14 [Bidirectional Execution](#614-bidirectional-execution)
   - 6.15 [PCB Language Reference Summary](#615-pcb-language-reference-summary)
7. [Monopoly Game Implementation](#7-monopoly-game-implementation)
   - 7.1 [Board](#71-board)
   - 7.2 [Board Spaces & Status](#72-board-spaces--status)
   - 7.3 [Player Management](#73-player-management)
   - 7.4 [MonopolyActions](#74-monopolyactions)
   - 7.5 [Rules Engine](#75-rules-engine)
   - 7.6 [Island System](#76-island-system)
8. [Actions System](#8-actions-system)
9. [Object Classification System (fication)](#9-object-classification-system-fication)
10. [Entity State Systems](#10-entity-state-systems)
    - 10.1 [DEG — Degradation](#101-deg--degradation)
    - 10.2 [DES — Destruction](#102-des--destruction)
    - 10.3 [SP_SUD — Spontaneous Spawn/Destroy](#103-sp_sud--spontaneous-spawndestroy)
    - 10.4 [PRG — Programmable Objects](#104-prg--programmable-objects)
    - 10.5 [PER — Permanent Objects](#105-per--permanent-objects)
    - 10.6 [UPGRD — Upgradable Objects](#106-upgrd--upgradable-objects)
    - 10.7 [ITM — Item System](#107-itm--item-system)
11. [Audio System (audiolizer)](#11-audio-system-audiolizer)
12. [Computer Vision (CV)](#12-computer-vision-cv)
13. [Neuron System (Nuron)](#13-neuron-system-nuron)
14. [Utility & Supporting Systems](#14-utility--supporting-systems)
15. [Design Patterns](#15-design-patterns)
16. [Full Data Flow Diagrams](#16-full-data-flow-diagrams)
17. [Build Configuration & Dependencies](#17-build-configuration--dependencies)
18. [Notable Design Choices & Quirks](#18-notable-design-choices--quirks)
19. [Developer Notes Context](#19-developer-notes-context)

---

## 1. High-Level Purpose

`trainingGround` is simultaneously three things living inside one Maven project:

1. **A 2D tile-based game engine** called *FlatLand*, with entity physics, sprites, levels, and game loop
2. **A custom scripting language interpreter** called *PCB* (Pocket/Cup/Box), complete with scanner, parser, resolver, and interpreter
3. **A Monopoly board game rules engine** embedded in the same runtime

These three systems are not loosely coupled plugins — they are deliberately interwoven. The PCB language can reach into the game world to create, destroy, and move FlatLander entities at runtime. The Monopoly engine uses FlatLand sprites for rendering board spaces. The PCB interpreter is exposed as an HTTP server so it can be driven from browser-based tooling.

The project is in active, pre-release development. Many subsystems are scaffolded but not fully implemented. The developer's own notes describe this as an exploration of "what is active" in language execution, framed around themes of recovery, renewal, and hope.

---

## 2. Repository Layout

```
trainingGround/
├── pom.xml                          # Maven build file
├── src/
│   ├── main/java/
│   │   ├── Actions/                 # Abstract action command objects (17 files)
│   │   ├── animation/               # Animation frames / asset management
│   │   ├── audiolizer/              # MIDI and PCM audio synthesis
│   │   ├── Box/                     # PCB language system (60+ files)
│   │   │   ├── Box/                 # Orchestrator (Box.java thread, Observer)
│   │   │   ├── GameSpaceInterpreter/# PCBServer (HTTP), SandBox, GameSpaceInterpreter
│   │   │   ├── Grouper/             # Second-pass token grouping
│   │   │   ├── Interpreter/         # Core runtime (Interpreter, Environment, etc.)
│   │   │   ├── math/                # Math sub-language (scanner/parser/interpreter)
│   │   │   ├── Parser/              # Legacy parser (ParserOLD.java)
│   │   │   ├── Scanner/             # Lexical analysis (Scanner.java)
│   │   │   ├── Syntax/              # AST node types (Declaration, Expr, Stmt, Fun...)
│   │   │   └── Token/               # Token, TokenType, TTDynamic
│   │   ├── Constructs/              # Point, Construct geometry
│   │   ├── CV/                      # Computer vision / face detection
│   │   ├── DEG/                     # Degradation trait
│   │   ├── DES/                     # Destruction trait
│   │   ├── dialogManagement/        # Dialogue tree system
│   │   ├── Drawing/                 # ImagePile layered rendering
│   │   ├── EnemyMappings/           # Enemy framework
│   │   ├── environment/             # Environmental data
│   │   ├── fication/                # Object classification hierarchy
│   │   │   ├── ENV/                 # Environmental objects (Ground, Platform...)
│   │   │   ├── Objectification/     # Generic objects
│   │   │   └── Personification/     # People/characters
│   │   ├── FlatLand/Physics/        # Gravity, collision, entity types
│   │   ├── FlatLander/              # Entity base class + registry
│   │   ├── flatLand/trainingGround/ # Game implementation core (levels, events, sprites)
│   │   ├── FlatLandStructure/       # ViewableFlatLand world container
│   │   ├── FSM/                     # Finite state machine for game modes
│   │   ├── GameView/                # Window management
│   │   ├── Island/                  # Monopoly island subsystem
│   │   ├── ITM/                     # Item system
│   │   ├── Math/                    # Dice, probability
│   │   ├── MonopolyActions/         # Monopoly action classes (20+ files)
│   │   ├── NonPlayablePlayer/       # NPC base
│   │   ├── Notes/                   # Developer notes (Java file)
│   │   ├── Nuron/                   # Neuron/synapse data structures
│   │   ├── Parser/                  # Shared parser utilities (used by Box)
│   │   ├── PCB/                     # (empty placeholder package)
│   │   ├── PCBDEFINITION/           # (empty placeholder package)
│   │   ├── PER/                     # Permanent object trait
│   │   ├── Playable/                # Game levels 0-12
│   │   ├── Player/                  # Player character
│   │   ├── PossesableNoNPlayablePlayer/ # Possessable NPC
│   │   ├── PRG/                     # Programmable object trait
│   │   ├── resolver/                # Static variable resolver (used by Box)
│   │   ├── Rules/                   # Monopoly board rules engine
│   │   ├── Sprites/                 # Sprite interface + implementations
│   │   ├── SP_SUD/                  # Spontaneous spawn/destruction
│   │   ├── testing/                 # Test utilities
│   │   ├── TheGame/                 # Monopoly board (Board.java, spaces, players)
│   │   ├── theStart/                # Application bootstrap & startup
│   │   │   ├── thePeople/           # Bootstrap-level FlatLander copy
│   │   │   ├── theSpace/            # FlatLandWindow (Swing canvas)
│   │   │   ├── theStuff/            # Synapse, Random, logging helpers
│   │   │   └── theView/             # Camera, GameScreen, KeyBoardHandler
│   │   ├── tool/                    # Code generation tools
│   │   ├── UPGRD/                   # Upgrade trait
│   │   ├── userInput/               # Input event system
│   │   ├── View/                    # Observers, FPS tracking, update cycle
│   │   ├── visualizer/              # JSON visualization utilities
│   │   └── XMLLEVELLOADER/          # XML level file parsing + wrapper types
│   └── test/                        # JUnit / Mockito tests
├── res/
│   ├── level0-3.xml                 # Level definitions
│   ├── Sprites/                     # PNG/JPG sprite assets
│   └── levelManifesto.txt           # Level metadata
├── java.src/                        # Legacy/alternative implementations
│   ├── Box/                         # Older Box interpreter
│   ├── Parser/                      # Parser generator utilities
│   ├── visualizer/                  # JSON visualization tools
│   └── audiolizer/                  # Audio synthesis tools
├── Sprites/                         # Subproject: sprite resource project
├── Monopoly_Island_Index/           # Subproject: Monopoly island data
├── Notes/content.txt                # Developer journal
└── TEST/                            # Test script files for PCB interpreter
```

---

## 3. Architecture Overview

The project's three major systems share the same JVM and are coupled at a handful of explicit integration points.

```
╔══════════════════════════════════════════════════════════════╗
║                      trainingGround JVM                      ║
║                                                              ║
║  ┌────────────────────────────────────────────────────────┐  ║
║  │               FLATLAND GAME ENGINE                     │  ║
║  │                                                        │  ║
║  │  BANG/BootStrap ──► TheStartCamera ──► Game Loop       │  ║
║  │        │                  │               │            │  ║
║  │  ViewableFlatLand    KeyBoardHandler   Physics          │  ║
║  │        │                               │               │  ║
║  │  FlatLandFacebook ◄─────────────── FlatLanders         │  ║
║  │  (entity registry)              (Player, NPP, PNPP)    │  ║
║  │        │                                               │  ║
║  │   Sprite rendering (ImagePile ◄─ Sprite impls)         │  ║
║  └──────────────┬─────────────────────────────────────────┘  ║
║                 │ create/destroy/move entities                ║
║  ┌──────────────▼─────────────────────────────────────────┐  ║
║  │                   PCB INTERPRETER                      │  ║
║  │                                                        │  ║
║  │  Scanner ──► Grouper ──► Parser ──► Resolver           │  ║
║  │                                        │               │  ║
║  │                                   Interpreter          │  ║
║  │                                        │               │  ║
║  │                              Environment (scopes)      │  ║
║  │                              BoxInstance / CupInstance  │  ║
║  │                                                        │  ║
║  │  PCBServer (HTTP :7070) ──► SandBox ──► Interpreter    │  ║
║  └────────────────────────────────────────────────────────┘  ║
║                                                              ║
║  ┌────────────────────────────────────────────────────────┐  ║
║  │              MONOPOLY RULES ENGINE                     │  ║
║  │                                                        │  ║
║  │  Board ──► BoardSpace ──► MonopolyActions (20+)        │  ║
║  │    │             │             │                       │  ║
║  │  Players     SpaceStatus   RuleInterpreter             │  ║
║  │  Portfolios  Connections   BoardRules                  │  ║
║  └────────────────────────────────────────────────────────┘  ║
║                                                              ║
║  ┌──────────────────────────────────────────────────────┐    ║
║  │   CROSS-CUTTING SYSTEMS                              │    ║
║  │   audiolizer (MIDI/PCM)  │  CV (webcam/face detect)  │    ║
║  │   Nuron (synapse graph)  │  Logging.LOG               │    ║
║  └──────────────────────────────────────────────────────┘    ║
╚══════════════════════════════════════════════════════════════╝
```

**Integration points between systems:**

| From | To | How |
|------|----|-----|
| PCB Interpreter | FlatLandFacebook | `FLCreate`, `FLDestroy`, `FLMove`, `FLSetValue` AST nodes that call into the entity registry |
| Monopoly Board | FlatLand sprites | `MonopolySpace` implements the `Sprites` interface, rendered by the FlatLand render pipeline |
| PCBServer | SandBox/Interpreter | HTTP POST `/run` invokes `sandbox.runJson()` |
| BANG (bootstrap) | WebcamUpdater | Starts `WebcamUpdater` on its own thread alongside the game loop |
| Physics | FlatLandFacebook | Calls `getInstance().getFlatlanderFaceBook()` to iterate all entities |

---

## 4. Startup & Bootstrap

### `theStart.BANG` — The Main Entry Point

`BANG.java` is the application's primary `main` method. It is an interactive CLI that prompts the user for parameters before starting the game.

**Startup sequence:**
```
1. Set up LOG with working directory and log file map
2. Prompt user for canvas width (int)
3. Prompt user for canvas height (int)
4. Prompt user for random seed (int, 0–16777215)
5. Prompt user for neuron count (int)
6. Create ViewableFlatLand(width, height, true)
7. Create Canvas and size it
8. Create FlatLandWindow(canvas) — the Swing window
9. Create TheStartCamera(width, height, 0, 0, flatland, seed, nroncount, canvas)
10. Bind keyboard to FlatLandWindow via camera.setKeyBindingsForPlayer()
11. Start WebcamUpdater thread
12. Enter game loop: call camera.takePictureOfFlatLand() + increment world time
```

The game loop is a raw `while(true)` with no explicit frame-rate cap. Frame timing is measured with `System.currentTimeMillis()` but the delta is only commented-out in `System.err` — it is not used to throttle the loop.

### `theStart.BootStrap` — Placeholder Launcher

`BootStrap.java` contains a `main` method where all code is commented out. It originally started multiple `BANG` threads (multi-world experiment). Currently a no-op stub.

### `theStart.thePeople` — Bootstrap-Level Entity Copies

`theStart/thePeople/FlatLander.java` and `FlatLanderFaceBook.java` are **separate copies** of the entity classes that exist at the bootstrap level, used specifically during early startup before the full package hierarchy is available. This is distinct from the canonical `FlatLander/FlatLander.java`. This duplication is a known structural artifact.

### `ViewableFlatLand` — The World Container

Located in `FlatLandStructure/`. This class holds the world dimensions and a time counter. It is passed into `TheStartCamera` and serves as the authoritative reference for the game space dimensions. The `boolean` third constructor argument (`true` in BANG) likely indicates "render mode" but its meaning is not documented internally.

---

## 5. FlatLand Game Engine

### 5.1 FlatLander Entity Model

**Package:** `FlatLander/`  
**Base class:** `FlatLander.java` (abstract)

`FlatLander` is the base for every moveable, renderable, collidable object in the game world.

```java
public abstract class FlatLander implements FlatLanderContract {
    protected Color myColor;
    public int x, y, z;            // World position. z is stubbed for future 3D.
    protected int moveX = 0;       // Per-frame X displacement
    protected int moveY = 0;       // Per-frame Y displacement
    protected int moveZ = 0;       // z velocity (unused)
    protected int time;            // Entity's own tick counter
    protected int actionsPerTimeUnit = 1;
    protected FlatLanderMemory memory;
    public double direction;       // Facing direction in radians
    protected String name;
    public boolean collidable;
    protected boolean shouldPhysicsApply;
    private TypeOfEntity entityType;
    protected int previousX, previousY, previousZ;
    protected FlatLanderClassification classification = UNDETERMINED;
}
```

**Key methods:**

| Method | Behavior |
|--------|----------|
| `update()` | Calls `moveX()` then `moveY()`, increments `time` |
| `moveX()` | `x += moveX` — direct velocity application, no acceleration |
| `moveY()` | `y += moveY` |
| `getMass()` | Returns constant `100.0` — all entities have the same mass |
| `pushToMemory(Point)` | Pushes a world position onto the entity's memory stack |
| `popMemory()` / `peekMemory()` | Stack-based position recall |
| `above(FlatLander)` | (defined on contract) — checks if this entity is above another |

**Design note:** `getMass()` returns a hard-coded constant `100`. This is intentional as a placeholder but means air resistance has no per-entity variation. The `z` axis and `moveZ` are fully stubbed with comments noting "not used in this version, but can be used for 3D later."

#### FlatLanderClassification

```java
public enum FlatLanderClassification {
    UNDETERMINED,
    PLAYER,
    ENEMY,
    NPC
}
```

New entities default to `UNDETERMINED`. Classification is set explicitly after construction.

#### FlatLanderMemory

Each entity has a `FlatLanderMemory` which is a stack of `Construct` objects. This allows entities to record and replay positions — the mechanism used for path following and scripted movement. The memory is commented out in the `FlatLander` constructor (`// memory = new FlatLanderMemory()`) meaning it is inactive by default.

#### FlatLandFacebook — Entity Registry

`FlatLandFacebook` is a **Singleton** that holds a global `ArrayList<FlatLander>` — the authoritative list of all active entities. The Physics system and the PCB interpreter both go through this registry.

```
FlatLandFacebook.getInstance()
    └── getFlatlanderFaceBook() → ArrayList<FlatLander>
```

**Notable:** there are two copies of this class:
- `FlatLander/FlatLandFacebook.java` — the canonical version used by Physics and Interpreter
- `theStart/thePeople/FlatLanderFaceBook.java` — the bootstrap-level copy with slightly different capitalization (`FaceBook` vs `Facebook`)

#### FlatLanderInstance & FlatLanderContract

`FlatLanderInstance` is a wrapper around a `FlatLander` used in contexts where you need to pass an entity by reference with metadata. `FlatLanderContract` is the interface that all FlatLanders implement, defining the minimum API surface.

#### BoundingBox

`FlatLander/BoundingBox.java` defines the axis-aligned bounding box used for collision detection. Each entity that implements `Collidable` carries a `BoundingBox`.

#### Class Hierarchy

```
FlatLanderContract (interface)
        │
   FlatLander (abstract)
   ├── Player                   (theStart/thePeople — bootstrap copy)
   ├── Player                   (Player/ — canonical)
   ├── NPP  (NonPlayablePlayer)
   │   └── NPPInstance
   ├── PNPP (PossesableNoNPlayablePlayer)
   │   └── PNPPInstance
   └── [other entity types via XMLLEVELLOADER wrappers]
        ├── FlatLanderWrper      (XML-loaded generic entity)
        └── PlayerWrper          (XML-loaded player entity)
```

---

### 5.2 Physics System

**Package:** `FlatLand/Physics/`  
**Main class:** `Physics.java`

`Physics` extends `LOG` and implements `Actions.Physics`. It is the engine that applies gravity, detects collisions, and resolves them.

#### Construction

```java
public Physics(double gravity, double airResistance,
               int cameraPosYinFlatland, int cameraHeight)
```

The camera position parameters are accepted but their use inside Physics is not implemented.

#### Fall Distance Calculation

```java
public Integer fallDistance(FlatLander flatLander) {
    double mass = flatLander.getMass();           // always 100
    double time = UpdateTimeSingleton.getInstance().getCurrentTime();
    double acceleration = gravity - (airResistance / mass);
    if (acceleration < 0) acceleration = 0;
    double velocity = acceleration * time;
    double distance = 0.5 * acceleration * Math.pow(time, 2);
    if (distance > 0 && distance < 1) return 1;
    return (int) distance;
}
```

This is the kinematic equation `d = ½at²` with air resistance modeled as a constant deceleration term `airResistance / mass`. Because `getMass()` always returns 100, the effective formula is `d = ½(gravity - airResistance/100) * t²`. Time comes from the global `UpdateTimeSingleton`.

**Design quirk:** The `time` used here is a global clock, not per-entity. This means all entities that have physics applied fall at the same rate at the same time — there is no independent fall tracking per entity.

#### Physics Loop

```java
public void applyPhysics() {
    ArrayList<FlatLander> bookOfFlatLanders =
        FlatLandFacebook.getInstance().getFlatlanderFaceBook();
    for (FlatLander flatLander : bookOfFlatLanders) {
        if (flatLander.shouldPhysicsApply()) {
            Integer fallDistance = fallDistance(flatLander);
            int moveY = flatLander.getMoveY() + fallDistance;
            flatLander.changeMoveYBy(moveY);   // NOTE: adds to itself + fallDistance
            flatLander.updatecurrentBB();
            checkForCollisions(bookOfFlatLanders);
        }
    }
}
```

**Design quirk:** `changeMoveYBy(moveY)` where `moveY = getMoveY() + fallDistance` means the entity's Y velocity is being **added to itself plus gravity** every frame — this is a compounding velocity bug. The Y velocity will accelerate quadratically every frame. This is either intentional extreme gravity or an unintentional double-increment.

#### Collision Detection

`checkForCollisions` is an O(n²) loop over all entity pairs. For each pair `(a, b)`:
1. Checks if both implement `Collidable`
2. Calls `a.collidesWith(b)` or `a.passesThrough(b)`
3. Gets collision side via `a.collidesFrom(b)`
4. Falls back to `a.above(b)` check

#### Collision Resolution

```java
private void resolveCollision(FlatLander mover, FlatLander obstacle, CollisionSide side) {
    int dx = 0, dy = 0;
    switch (side) {
        case TOP    -> dy = -1;
        case BOTTOM -> dy =  1;
        case LEFT   -> dx = -1;
        case RIGHT  -> dx =  1;
    }
    Collidable a = (Collidable) mover;
    Collidable b = (Collidable) obstacle;
    while (a.collidesWith(b)) {
        mover.update();
        obstacle.update();
        mover.setMoveX(dx);
        mover.setMoveY(dy);
        obstacle.setMoveX(-dx);
        obstacle.setMoveY(-dy);
    }
}
```

Resolution pushes both entities apart by 1 unit per direction, calling `update()` on both each iteration. This is a "push apart" resolver, not an impulse-based resolver.

**Design quirk:** The method `some_awesome_function_that_is_totaly_finished_and_not_made_up_oh_hey_look_over_there(...)` exists in Physics.java with an intentionally absurd signature spanning multiple lines of mocked-up variable names. It is a `// TODO Auto-generated method stub` from implementing the `LOG` abstract class and was never given a real implementation.

#### Supporting Types

| Type | Description |
|------|-------------|
| `CollisionSide` | `enum { TOP, BOTTOM, LEFT, RIGHT }` |
| `Collidable` | Interface: `collidesWith()`, `passesThrough()`, `collidesFrom()` |
| `TypeOfEntity` | `enum { PLAYER, ENEMY, PLATFORM, PROJECTILE, ... }` |
| `UpdateTimeSingleton` | Global monotonic time counter, accessed by Physics for fall calculations |

---

### 5.3 Sprite System

**Packages:** `Sprites/`, `flatLand/trainingGround/Sprites/`

The `Sprites` interface defines how visual representations update and produce `BufferedImage` frames.

```java
public interface Sprites {
    BufferedImage update(FlatLander actor);
    BufferedImage update(String key, boolean gameMode, boolean prompt);
    int getWidth();
    int getHeight();
    void updateState();
}
```

The two `update` overloads reflect the two rendering contexts: entity-driven (in-game rendering) vs. key-driven (UI/menu rendering).

#### Sprite Implementations

| Class | Description |
|-------|-------------|
| `GenericSprite` | Fallback renderer |
| `Skeleton` | Skeletal enemy sprite |
| `SkeletonTwo` | Variant skeleton with different animation |
| `ZombieBaby` | Zombie enemy sprite |
| `Collectible` | Pickup item sprite |
| `Destructibles` | Breakable object sprite |
| `ItemObject` | Wrapper for item rendering |
| `Item` | Item interface |
| `MonopolySpace` | Renders a Monopoly board square; bridges Monopoly and FlatLand rendering |
| `SceneObject` | Static non-interactive scene element |
| `TerminalSprite` | Interactive terminal UI element, renders a command prompt overlay |
| `ObserverPrompt` | Renders UI prompt for observer feedback |
| `SpriteType` | `enum` classifying sprite types |

#### Drawing Stack — ImagePile

`Drawing/ImagePile.java` implements a stack-based layered rendering approach. Individual `ImageStackEntry` objects are pushed onto the pile, each containing a `BufferedImage`, position offset, and `ImageType`. At render time, the pile is drawn bottom-up (painter's algorithm).

---

### 5.4 View & Rendering

**Package:** `View/`, `GameView/`, `theStart/theView/`

| Class | Description |
|-------|-------------|
| `FlatLandWindow` | Original Swing window (deprecated) |
| `FlatLandWindowV2` | Modernized Swing window |
| `TheStartCamera` | The active camera/renderer; calls `takePictureOfFlatLand()` each frame |
| `GameScreen` | Panel that handles paint operations |
| `Observable` | Base class for anything that can have observers |
| `Observer` | Observer interface: `update(Observable, Object)` |
| `FPSObserver` | Tracks frames per second |
| `TimeObserver` | Tracks elapsed wall-clock time |
| `GeneralFieldObserver` | Generic field monitoring observer |
| `UpdateCycle` | Manages the timing of game-loop update cycles |
| `WebcamUpdater` | `Runnable` that polls the webcam on its own thread |
| `ImageRepository` | Caches loaded `BufferedImage` assets |

The camera (`TheStartCamera`) is the central rendering object. It holds references to the world (`ViewableFlatLand`), the canvas, and the game entities. Each call to `takePictureOfFlatLand()` repaints the canvas by iterating entities and calling their sprite's `update()` method.

---

### 5.5 Input Handling

**Package:** `theStart/theView/TheControls/`

`KeyBoardHandler` implements `KeyListener`. It maps keyboard events to entity movement commands. The player's movement is bound via `camera.setKeyBindingsForPlayer(flatLandWindow)`. The specific key bindings are set during camera construction.

`userInput/` contains a separate input event system for handling in-game events beyond raw keyboard input.

---

### 5.6 Level Loading (XMLLEVELLOADER)

XML level files in `res/level0-3.xml` are parsed by the XMLLEVELLOADER package. Two wrapper types bridge the XML data into the entity system:

| Class | Description |
|-------|-------------|
| `FlatLanderWrper` | Wraps a generic XML-loaded entity into a FlatLander |
| `PlayerWrper` | Wraps the XML-loaded player entity; implements `Collidable` |

Both wrappers extend FlatLander and are the types the Physics system checks for via `instanceof` in the legacy `checkEntity` method.

**Level manifest:** `res/levelManifesto.txt` contains metadata about which levels exist and their ordering.

**Levels 0–12:** `Playable/LevelZero.java` through `Playable/LevelTwelve.java` define 13 distinct game level states.

---

## 6. PCB — Pocket/Cup/Box Language System

PCB is the project's custom programming language. It is the most architecturally complete subsystem in the codebase. PCB follows the classic interpreter pipeline: source text → tokens → grouped tokens → AST → resolved AST → execution.

The name "PCB" stands for Pocket/Cup/Box, referencing the language's primary container types. It is also a literal circuit board metaphor: the language is meant to be the "wiring" that connects game behaviors.

### 6.1 What PCB Is

PCB is a dynamically-typed, interpreted scripting language with:
- Six container types arranged in two mirrored hierarchies: `box`/`xob` (data), `cup`/`puc` (code execution), `pkt`/`tkp` (stack/process) — plus `knt`/`tnk` which act as container, orientation marker, and operator
- Functions declared with `fun`
- Control flow: `if`/`fi` (forward/reverse), loops via `run`/`nur`, conditional branching via `knt`/`tnk` knots
- Bidirectional execution: forward and backward strands run simultaneously on shared state; knots can flip global direction mid-execution
- Mathematical expressions including trig, logarithms, and factorial
- Access to the FlatLand game world via special statement types (`FLCreate`, `FLDestroy`, `FLMove`)
- An HTTP server interface (`PCBServer`) for browser-driven execution

**Container hierarchy (formal):**
```
Forward:   box  :=  box
           cup  :=  cup  n  box
           pkt  :=  pkt  n  cup  n  box

Backward:  xob  :=  xob  n  box
           puc  :=  puc  n  xob  n  box
           tkp  :=  tkp  n  puc  n  xob  n  pkt  n  cup  n  box

Operators: knt  ⊃  container  n  orientation  n  operator
           tnk  ⊃  container  n  orientation  n  operator
```
`:=` = "is a", `n` = "and a", `⊃` = "acts as". `tkp` spans both hierarchies — it is the convergence point of bidirectional execution.

**Directional semantics (working model):** Direction is not text order — it is evaluation flow over a structure. A PCB program defines an execution graph. The forward strand enters at the root and follows edges in declaration order; the backward strand enters at the leaves and follows in reverse. Both strands share the same environment. A knot is a direction-switch node: `KnotRunner` uses an oscillation model — at each loop boundary the direction alternates while the condition holds, so both forward and backward statements fire on alternating passes through the body. The direction at knot exit persists as global state. Lexical reversal (backward-spelled keywords, mirrored dot-chain order) is the surface projection of this model — a notational consequence of traversal inversion, not its cause.

**Dot-chain as execution graph construction:** The `.` operator encodes two things simultaneously: data flow (what value passes between nodes) and execution order (which node evaluates first). `print.("hello")` and `("hello").tnirp` are the same graph node traversed from opposite ends. Multi-step chains build a linear sub-graph: `save.("/path").(value)` constructs three nodes — evaluate `value`, evaluate `"/path"`, execute `save` — with data flowing through them in declaration order (forward) or reverse order (backward). The graph structure is invariant; only the traversal direction differs.

### 6.2 Token System

**Package:** `Box/Token/`

| Class | Description |
|-------|-------------|
| `Token` | A single lexeme with type, literal value, line number, and column number |
| `TokenType` | The primary `enum` of all token types (~150+ entries; 54 new tokens added in the latest batch) |
| `TokenTypeEnum` | Marker interface implemented by both `TokenType` and `TTDynamic` |
| `TTDynamic` | Dynamic token type for user-defined identifiers and class names |
| `DynamicEnumTokenType` | Supports runtime-generated token categories |

`Token` carries:
```java
TokenTypeEnum type;
String lexeme;       // Raw text from source
Object literal;      // Parsed literal value (Double, String, etc.)
int line;
int column;
```

### 6.3 Scanner (Lexical Analysis)

**File:** `Box/Scanner/Scanner.java`

The Scanner performs a single-pass lexical analysis converting raw source text into a `List<Token>`. It is called `scanTokensFirstPass()` — the "first pass" name foreshadows the Grouper's second pass.

#### Keyword Map

The scanner uses a static `HashMap<String, TokenType>` initialized in the constructor. The full keyword set includes:

**Container types:**
- `box` → `BOX`, `cup` → `CUP`, `pocket` → `POCKET`

**Control flow (forward and reverse pairs):**
- `run` / `nur` (loop forward / loop backward)
- `if` / `fi` (if forward / if reverse — note: `fi` is if spelled backward)
- `return` / `nruter`
- `print` / `tnirp`
- `true` / `eurt`, `false` / `eslaf`

**Math operations:**
- `sin`/`nis`, `cos`/`soc`, `tan`/`nat`
- `sinh`/`hnis`, `cosh`/`hsoc`, `tanh`/`hnat`
- `log`/`gol`, `ln`/`nl`, `exp`/`pxe`
- `yroot`/`toory` (nth root)
- `abs`/`sba`, `sqrt`/`trqs`, `floor`/`roolf`, `ceil`/`liec`, `round`/`dnuor`, `sign`/`ngis`
- `asin`/`nisa`, `acos`/`soca`, `atan`/`nata`
- `asinh`/`hnisa`, `acosh`/`hsoca`, `atanh`/`hnata`
- `fresnelc`/`clenserf` (Fresnel C integral)

**Bitwise / min / max operations:**
- `bnot`/`tonb` (bitwise NOT, monadic)
- `band`/`dnab`, `bor`/`rob`, `bxor`/`roxb` (bitwise AND / OR / XOR, binary)
- `bleft`/`tfelb`, `bright`/`thgirb` (bit-shift left / right, binary)
- `min`/`nim`, `max`/`xam` (scalar minimum / maximum, binary)

**Assertion:**
- `assert`/`tressa`

**Vector / matrix operations:**
- `norm`/`mron`, `unit`/`tinu` (vector norm / unit vector)
- `trans`/`snart` (matrix transpose)
- `vdet`/`tedv` (matrix determinant)
- `vinv`/`vniv` (matrix inverse)
- `trace`/`ecart` (matrix trace)
- `vdot`/`todv` (dot product / matrix multiply), `cross`/`ssorc` (cross product)
- `vadd`/`ddav`, `vsub`/`busv` (element-wise add / subtract)
- `vscale`/`elacsv` (scalar multiply of vector or matrix)

**Container operations:**
- `add`/`dda`, `remove`/`evomer`, `clear`/`raelc`
- `size`/`ezis`, `empty`/`ytpme`
- `push`/`hsup`, `pop` (no reverse listed)
- `setat`/`tates`, `getat`/`tateg`
- `sub`/`bus`
- `alive`/`evila` — pocket liveness query

**Special syntax:**
- `#HATTAG` / `#GATTAH` — tag markers (case-insensitive via exhaustive variant list)
- `contains` / `sniatnoc`
- `fun` / `nuf` (function declaration forward/reverse)
- `move` / `evom`, `save` / `evas`, `read` / `daer`
- `into` / `otni`, `rename` / `emaner`, `to` / `ot`
- `open` / `nepo`, `and` / `dna`, `or` / `ro`

**Design note on `#HATTAG`:** The scanner explicitly maps every capitalisation variant of `#HATTAG` individually (16+ entries). This is not a case-insensitive comparison — it is a brute-force enumeration of all combinations of upper/lower case for the 6 letters H-A-T-T-A-G. This is an unusual but effective approach.

#### Token Emission

The scanner tracks `line`, `column`, `start`, and `current` positions. It handles:
- String literals (`"..."`) with escape sequences
- Character literals (`'.'`)
- Integer and double numeric literals
- Binary numbers (prefix `0b`)
- Single and multi-character operators (`==`, `!=`, `+=`, `++`, `--`, etc.)
- Single-line comments (started by `//` or `;`)
- Whitespace and newlines

---

### 6.4 Grouper (Token Grouping — Second Pass)

**File:** `Box/Grouper/Grouper.java`

**Brackets are structural carriers, not operators.** `(`, `{`, `[` (and their close counterparts) carry no execution semantics on their own. They delimit structure. The Grouper's bracket-pairing pass assigns each pair a unique `BigInteger` identifier so containers and scopes are addressable at runtime — but the execution meaning belongs to the keywords and operators that appear alongside the brackets, not to the brackets themselves. This distinction is intentional: bracket type (`PAREN`/`BRACE`/`SQUARE`) is used as a structural discriminator by `KnotRunner` and `Flow`, but the brackets do not execute anything.

The Grouper performs a structural second pass over the flat token list. It is not just bracket matching — it does four distinct things:

1. **Hashtag and bang-tag attachment** (`addHashTagsToIdents`, `addBangTagsToIdents`): The `#` and `!` sigils adjacent to identifiers are fused into the identifier token's lexeme (e.g., `name#` or `!name`). This supports template and reverse-template naming patterns used by `Expr.Template`.

2. **Bracket pairing** (`matchIdentifiersToOpenClosedParenBraceSquare`): Each open bracket token (`(`, `{`, `[`) is matched to its corresponding close bracket and assigned a unique `BigInteger` identifier. This identifier is stored on both the open and close tokens via `identifierToken` and `reifitnediToken` fields, enabling O(1) pair lookup during parsing and execution.

3. **Container renaming** (`renameNonBoxContainers`, `renameBoxes`): For named containers (e.g., `name(...)eman`), the Grouper ensures the open token carries the forward name as `identifierToken` and the close token carries the reversed name as `reifitnediToken`. If only one side has a name, the other is synthesized by reversing. This cross-referencing is what makes the bidirectional naming scheme structurally enforced at the token level. `KnotAnalyzer` reads the raw lexeme from each bracket token and normalizes it (strips the bracket character) to produce the identifier used for routing-target resolution.

4. **EOF injection**: Appends an `EOF` token to the end of the token stream.

`ContainerIndexes.java` stores start/end index pairs for matched brackets, used by `Util.findContainers()` to locate all container boundaries before renaming.

---

### 6.5 Parser & AST

**Package:** `Parser/` (shared by Box)

The parser (`ParserTest.java`) takes the grouped token list and builds an Abstract Syntax Tree. The AST is composed of three node hierarchies:

#### Declaration Hierarchy

```
Declaration (base)
├── StmtDecl    — wraps a statement as a declaration
└── FunDecl     — wraps a function as a declaration
```

`Declaration` implements `Visitor<Object>` for the visitor pattern.

#### Expr Hierarchy (Expressions — ~50 types)

Expressions return values. The naming convention is that every expression has a forward form and a **reverse form** (spelled backwards):

| Forward | Reverse | Meaning |
|---------|---------|---------|
| `Binary` | `Binaryyranib` | Binary operation (a op b) |
| `Unary` | `Yranu` | Unary operation |
| `Additive` | `Addittidda` / `Evitidda` | Addition |
| `Assignment` | `Assignmenttnemgissa` / `Tnemngissa` | Variable assignment |
| `Call` | `Callllac` / `Llac` | Function call |
| `Get` | `Teg` | Property access |
| `Set` | `Tes` / `Setat` / `Setattates` | Property assignment |
| `Log` | `Loggol` / `Gol` | Logarithm |
| `Mono` | `Monoonom` / `Onom` | Monadic expression |
| `BoxOpen` | `BoxClosed` | Box container open/close |
| `CupOpen` | `CupClosed` | Cup container open/close |
| `PocketOpen` | `PocketClosed` | Pocket container open/close |
| `Knot` | `Tonk` | Bracket group (Knot forward, Tonk backward) |
| `Factorial` | `Lairotcaf` | Factorial (!n) |
| `Contains` | `Containssniatnoc` / `Sniatnoc` | Membership test |
| `Variable` | — | Variable reference |
| `Literal` | — | Literal value |
| `LiteralBool` | `LiteralLoob` | Boolean literal |
| `LiteralChar` | — | Character literal |
| `Bus` | `Sub` | Subtraction |
| `Swap` | — | Swap two variables |
| `Link` | — | Link expression |
| `Type` | `Epyt` | Type expression |
| `Template` | — | Template instantiation |
| `NoPaCoOOoCaPoN` | `ParCoOppOoCraP` | Non/Param Container Op |
| `NonParamContOp` | `PoTnocMarapNon` | Non-param container operation |
| `ParamContOp` | `PoTnocMarap` | Param container operation |

#### Stmt Hierarchy (Statements — ~30 types)

Statements do not return values (side effects only):

| Forward | Reverse | Meaning |
|---------|---------|---------|
| `Print` | (via `TNIRP` token) | Print to output |
| `If` | `Fi` / `Ifi` | Conditional execution |
| `Move` | `Moveevom` | Move variable |
| `Nur` | — | Reverse loop (`nur` keyword) |
| `Nruter` | — | Reverse return |
| `Rav` | — | Variable declaration (reverse?) |
| `Read` | `Readdaer` / `Daer` | Read input |
| `Save` | `Evas` | Save to file |
| `Rename` | `Renameemaner` / `Emaner` | Rename variable |
| `Expel` | — | Expel/eject |
| `Consume` | — | Consume a value |
| `Evom` | — | Reverse move |
| `FLCreate` | — | Create a FlatLander in game world |
| `FLDestroy` | — | Destroy a FlatLander |
| `FLMove` | — | Move a FlatLander |
| `FLECreate` | — | Create FlatLand environment object |
| `FLEDestroy` | — | Destroy FlatLand environment object |
| `FLsetValue` | — | Set a value on a FlatLander |
| `Assert` | — | Assertion (forward only; backward is no-op) |

The `FL*` statement types are the direct game-world integration points — PCB scripts can create and control FlatLander entities.

#### Parser Grammar Extensions (new ops)

The monadic (`findMono`/`findOnom`) production rule now includes the following `TokenType` values in addition to existing math keywords:
- `NORM`, `UNIT`, `TRANS`, `VDET`, `VINV`, `TRACE` (and backward counterparts `MRON`, `TINU`, `SNART`, `TEDV`, `VNIV`, `ECART`)
- `ABS`/`SBA`, `SQRT`/`TRQS`, `FLOOR`/`ROOLF`, `CEIL`/`LIEC`, `ROUND`/`DNUOR`, `SIGN`/`NGIS`
- `ASIN`/`NISA`, `ACOS`/`SOCA`, `ATAN`/`NATA`, `ASINH`/`HNISA`, `ACOSH`/`HSOCA`, `ATANH`/`HNATA`
- `FRESNELC`/`CLENSERF`
- `BNOT`/`TONB`

The binary expression (`visitBinaryExpr`/`visitYranibExpr`) production now includes:
- `VDOT`/`TODV`, `CROSS`/`SSORC`, `VADD`/`DDAV`, `VSUB`/`BUSV`, `VSCALE`/`ELACSV`
- `BAND`/`DNAB`, `BOR`/`ROB`, `BXOR`/`ROXB`, `BLEFT`/`TFELB`, `BRIGHT`/`THGIRB`
- `MIN`/`NIM`, `MAX`/`XAM`

#### Fun Hierarchy (Functions)

```
Fun
├── Function     — forward function definition
└── FunctionLink — function reference/alias
```

---

### 6.6 Resolver (Static Analysis)

**Package:** `resolver/`

`Resolver.java` performs a static analysis pass over the AST before interpretation. It resolves variable references to their lexical scope depths, enabling the interpreter to do direct environment lookups by depth rather than searching up the scope chain at runtime. This is the standard Lox-style resolver pattern (from Robert Nystrom's *Crafting Interpreters*).

---

### 6.7 Interpreter (Execution Engine)

**File:** `Box/Interpreter/Interpreter.java`

The `Interpreter` class is the runtime execution engine. It implements the Visitor pattern on all AST node types. It has approximately 500+ lines of `visit*` methods, one per AST node type.

#### Key Interpreter State

```java
public Environment globals;     // Top-level global scope
private Environment environment;// Current scope (changes with blocks)
private Map<Expr, Integer> locals; // Resolver-provided scope depths
private boolean forward = true; // Global execution direction — readable and flippable at runtime
private ArrayList<FlatLander> facebook; // Entity registry reference
```

`Interpreter extends Thread`. The two-strand concurrent execution model (forward + backward strands sharing one environment) is the intended architecture. The `forward` flag being on the interpreter instance (not a local) means it is genuinely global — any code path that calls `setForward()` affects the entire ongoing execution.

#### Visit Method Pattern

Every node type has a corresponding `visit` method. For example:

```java
public Object visitPrintStmt(Stmt.Print stmt) {
    Object value = evaluate(stmt.expression);
    System.out.println(stringify(value));
    return null;
}
```

For bidirectional execution, reverse-named AST nodes (e.g., `Stmt.Moveevom`) have their own `visit` methods that execute the reverse semantic.

#### Proactive Pocket Lifetime Enforcement

After every statement in the main loop, block execution, and cup execution, the interpreter calls `probeLifetimes()`. This walks the entire environment chain and calls `probeLifetime()` on every live `PocketInstance` and `TkpInstance`. DEPENDENT and CONDITIONAL lifetimes are enforced here — a pocket begins dying within one statement of its condition becoming false or its dependency dying. TRAVERSAL lifetimes are not affected by this probe (they only decrement on explicit container operations via `onTraversal()`).

Pockets in stage 1 (stripping) are advanced to stage 2 (destroyed) by `probeLifetimes()` on the following statement.

#### Pocket Death — Two-Stage State Machine

Pocket death is not instantaneous. `PocketInstance` transitions through two stages:

| Stage | Field state | Behavior |
|-------|-------------|----------|
| 1 — stripping | `stripping = true` | Flows cleared, tick thread exits on next `isAlive()` check, body readable. All mutations (`add`, `remove`, `clear`, `pop`, `push`, `setat`) blocked. Read ops (`getat`, `size`, `empty`, `sub`, `contains`) still work. |
| 2 — dead | `destroyed = true` | Body cleared. All ops silently return null/false/0. |

**`beginDeath()`** triggers stage 1. It is called by:
- `visitAssignmentExpr` / `visitAssignmenttnemgissaExpr` when null is assigned to a pocket variable
- `probeLifetime()` when a DEPENDENT or CONDITIONAL lifetime condition fails

**`destroy()`** triggers stage 2. It is called by:
- `probeLifetimes()` when a pocket is already in stage 1
- `onTraversal()` inside any mutation op when `stripping == true`
- TRAVERSAL lifetime expiry via `onTraversal()`

**Cascading death:** `probeLifetime()` checks `!dep.isAlive()` (true for both stage 1 and stage 2). When A enters stage 1, dependent pockets detect it on the next `probeLifetimes()` call and call `beginDeath()` on themselves. Death propagates through the entire dependency tree. There is no orphaning.

**Nested pocket cascading death:** `PocketInstance.destroy()` (stage 2 transition) now walks the entire body list before clearing it. For every item in the body that is itself a `PocketInstance`, it calls `beginDeath()` on that nested instance before proceeding. This ensures nested pockets — ones stored as values inside a parent pocket's body rather than in separate environment variables — also enter stage 1 and then complete the two-stage shutdown sequence. Without this walk, nested pockets would be silently dropped (garbage collected) with no lifecycle callbacks firing.

#### KnotRunner — Direction-Aware Control Flow

`KnotRunner` (`Box/Interpreter/KnotRunner.java`) is the execution engine for `knt`/`tnk` bodies. It is instantiated and run inline by `visitKnotExpr` and `visitTonkExpr`, and also by `pkt`/`tkp` tick loops via `runWithRouting()`.

**Static analysis (constructor):** The constructor passes the body statement list to `KnotAnalyzer`, which performs a 6-pass static analysis and returns a `ControlGraph`. No manual bracket scanning or condition table building happens in `KnotRunner` — all structural knowledge comes from the graph. Inner classes for `ControlNode`, `Region`, `Condition`, and `MatchTable` are gone; the graph provides all equivalent information via a typed query API.

**What it does:**
- Pre-scans all statement slots for `Stmt.Var` / `Stmt.Rav` declarations via `initializeAllDeclarations()` and executes them before setup regions run, forcing `forward=true` for `Var` (forward initializer type `Stmt.Expression`) and `forward=false` for `Rav` (backward initializer type `Stmt.Stmtnoisserpxe`). Direction is restored after. Each initialized slot is marked already-run to prevent double-execution. Control node slots (where `graph.getNodeAt(i) != null`) are skipped.
- Runs SETUP regions identified in `graph.getRegions()` before the main loop begins.
- Traverses the body forward or backward according to `interp.isForward()`.
- Uses an **oscillation model** at condition boundaries: at the false endpoint of a forward condition, if the condition is still true, flips direction to backward (`setForward(false)`) and traverses the body in reverse — this is when backward statements (`tnirp`, `rav`, etc.) fire. When the backward pass reaches the condition's inner bracket (`indexTrue`), if the condition is still true, flips back to forward (`setForward(true)`) and re-enters the body. Oscillation continues until the condition fails at either endpoint.
- When the condition fails, the knot exits. The `forward` flag is left at whatever direction was active at exit — it is NOT restored after the knot. The direction at exit is the effective new direction for all subsequent execution.

**Condition queries:** `stepForwardControl` and `stepBackwardControl` use the ControlGraph query API exclusively:
- `graph.hasForwardCondition(index)` / `graph.hasBackwardCondition(index)` — detects condition start
- `graph.forwardConditionTrueTarget(index)` / `graph.forwardConditionFalseTarget(index)` — jump targets
- `graph.forwardConditionStartForFalseExit(index)` / `graph.backwardConditionStartForTrueEntry(index)` — oscillation detection (reverse-lookup methods added to ControlGraph)
- Condition range evaluation (`checkConditionRange`) still runs at runtime against the live statement list

**knt vs tnk:** `visitKnotExpr` fires only when `forward == true`; `visitTonkExpr` fires only when `forward == false` — matching the `if`/`fi` direction-gate convention. `runTonk()` delegates to `runKnot()`; since direction is already backward when `tnk` executes, `runKnot()`'s traversal is correct.

**Pocket transport routing:** Inside a `pkt` or `tkp` tick loop, when a flow hits a `KnotInstance` or `TonkInstance`, the tick method creates a `KnotRunner` and calls `runWithRouting(boolean forKnot)`. `runWithRouting` calls `resolveRouteTarget()` (which uses the first CONDITION region from the ControlGraph) to determine the destination container name, runs the knot/tonk to produce data results, looks up the destination in `interp.environment`, and injects the results. Routing is fully self-contained in `KnotRunner` — the `pkt`/`tkp` tick loop is not involved in routing decisions.

The full spec-level routing table (all container types):

```
knt routes (same-side / promotion):
  box → box, box → cup
  cup → cup, cup → pkt
  pkt → pkt
  xob → xob, xob → puc
  puc → puc, puc → pkt
  tkp → tkp

tnk routes (cross-side / reversal):
  box <-> xob
  cup <-> puc
  pkt <-> tkp
  pkt → box        (explicit reduction route)
```

Routing is triggered inside `pkt` and `tkp` tick loops (they have the flow engine). All container types are valid destinations — they are arrays and receive data via body append. Runtime validation: `pkt+knt` → `PocketInstance|BoxInstance|CupInstance`; `pkt+tnk` → `TkpInstance|BoxInstance|CupInstance`; `tkp+knt` → `TkpInstance|BoxInstance|CupInstance`; `tkp+tnk` → `PocketInstance|BoxInstance|CupInstance`. `BoxInstance` covers `box`; `XobInstance` (extends `BoxInstance`) covers `xob` — check `instanceof XobInstance` first. `CupInstance` covers `cup`; `PucInstance` (extends `CupInstance`) covers `puc` — check `instanceof PucInstance` first. Routing model flagged unstable — condition evaluation order, knot execution side-effects, and multi-condition bodies not yet fully validated.

#### findMono Return-Type Widening

`findMono` (and its reverse mirror `findOnom`) formerly returned `Double`. The return type is now `Object` to accommodate `BoxInstance` results. The following monadic ops return a `BoxInstance` rather than a scalar:
- `unit` / `tinu` — unit vector (same structure as input vector)
- `trans` / `snart` — transposed matrix
- `vinv` / `vniv` — matrix inverse
- Vector-valued results from `cross`, `vadd`, `vsub`, `vscale`, `vdot` (when operating on matrices)

Callers that previously assumed a `Double` return now receive an `Object` and check the type before use. Scalar ops still return `Double`.

#### isVecBinaryOp — Guard for Multi-Item BoxInstances

`isVecBinaryOp(TokenType t)` returns `true` for the tokens `VDOT`, `TODV`, `CROSS`, `SSORC`, `VADD`, `DDAV`, `VSUB`, `BUSV`, `VSCALE`, `ELACSV`. Before a binary operation calls `parseBinData()` to unwrap its operands, it checks this predicate. For vector/matrix ops the check is skipped — `parseBinData()` would throw on multi-item `BoxInstance`s, so `VecMatHelper` receives the raw `BoxInstance` directly.

#### VecMatHelper — Vector and Matrix Operations

`VecMatHelper.java` (package `Box.Interpreter`) is a stateless utility class containing all vector and matrix computation. It is called from `findMono`/`findOnom` and `visitBinaryExpr`/`visitYranibExpr`.

**Type model:**
- A *vector* is a `BoxInstance` whose body contains only numeric (`Double`/`Integer`) elements.
- A *matrix* is a `BoxInstance` whose body contains `BoxInstance` rows, each of which is itself a vector.
- `is1D(BoxInstance)` — returns true if body is all numeric.
- `is2D(BoxInstance)` — returns true if body is all `BoxInstance` rows with uniform column count.

**Conversion helpers:**
- `toVec(BoxInstance)` → `double[]`
- `toMatrix(BoxInstance)` → `double[][]`
- `fromVec(double[], Interpreter)` → `BoxInstance`
- `fromMatrix(double[][], Interpreter)` → `BoxInstance` of `BoxInstance` rows

**Monadic ops (dispatch via `dispatchVecMono`):**

| Token | Method | Algorithm |
|-------|--------|-----------|
| `NORM`/`MRON` | `norm(double[])` | Euclidean norm |
| `UNIT`/`TINU` | `unit(double[])` | Normalize to unit vector |
| `TRANS`/`SNART` | `transpose(double[][])` | Standard matrix transpose |
| `VDET`/`TEDV` | `det(double[][])` | Cofactor expansion (recursive) |
| `VINV`/`VNIV` | `inverse(double[][])` | Gauss-Jordan elimination |
| `TRACE`/`ECART` | `trace(double[][])` | Sum of main-diagonal elements |

**Binary ops (dispatch via `dispatchVecBinary`):**

| Token | Method | Behavior |
|-------|--------|----------|
| `VDOT`/`TODV` | `dot(double[], double[])` or `matMul(double[][], double[][])` | Dot product for vectors; matrix multiply for 2-D inputs |
| `CROSS`/`SSORC` | `cross(double[], double[])` | 3-element cross product; throws `RuntimeError` if not length 3 |
| `VADD`/`DDAV` | `elemAdd` | Element-wise addition (vectors or matrices) |
| `VSUB`/`BUSV` | `elemSub` | Element-wise subtraction |
| `VSCALE`/`ELACSV` | `scalarMul` | Multiplies every element by a scalar; operand order: `(vector, scalar)` |

All `VecMatHelper` methods throw `RuntimeError` with a descriptive message on dimension mismatches.

#### visitAssertStmt

`Stmt.Assert` holds a single expression. `visitAssertStmt` evaluates the expression; if the result is falsy (null, `false`, or numeric zero) and execution is **forward**, a `RuntimeError` is thrown with the message `"Assertion failed"`. When `forward == false` the method is a no-op — assertions are direction-gated and do not fire on the backward strand.

#### visitConsumeStmt

`Stmt.Consume` (`<<<` operator) reads file content into a container variable. Execution steps:
1. Evaluate the left-hand side to obtain the target container (any of box, cup, pkt, knt, tnk, tkp).
2. Read the file line by line.
3. Parse each line with `ContainerPersistence.parseValue()`, which produces a typed PCB value (numeric, string, boolean, or nested container) from the text representation.
4. Dispatch on the container type and call the appropriate insertion method (`add`, `push`, or body-append depending on whether the target is a `BoxInstance`, `CupInstance`, `PocketInstance`, `KnotInstance`, `TonkInstance`, or `TkpInstance`).

`ContainerPersistence.parseValue()` is the same parser used by the six-container batch file load at startup, so all value types that survive persistence round-trip correctly through `<<<`.

#### FlatLand Integration in Interpreter

The interpreter handles the `FL*` statement types to manipulate the game world:
- `visitFLCreateStmt` — instantiates a new FlatLander and registers it with FlatLandFacebook
- `visitFLDestroyStmt` — removes a FlatLander from FlatLandFacebook
- `visitFLMoveStmt` — sets position/velocity on a named FlatLander
- `visitFLSetValueStmt` — sets arbitrary field values on a FlatLander

This is how PCB scripts drive the game engine.

---

### 6.8 Environment (Variable Scoping)

**File:** `Box/Interpreter/Environment.java`

The `Environment` class implements lexical scoping via a linked list of scope frames.

```java
public class Environment {
    public Environment enclosing;                       // Parent scope
    private final Map<String, Object> values;           // Variable values
    private final Map<String, TypesOfObject> types;     // Type tracking
}
```

**Key operations:**

| Method | Description |
|--------|-------------|
| `define(name, type, value)` | Declare a new variable in current scope |
| `get(token, fromCall)` | Look up a variable, walking up scope chain; if `fromCall=true`, looks for `name + "Class_Definition"` suffix |
| `assign(name, exprValue, value, interpreter)` | Assign to existing variable with type checking |
| `getAt(distance, lexeme)` | Direct scope-depth access (from Resolver) |
| `ancestor(distance)` | Walk `distance` steps up the scope chain |
| `assignAt(distance, name, value)` | Assign at a specific scope depth |
| `allLocalValues()` | Returns the local scope's value collection; used by `probeLifetimes()` to scan for live pockets |

**Type enforcement:** The `assign` method checks `RunTimeTypes` compatibility. If the variable was declared as a specific type and you try to assign the wrong type, it calls `Box.error(...)` with a type mismatch message. The `Any` type bypasses this check.

---

### 6.9 Runtime Types & Instances

**Package:** `Box/Interpreter/`

#### RunTimeTypes

`RunTimeTypes` is an enum of all runtime type categories:

```
Any, box, cup, pkt, knt, tnk, int, dbl, bool, str, char, bin, null, fun, cls
```

- `knt` = Knot (bracket group forward)
- `tnk` = Tonk (bracket group backward)
- `bin` = binary number

`RunTimeTypes.getTypeBasedOfToken(token)` maps from `TokenType` to `RunTimeTypes`. `getObjectType(exprValue, value, interpreter)` infers the runtime type of an evaluated value.

#### Interface Type Hierarchy

Every PCB container implements `IBox`. The marker interfaces live in `Box/Interpreter/` and form the "everything is a box" invariant:

```
IBox
 ├── ICup  extends IBox
 │    └── IPkt  extends ICup
 │         ├── ITkp  extends IPkt
 │         ├── IKnt  extends IPkt
 │         └── ITnk  extends IPkt
 └── IXob  extends IBox
      └── IPuc  extends IXob, ICup
```

| Class | Implements | Also extends |
|-------|------------|--------------|
| `BoxInstance` | `IBox` | — |
| `CupInstance` | `ICup` | — |
| `PocketInstance` | `IPkt` | — |
| `KnotInstance` | `IKnt` | — |
| `TonkInstance` | `ITnk` | — |
| `TkpInstance` | `ITkp` | — |
| `XobInstance` | `IXob` | `BoxInstance` |
| `PucInstance` | `IPuc` | `CupInstance` |

Any code that needs to handle "any container" should check `instanceof IBox`.

#### Instance Types

| Class | Container | Role |
|-------|-----------|------|
| `Instance` | — | Base class for all runtime object instances. Has `getBody()` returning `null` (overridden by every container to return its mutable body list) |
| `BoxInstance` | `box` | Transparent data container — all ops permitted (getat, setat, sub, contains, remove, push, pop, add, size, empty, clear) |
| `XobInstance` | `xob` | Opaque data container (extends `BoxInstance`) — push/pop/add/size/empty/clear permitted; getat/setat/sub/contains/remove throw `RuntimeError("xob: 'op' not permitted — xob is opaque")`. Routing note: check `instanceof XobInstance` before `instanceof BoxInstance` since XobInstance IS-A BoxInstance |
| `CupInstance` | `cup` | Code execution container; evaluates body at construction; stores `originalBody` so `execute()` can re-run them when bootstrapped by a flow inside a pkt/tkp |
| `PucInstance` | `puc` | Execution-inversion cup (extends `CupInstance`) — identical container interface to CupInstance; `execute()` toggles `interpreter.invertedMode` (try/finally) before calling `super.execute()`. While `invertedMode` is active, visitor methods flip: PLUS↔MINUS, TIMES↔FORWARDSLASH, `>`↔`<`, `>=`↔`<=`, AND↔OR, DNA↔RO, QMARK drops negation; push/add→pop, remove/getat→pop, pop→no-op, setat→add |
| `PocketInstance` | `pkt` | Ecosystem container (forward); supports lifetime; has independent Flow tick engine on its own thread |
| `TkpInstance` | `tkp` | Ecosystem container (reverse); independent tick-based lifecycle; seam-crosses to PocketInstance at heat death OR natural lifetime expiry; user-kill destroys without transformation; env updated asynchronously |
| `KnotInstance` | `knt` | Stored knot body (bracket group); executed via `KnotRunner` |
| `TonkInstance` | `tnk` | Stored tonk body (reverse bracket group); currently uses same `KnotRunner` as knt |

#### Primitive Boxing (`Boxer`)

`Boxer.java` enforces the "everything is a box" invariant. Every value at a storage boundary is wrapped in a `BoxInstance`.

**Storage boundaries** (where `Boxer.box()` is called):
- `evaluateBody()` in all containers — result of `interpreter.execute()` on any `Expr`
- `visitVarStmt` / `visitRavStmt` — raw primitives in the else-branch before `environment.define()`
- `visitAssignmentExpr` / `visitAssignmenttnemgissaExpr` — both forward and backward assignment
- `KnotRunner.injectIntoContainer()` — before `addAll()` into any routing target
- `PocketInstance.tick()` / `TkpInstance.tick()` — bootstrap result before `body.add(fnIndex+1, ...)`

**`Boxer` API:**

| Method | Behavior |
|--------|----------|
| `box(Double/Integer/String/Boolean, interp)` | Returns `BoxInstance([value])` |
| `box(null, interp)` | Returns `BoxInstance([null])` — body size 1, NOT empty |
| `box(Instance, interp)` | Returns the Instance unchanged — containers never re-wrapped |
| `box(other, interp)` | Returns `null`; routes error to `NON`/`LIMBO` via `interp.addToErrorSink()` |
| `unbox(BoxInstance([v]))` | Returns `v` (may be null) |
| `unbox(empty BoxInstance)` | Returns `null` |
| `unbox(multi-item BoxInstance)` | Throws `RuntimeError` — explicit pop required |
| `unbox(raw primitive)` | No-op, returns unchanged |
| `isBoxable(v)` | `true` for `Double`, `Integer`, `String`, `Boolean`, `null`, `Instance`; `false` otherwise |

**Auto-unboxing** in binary operators: `parseBinData()` unwraps `BoxInstance` on either operand before arithmetic or comparison. The operator result is raw; re-boxing happens at the next storage boundary.

**`Interpreter.invertedMode`:** `private boolean invertedMode = false` with `isInverted()` / `setInverted()`. Toggled (not set) by `PucInstance.execute()` — double-puc nesting restores normal execution. Checked in `visitBinaryExpr`, `visitUnaryExpr`, `visitAdditiveExpr`, `visitParamContOpExpr`, `visitNonParamContOpExpr`, and `visitSetatExpr`.

**Directionality collapse on divide:** `FORWARDSLASH` (`a / b` → `div(left, right)`) inverts to `times(left, right)`; `BACKSLASH` (`a \ b` → `div(right, left)`) inverts to `times(right, left)`. Since multiplication is commutative both produce `a * b` — the forward/backward distinction of the two divide tokens is lost after inversion. This is intentional: there is no directional multiply token to invert into, and the computed value is correct regardless.

**`BoxClass.call()` routing:** Checks `isXob` flag on `Expr.Box` → constructs `XobInstance` vs `BoxInstance`; checks `isPuc` flag on `Expr.Cup` → constructs `PucInstance` vs `CupInstance`. Both flags are `false` by default and set by the parser when `xob [...]` or `puc {...}` syntax is detected.

#### Pocket Lifetime System

Both `PocketInstance` and `TkpInstance` support an optional **lifetime** (`Expr.Lifetime`) with four kinds:

| Kind | Syntax | Semantics |
|------|--------|-----------|
| `INDEFINITE` | `.*` | Never expires (default) |
| `TRAVERSAL` | `.N` | Expires after N container operations |
| `DEPENDENT` | `.^(name)` | Expires when the named pocket is destroyed |
| `CONDITIONAL` | `.^{expr}` | Expires when `expr` evaluates to false/null |

Key implementation details:
- `onTraversal()` — called at the start of every mutating container op; decrements TRAVERSAL counter, enforces all kinds; on expiry calls `seamCrossAndUpdate()` (not `beginDeath()`)
- `probeLifetime()` — called by the interpreter after every statement; enforces only DEPENDENT and CONDITIONAL (no traversal decrement); on expiry calls `seamCrossAndUpdate()`
- `seamCrossAndUpdate()` — performs natural-death seam crossing: creates the opposite container type, transfers body + lifetime budget, assigns new instance into env, starts tick if new container has flows
- `beginDeath()` / `destroy()` — used only on **user-kill** (`= null`): stage 1 strips flows and blocks mutations; stage 2 clears body entirely; no seam crossing
- `isAlive()` — `!destroyed && !stripping`; public predicate exposed to PCB via `x.alive.()` / `().evila.x`
- `isStripping()` — `stripping && !destroyed`; used by `probeLifetimes()` to advance to stage 2
- Lifetime transfers through seam crossing: TRAVERSAL carries `remainingTraversals` (not original count); other kinds carry unchanged; INDEFINITE carries through
- Seam crossing direction: `pkt` natural expiry → `tkp`; `tkp` natural expiry OR heat death → `pkt`; user-kill on either → destroy, no transformation; INDEFINITE never crosses

#### Flow Independence

Flow processing is **completely independent of program execution**. Neither `TkpInstance` nor `PocketInstance` blocks the main interpreter thread. Both run their tick loops on daemon threads and interact with the environment asynchronously.

**Flow connectors** (`(.` forward, `.)` backward) are created as `Flow` objects by `visitFlowFwdExpr`/`visitFlowBwdExpr` during normal expression evaluation. They are placed into container bodies as values. The container's independent tick thread then picks them up and drives processing.

#### TkpInstance Lifecycle

```
tkp declared
    │
    ├── env.define(name, tkp)       ← main thread continues immediately
    │
    └── tkp.startIndependent(env, nameToken)
            │
            └── daemon thread: "tkp-tick-<name>"
                    │
                    ▼
                tick loop (runToCompletion — up to 100k ticks)
                    │
                    │  ── FLOW — per tick, each active flow scans body ────────────
                    ├── Flow connector object → scavenged (chain absorbed, removed)
                    ├── String "." (single period) → flip active flow direction, consumed
                    ├── Bare bracket string "(" "{" ")" "}" → synthesize period →
                    │       new independent Flow (open=fwd, close=bwd), consumed
                    ├── PocketOpen/CupOpen/PocketClosed/CupClosed expr → synthesize period →
                    │       new independent Flow (open=fwd, close=bwd), consumed; label discarded
                    ├── String with embedded connectors "(." ".)" etc. → chain absorbed, string as cargo
                    ├── CupInstance/PucInstance (cup/puc) → bootstrapped BY Flow → re-executes originalBody (puc toggles invertedMode), consumed
                    ├── KnotInstance / TonkInstance → bootstrapped BY Flow →
                    │       KnotRunner.runWithRouting() handles routing independently
                    │       (routing = KnotRunner's job; uses interp.environment; not Flow's concern)
                    ├── Nested pkt / tkp → ignored by Flow (Flow does not enter them)
                    │
                    └── Null-scan penalty: ghost flows drain to 0
                    │
                    ▼
         ┌──────────┴──────────────────────────────┐
         │                                         │
    heat death                           natural lifetime expiry
    (isHeatDeath() == true)         (TRAVERSAL/DEPENDENT/CONDITIONAL)
         │                                         │
         └──────────┬──────────────────────────────┘
                    │
                    ▼
            seamCrossAndUpdate()
                seamCross() → PocketInstance
                    ├── Body transferred (flows stripped — none remain at heat death)
                    ├── Lifetime budget transferred
                    └── tkp marked seamCrossed
                    │
                    ▼
            env.assign(nameToken, pkt)   ← env updated asynchronously
                    │
                    └── if pkt has flows → pkt.startIndependent(env, nameToken)

    [user-kill path: = null → beginDeath() → destroy() — no seam crossing]
```

#### PocketInstance Lifecycle (with flows)

```
pkt declared with flows in body
    │
    └── pkt.startIndependent(env, nameToken)
            │
            └── daemon thread: "pkt-tick-<name>"
                    │
                    ▼
                tick loop (up to 100k ticks)
                    │
                    │  ── FLOW — per tick, each active flow scans body ────────────
                    ├── Flow connector object → scavenged (chain absorbed, removed)
                    ├── String "." (single period) → flip active flow direction, consumed
                    ├── Bare bracket string "(" "{" ")" "}" → synthesize period →
                    │       new independent Flow (open=fwd, close=bwd), consumed
                    ├── PocketOpen/CupOpen/PocketClosed/CupClosed expr → synthesize period →
                    │       new independent Flow (open=fwd, close=bwd), consumed; label discarded
                    ├── String with embedded connectors "(." ".)" etc. → chain absorbed, string as cargo
                    ├── CupInstance/PucInstance (cup/puc) → bootstrapped BY Flow → re-executes originalBody (puc toggles invertedMode), consumed
                    ├── KnotInstance / TonkInstance → bootstrapped BY Flow →
                    │       KnotRunner.runWithRouting() handles routing independently
                    │       (routing = KnotRunner's job; uses interp.environment; not Flow's concern)
                    ├── Nested pkt / tkp → ignored by Flow (Flow does not enter them)
                    │
                    └── Null-scan penalty: ghost flows drain to 0
                    │
                    ▼
         ┌──────────┴──────────────────────────────┐
         │                                         │
    heat death                           natural lifetime expiry
    (no seam crossing)              (TRAVERSAL/DEPENDENT/CONDITIONAL)
    thread exits, pkt remains               │
                                            ▼
                                    seamCrossAndUpdate()
                                        new TkpInstance
                                            ├── Body transferred
                                            ├── Lifetime budget transferred
                                            └── pkt marked destroyed
                                            │
                                            ▼
                                    env.assign(nameToken, tkp)
                                            │
                                            └── tkp.startIndependent(env, nameToken)

    [user-kill path: = null → beginDeath() → destroy() — no seam crossing]
    [INDEFINITE: never expires naturally — only user-kill or heat death applies]
```

`startIndependent()` is idempotent — if a thread is already running on the instance, subsequent calls are no-ops.

#### BoxClass & BoxFunction

`BoxClass` represents a class definition (the PCB equivalent of a Java class). `BoxFunction` represents a callable function with its closure environment captured at definition time. `BoxCallable` is the interface implemented by both.

#### Returns & Snruter

`Returns.java` is a Java exception subclass used for non-local return from functions (the classic interpreter trick). `Snruter.java` is the reverse version — thrown when a `nruter` (reverse return) statement is executed.

#### WesMap

`WesMap.java` is a custom `HashMap`-like structure named after the developer. Its specific behavior differences from standard `HashMap` are not documented in comments but it is used in certain interpreter contexts.

#### Bin

`Bin.java` represents a binary number literal in the runtime.

#### UnknownVariable

`UnknownVariable.java` is a sentinel value returned when a variable cannot be resolved, used to distinguish "not found" from `null`.

---

### 6.10 PCB Math Sub-Language

**Package:** `Box/math/`

PCB includes a complete embedded math expression language with its own scanner, parser, interpreter, and derivative calculator.

```
Box/math/
├── BoxMath.java              — entry point for math sub-language
├── BoxMathTokenType.java     — token types for math expressions
├── Scanner/MathScanner.java  — tokenizes math expressions
├── Parser/MathParser.java    — parses into math AST
├── Syntax/Term.java          — math AST node
├── Token/MathToken.java      — math-specific token
└── Interpreter/
    ├── MathInterpreter.java  — evaluates math expressions
    └── MathDeriver.java      — computes symbolic derivatives
```

`MathDeriver.java` is particularly notable: it performs **symbolic differentiation** of mathematical expressions. This means PCB can compute the derivative of a function at runtime, not just evaluate it. This is used for the math operation keywords in the main language.

#### Scalar Operations

The following monadic scalar operations are dispatched through `findMono` (forward) and `findOnom` (backward). All accept one numeric argument and return a `Double`.

| Forward | Backward | Operation |
|---------|----------|-----------|
| `abs` | `sba` | Absolute value |
| `sqrt` | `trqs` | Square root |
| `floor` | `roolf` | Floor (round toward −∞) |
| `ceil` | `liec` | Ceiling (round toward +∞) |
| `round` | `dnuor` | Round to nearest integer |
| `sign` | `ngis` | Sign: −1, 0, or 1 |
| `asin` | `nisa` | Inverse sine (radians) |
| `acos` | `soca` | Inverse cosine (radians) |
| `atan` | `nata` | Inverse tangent (radians) |
| `asinh` | `hnisa` | Inverse hyperbolic sine |
| `acosh` | `hsoca` | Inverse hyperbolic cosine |
| `atanh` | `hnata` | Inverse hyperbolic tangent |
| `fresnelc` | `clenserf` | Fresnel C integral |

`bnot`/`tonb` is a **monadic bitwise NOT**: casts the argument to `int`, applies Java `~`, and returns the result as `Double`. Dispatched through the same monadic loop as the ops above.

**Fresnel C:** `fresnelc(x)` computes the Fresnel cosine integral C(x) = ∫₀ˣ cos(πt²/2) dt. The implementation in `MathInterpreter.C()` uses:
- **Simpson's rule** (adaptive composite, `n=1000` subintervals) for |x| ≤ 3
- **Asymptotic expansion** for |x| > 3: `C(x) ≈ 0.5 + f(x)·sin(πx²/2) − g(x)·cos(πx²/2)` where `f` and `g` are the standard auxiliary functions

**Binary scalar ops** dispatched through `visitBinaryExpr`:

| Forward | Backward | Operation |
|---------|----------|-----------|
| `min.` | `.nim` | Scalar minimum of two values, via `bitwiseMin()` |
| `max.` | `.xam` | Scalar maximum of two values, via `bitwiseMax()` |
| `band.` | `.dnab` | Bitwise AND |
| `bor.` | `.rob` | Bitwise OR |
| `bxor.` | `.roxb` | Bitwise XOR |
| `bleft.` | `.tfelb` | Bit-shift left |
| `bright.` | `.thgirb` | Bit-shift right |

All bitwise binary ops cast both operands to `int` before operating. When `invertedMode` is active (inside a `puc`), bitwise ops apply an additional inversion: AND↔OR, XOR remains. `bitwiseMin`/`bitwiseMax` are unaffected by `invertedMode`.

#### Vector and Matrix Operations

Vector and matrix operations are executed through `VecMatHelper` (package `Box.Interpreter`), a stateless utility class. They are available as keywords in the main PCB language and respect the forward/backward naming convention.

**Value model:**

| PCB value | Java representation |
|-----------|---------------------|
| Vector | `BoxInstance` with all-numeric body (e.g., `box v = [1, 2, 3]`) |
| Matrix | `BoxInstance` whose body is all `BoxInstance` rows of equal length |

`VecMatHelper.is1D(b)` and `is2D(b)` test which form a `BoxInstance` has. Operations that receive the wrong shape throw `RuntimeError`.

**Monadic vector/matrix ops** (called from `findMono`/`findOnom`):

| Forward | Backward | Argument | Returns | Notes |
|---------|----------|----------|---------|-------|
| `norm` | `mron` | vector | `Double` | Euclidean L2 norm |
| `unit` | `tinu` | vector | `BoxInstance` | Unit vector; same length as input |
| `trans` | `snart` | matrix | `BoxInstance` | Transposed matrix |
| `vdet` | `tedv` | square matrix | `Double` | Determinant via cofactor expansion |
| `vinv` | `vniv` | square matrix | `BoxInstance` | Inverse via Gauss-Jordan |
| `trace` | `ecart` | square matrix | `Double` | Sum of main diagonal |

**Binary vector/matrix ops** (called from `visitBinaryExpr`/`visitYranibExpr`):

| Forward | Backward | Arguments | Returns | Notes |
|---------|----------|-----------|---------|-------|
| `vdot` | `todv` | vec,vec or mat,mat | `Double` or `BoxInstance` | Dot product for 1-D; matrix multiply for 2-D |
| `cross` | `ssorc` | vec3, vec3 | `BoxInstance` | Cross product; operands must have length 3 |
| `vadd` | `ddav` | vec,vec or mat,mat | `BoxInstance` | Element-wise addition |
| `vsub` | `busv` | vec,vec or mat,mat | `BoxInstance` | Element-wise subtraction |
| `vscale` | `elacsv` | vec/mat, scalar | `BoxInstance` | Multiply every element by scalar |

For `vdot` on 2-D inputs, `matMul` is called instead of `dot`; the result is a `BoxInstance` matrix, not a scalar. `cross` throws `RuntimeError("cross product requires length-3 vectors")` if either operand is not length 3. Dimension mismatches in `vadd`, `vsub`, and `matMul` also throw `RuntimeError`.

**`isVecBinaryOp` guard:** Before any binary expression calls `parseBinData()` (which throws on multi-item `BoxInstance`s), `isVecBinaryOp(TokenType)` is checked. If it returns `true`, `parseBinData()` is skipped and the raw `BoxInstance` operands are passed directly to `VecMatHelper`.

---

### 6.11 Box Orchestrator Class

**File:** `Box/Box/Box.java`

`Box` extends `Thread` and serves as the public API for the PCB runtime.

**Threading model:** `Box` is designed to run on its own thread (via `Thread.run()`). When run, it calls `runPrompt()` which flushes the `ByteArrayOutputStream` and notifies the `Observer`. This is the push-notification model for the game terminal UI.

**Key methods:**

| Method | Description |
|--------|-------------|
| `runJson(String, boolean)` | Full pipeline: scan → group → parse → resolve → interpret |
| `run(String, boolean)` | Scan → group → parse → serialize AST to JSON (debug mode) |
| `runFile(String, boolean, boolean)` | Read from file system, then `run()` |
| `runPrompt()` | Flush captured output, notify observer |
| `addObserver(Observer)` | Register an observer for output events |
| `notify(String)` | Accept new PCB source, execute it, reset error state |
| `error(...)` | Report syntax errors with line/column info |
| `runtimeError(RuntimeError)` | Report runtime errors |
| `resetHadError()` | Reset the error flag (called by PCBServer between executions) |

**Output capture:** The `ByteArrayOutputStream baos` captures all `System.out` output during execution. The `Observer`/`PromptObserver` pattern allows this captured output to be pushed to the game UI without polling.

**Execution direction flags:** The commented-out `main` method shows the original CLI interface supported `fon`/`foff`/`bon`/`boff` command-line flags for "forward on/off" and "backward on/off". These flags are preserved in comments as documentation of intent.

---

### 6.12 SandBox (Restricted Runtime)

**File:** `Box/GameSpaceInterpreter/SandBox.java`

`SandBox` wraps the `Box` interpreter with:
- A `ByteArrayOutputStream` for output capture
- Seed variable injection (manifold, grammar, traversal, user seeds)
- Error recovery (errors don't crash the server)

It is used exclusively by `PCBServer` to provide an isolated execution environment for each HTTP request.

---

### 6.13 PCBServer (HTTP Interface)

**File:** `Box/GameSpaceInterpreter/PCBServer.java`

PCBServer exposes the PCB interpreter as a REST API, described in the source as a "Combined Seed Workbench" interface. It uses only Java 21 built-in `com.sun.net.httpserver` — no external HTTP framework.

#### Server Startup

```java
PCBServer.start();         // uses default port 7070
PCBServer.start(port);     // custom port
PCBServer.stop();          // graceful shutdown
```

Uses a `CachedThreadPool` executor — each request is handled on its own thread.

#### Endpoints

**`POST /seeds`**

Loads up to four "seed" JSON objects into the server's memory. These seeds are injected as variables into subsequent PCB executions.

Request body:
```json
{
  "manifold":  { "manifoldSeedId": "...", ... },
  "grammar":   { "grammarSeedId": "...", ... },
  "traversal": { "traversalSeedId": "...", ... },
  "user":      { "projectId": "...", ... }
}
```

Response:
```json
{
  "ok": true,
  "manifest": {
    "manifold":  { "type": "ManifoldSeedObject", "seedId": "...", "properties": [...] },
    "grammar":   { "type": "GrammarSeedObject", ... },
    "traversal": { "type": "TraversalSeedObject", ... },
    "user":      { "type": "UserDataObject", ... }
  }
}
```

**`POST /run`**

Executes PCB source code. Seeds are automatically injected as `box` variables in a preamble.

Request body:
```json
{
  "source":    "box x = 42\nprint(x)",
  "direction": "fwd"
}
```

`direction` can be `"fwd"` (forward execution, default) or `"bwd"` (backward execution).

Response:
```json
{
  "ok": true,
  "output": "42\n",
  "executionTime": 12,
  "direction": "fwd",
  "interfaceSeedId": "iface_abc123",
  "bindings": [...]
}
```

The `bindings` array is parsed from the source — any line matching `box|cup|pkt name = [elem, ...]` is extracted and described with container type, name, user anchor, seed refs, natural flag, and confidence score.

**`GET /status`**

Health check. Returns which seeds are currently loaded:
```json
{
  "ok": true,
  "runtime": "PCBServer",
  "seedsLoaded": 2,
  "manifold": true,
  "grammar": false,
  "traversal": true,
  "user": false
}
```

#### Seed Preamble Injection

Before executing user source, the server prepends a PCB preamble:
```
box manifoldId = "manifold_seed_id_value"
box grammarId = "grammar_seed_id_value"
...
```

This makes seed identity values available as named `box` variables in every PCB script without the user needing to declare them.

#### CORS

All endpoints include CORS headers (`Access-Control-Allow-Origin: *`) so the server can be called directly from a browser-based workbench without a proxy.

---

### 6.14 Bidirectional Execution

PCB executes in **both directions simultaneously** — not as a mode selection but as two concurrent strands operating on shared data. This is the central design principle of the language.

#### Direction Model

The `Interpreter` class extends `Thread`. The intended full model is two `Interpreter` instances sharing the same `Environment`, each traversing the statement list from opposite ends, writing and reading the same variables. Because state is shared, operations from one strand are immediately visible to the other; contention is first-come-first-serve.

The current PCBServer implementation initializes a single direction and runs one strand. The Thread architecture is in place for the full concurrent model.

#### Direction State

`forward` is a boolean field on the `Interpreter` instance. It controls:
- Which direction `interpret()` traverses the statement list (0→n vs n→0)
- Which direction `executeCupExpr()` traverses cup bodies
- Which branch of direction-sensitive `visit*` methods executes (e.g., `visitFiStmt` only fires when `!forward`)

#### Lexical Reversal

Every keyword has a reverse form — the word spelled backward. This is the surface projection of directional traversal:

```
forward:  print  →  reverse: tnirp
forward:  run    →  reverse: nur
forward:  if     →  reverse: fi
forward:  box    →  reverse: xob
forward:  cup    →  reverse: puc
forward:  pkt    →  reverse: tkp
forward:  knt    →  reverse: tnk
```

#### Knots as Direction Switches

Knots (`knt`/`tnk`) are the mechanism by which execution transitions between directions mid-run. `KnotRunner` uses an **oscillation model**: when a condition remains true at the forward endpoint of a loop, direction flips to backward and the body traverses in reverse; when the backward pass reaches the inner bracket and the condition still holds, direction flips back to forward. Both forward and backward statements fire on alternating passes. When the condition fails, the knot exits in whichever direction was active at that moment — that direction persists as global state into all subsequent execution. A program may exit a knot in either forward or backward direction depending on which pass the condition last failed on.

---

### 6.15 PCB Language Reference Summary

#### Variable Declaration

```pcb
box name = value          // ordered list / general variable
cup name = value          // cup container
{ pkt name = ( values } body )   // pocket (stack)
```

#### Container Operations

```pcb
add name value            // add to box/cup
remove name               // remove from box/cup
push name value           // push to pkt
pop name                  // pop from pkt
size name                 // get size
clear name                // empty container
setat name index value    // set at index
getat name index          // get at index
contains name value       // membership test
```

#### Control Flow

```pcb
if condition
  // body
fi                        // end if (forward)

run condition
  // body
// end run loop

fun myFunction(param)
  // body
  return value
```

#### Math

```pcb
sin(x)   cos(x)   tan(x)
sinh(x)  cosh(x)  tanh(x)
log(x)   ln(x)    exp(x)
yroot(n, x)                // nth root of x
n!                         // factorial

// New scalar ops
abs(x)   sqrt(x)  floor(x)  ceil(x)  round(x)  sign(x)
asin(x)  acos(x)  atan(x)
asinh(x) acosh(x) atanh(x)
fresnelc(x)                // Fresnel C integral

// Bitwise (monadic)
bnot(x)

// Bitwise (binary)
band.(a, b)   bor.(a, b)   bxor.(a, b)
bleft.(a, b)  bright.(a, b)

// Min / max (binary)
min.(a, b)    max.(a, b)
```

#### Assertions

```pcb
assert condition           // throws RuntimeError if falsy (forward only; backward is no-op)
```

#### Vector and Matrix Operations

```pcb
// Vectors are boxes with numeric bodies
box v = [1, 2, 3]
box w = [4, 5, 6]

norm(v)                    // Euclidean norm → Double
unit(v)                    // unit vector → BoxInstance
vdot.(v, w)                // dot product → Double
cross.(v, w)               // cross product → BoxInstance (length-3 vectors only)
vadd.(v, w)                // element-wise add → BoxInstance
vsub.(v, w)                // element-wise subtract → BoxInstance
vscale.(v, 2)              // scalar multiply → BoxInstance

// Matrices are boxes of box rows
box m = [[1, 2], [3, 4]]
trans(m)                   // transpose → BoxInstance
vdet(m)                    // determinant → Double
vinv(m)                    // inverse → BoxInstance
trace(m)                   // trace → Double
vdot.(m, m)                // matrix multiply → BoxInstance
```

#### File Consume

```pcb
container <<< "filename"   // read file lines into container body
```

#### Print & I/O

```pcb
print("hello world")      // forward print
tnirp("dlrow olleh")      // reverse print

save name into "filename" // write variable to file
read "filename" into name // read file into variable
```

#### FlatLand Integration

```pcb
FLCreate entityName type x y
FLDestroy entityName
FLMove entityName x y
FLsetValue entityName field value
```

---

### 6.16 ControlGraph Architecture

> **Status:** Design complete, invariants locked, `KnotAnalyzer` implemented, migration to `ControlGraph`-backed `KnotRunner` complete. The `KnotRunner` oscillation model is preserved — the ControlGraph formally grounds it.

#### Overview

The GPT design session produced a complete redesign of PCB's control-flow execution model. The core shift:

| Old model | New model |
|-----------|-----------|
| Linear scan through tokens | Node traversal over a static ControlGraph |
| Direction-driven ("ping-pong") | Edge selection → then direction as post-edge effect |
| Implicit loop/condition behavior | Explicit formal edge types |
| Structure and execution intertwined | Graph is static; runtime is a projection over it |

**One-line summary:** replaced *"execution as directional movement through text"* with *"execution as traversal over a statically defined control graph."*

---

#### ControlGraph fundamentals

A **ControlGraph** is a directed graph whose nodes are `ControlNode` objects (the bracket/label structures already built by `KnotRunner`). Edges are not implicit direction arrows — they are explicitly typed:

| Edge type | Meaning |
|-----------|---------|
| **Adjacency** | Move to the immediately adjacent ControlNode (both directions, always symmetric) |
| **Ownership** | Jump to the matched partner (MatchTable pairing) |
| **Condition** | Cross a condition boundary — true edge or false edge |
| **Entry** | Descend into the smallest valid nested interval (precomputed, deterministic) |
| **Unwind** | Move outward to the enclosing boundary (iterative, stops at outermost) |

The graph is **static** — it is built once from the token structure and never modified at runtime. Runtime execution is a *projection* over the graph: it selects edges, may flip direction as a post-edge effect, but cannot add or remove edges.

---

#### Condition model (formal)

A **CONDITION** is a cross-family boundary region — a region whose left and right `ControlNode` bracket tokens belong to different families (cup vs pocket).

- **TRUE edge** = the cross-family adjacency (the boundary itself)
- **FALSE edge** = the ownership return (back to the matching open/close partner)

Condition triggering rules (all must hold):
1. Arrival is via **adjacency**
2. Arrival crosses the boundary in the correct orientation
3. Arrival is **immediate** — a single edge, not via ownership jump or UNWIND
4. **No re-trigger** on the same node in the same step

If condition edges are invalid (no valid true/false targets), the condition becomes **non-participating** and its edges are removed from the candidate set. Fallback behavior applies — execution continues without condition evaluation.

---

#### Direction as post-edge effect

Direction no longer influences which edge is chosen. The sequence is:

1. Evaluate candidate edges from current node
2. **Select** the highest-priority valid edge
3. Traverse to target node
4. **Then** (and only then) — direction may flip as a side effect

This removes mid-step instability and non-deterministic traversal. Edge selection is a pure function of graph structure and current state; direction is output, not input.

---

#### ENTRY and UNWIND edge types

**ENTRY:** When execution needs to enter a nested structure, it descends to the *smallest valid nested interval* — precomputed, deterministic. No dynamic search at runtime.

**UNWIND:** When execution exits a nested structure, it moves outward through enclosing boundaries iteratively, stopping at the outermost boundary. This is the formal replacement for implicit nesting behavior.

Critical distinction:

```
ownership jump  ≠  unwind  ≠  adjacency
```

These three must never be conflated. `MatchTable` defines ownership/pairing. Traversal edges define movement. They are separate systems.

---

#### Reachability system

A **ReachabilityGraph** is a superset of the runtime graph — it includes ALL edges, ignoring direction, condition evaluation, and priority. It answers: *can node A ever reach node B regardless of runtime state?*

`ReachabilityKind` values:

| Kind | Meaning |
|------|---------|
| `DEFAULT` | Reachable via normal forward traversal |
| `REVERSE` | Reachable only via backward traversal |
| `CONDITIONAL` | Reachable only if a condition evaluates to true |
| `UNREACHABLE` | Not reachable from any starting state |

The **DefaultTraversalGraph** (the actual runtime graph) is a *filtered subset* of the ReachabilityGraph — it can only remove edges, never add them. This prevents runtime mutation and guarantees consistent behavior.

---

#### UnreachableGraph as first-class

Unreachable nodes and edges are not ignored — they form a structured `UnreachableGraph` with its own edge types:

| Edge type | Meaning |
|-----------|---------|
| `boundary` | Unreachable via structural boundary |
| `ownership` | Unreachable via ownership pairing |
| `enclosure` | Unreachable due to nesting enclosure |
| `crossing` | Unreachable due to crossing constraint |
| `mirror` | Unreachable due to directional mirror |
| `role` | Unreachable due to role incompatibility |

The UnreachableGraph supports deterministic BFS traversal and is a first-class static analysis artifact, not a discard pile.

---

#### Region model (stabilized)

- A **region** is an ordered pair of adjacent `ControlNode` objects
- Regions are **not traversed** — traversal is always node-based
- Region classification (`SETUP`, `CONDITION`, `BODY`, `ENTRY`, `UNWIND`, `EMPTY`) is a structural label used during graph construction, not a runtime execution unit

This eliminates region/node confusion in the implementation.

---

#### Termination and determinism

**Termination rule:** If no valid outgoing edges exist from the current node, execution terminates — unless an alternate mode is explicitly invoked. There are no undefined endings.

**Determinism guarantee:** Traversal is a pure function of:
- The ControlGraph (static structure)
- The initial runtime state (starting node + direction)

No dependence on iteration order, data structure ordering, or evaluation timing. Given the same graph and same initial state, traversal is always identical.

---

#### Relationship to KnotRunner (migration complete)

`KnotRunner` has been fully migrated to use `KnotAnalyzer` and `ControlGraph`. The prior manual implementation — `Condition` objects, `condForward`/`condBackward` tables, inner `ControlNode`/`Region`/`MatchTable` classes — has been removed.

| Old KnotRunner | Current KnotRunner |
|----------------|--------------------|
| `Condition` with manual `indexStart`/`indexTrue`/`indexFalse` | Explicit typed edges in ControlGraph |
| `condForward` / `condBackward` `Conditions` tables, lexeme-keyed | `graph.hasForwardCondition(i)`, `forwardConditionTrueTarget(i)`, `forwardConditionFalseTarget(i)` |
| Oscillation detection via linear search over condition lists | `graph.forwardConditionStartForFalseExit(i)` / `graph.backwardConditionStartForTrueEntry(i)` (reverse-lookup methods added to ControlGraph) |
| `buildControlNodes` / `buildMatches` / `buildRegions` / `classifyRegions` / `buildConditionsFromRegions` (inner methods) | `new KnotAnalyzer(stmts).analyze()` in constructor |
| Inner `ControlNode`, `Region`, `MatchTable`, `Condition`, `Conditions` classes | Package-level `ControlNode`, `ControlRegion`, `MatchTable` via ControlGraph |
| `runSetupRegions` iterating inner `Region` list | `graph.getRegions()` filtered by `RegionKind.SETUP` |
| `isKnotControl(i)` predicate | `graph.getNodeAt(i) != null` |
| `resolveRouteTarget` via `condForward`/`condBackward` lexeme lookup | `resolveRouteTarget` via first `CONDITION` region in `graph.getRegions()`, using `ControlNode.normalizedLabel` for target name |

The oscillation execution semantics (`stepForwardControl` / `stepBackwardControl`) are preserved exactly — the ControlGraph supplies the same structural information that the manual tables did, just via typed graph queries instead of list scans.

---

## 7. Monopoly Game Implementation

### 7.1 Board

**File:** `TheGame/Board.java`

`Board` is the central Monopoly data structure. It manages all game state.

```java
public class Board {
    int width = 1100;
    int height = 1100;
    static ArrayList<BoardSpace>          gameSpaces;
    static ArrayList<BoardSpace>          freeSpaces;
    static HashMap<PlayerWrper, ArrayList<BoardSpace>> takenSpaces;
    ArrayList<SpaceConnections>           connections;
    BoardRules                            rules;
    RuleInterpreter                       rI;
    ArrayList<PlayerWrper>                players;
    HashMap<Integer, PlayerWrper>         turnOrderPlayer;
    HashMap<PlayerWrper, Integer>         playersTurnOrder;
    Integer                               currentTurn = 0;
    HashMap<PlayerWrper, PlayerSpaces>    portfolios;
    HashMap<PlayerWrper, PlayerChance>    chance;
    HashMap<PlayerWrper, PlayerCommieChest> comunism;
    HashMap<PlayerWrper, ArrayList<BoardSpace>> currentPositions;
    HashMap<PlayerWrper, Status>          statusus;
    ArrayList<RoundAction>                roundActionHistory;
    ArrayList<Actn>                       actionsToTakeThisRound;
    ArrayList<HashMap<PlayerWrper, PlayerPositions>> posHist;
    private GameDice                      instance;
}
```

`gameSpaces` and `freeSpaces` are `static` — shared across all `Board` instances. This is an unusual design choice that means two boards would share the same space lists.

`GameDice` is obtained as a Singleton in the constructor.

`updatePlayerPositionsBasedOnBoardRules()` is defined but its body is empty — positions are updated elsewhere or this is a placeholder.

---

### 7.2 Board Spaces & Status

| Class | Description |
|-------|-------------|
| `BoardSpace` | A renderable board square with graphical representation |
| `Space` | Interface defining the minimal contract for any board position |
| `SpaceConnections` | Represents the connections/paths between board spaces |
| `Status` (enum) | `GO, PROPERTY, RAILROAD, UTILITY, TAX, CHANCE, COMMUNITY_CHEST, JAIL, FREE_PARKING, GOTO_JAIL` |

`BoardSpace` implements the `Sprites` interface, making it renderable by the FlatLand engine.

---

### 7.3 Player Management

| Class | Description |
|-------|-------------|
| `PlayerWrper` | Wraps a player for the board context; also extends `FlatLander` (via XMLLEVELLOADER) |
| `PlayerPositions` | Tracks a player's board position |
| `PlayerSpaces` | Tracks a player's property portfolio |
| `PlayerChance` | Tracks a player's chance cards |
| `PlayerCommieChest` | Tracks a player's community chest cards (renamed "Commie Chest") |
| `PlayerStatus` | Player status data (in jail, bankrupt, etc.) |
| `GameDice` | Singleton dice roller |
| `GameInstance` | Game state manager |

**Note on "Commie Chest":** The standard Monopoly "Community Chest" is renamed `CommieChest`/`PlayerCommieChest` throughout the codebase. The `HashMap` field holding it is named `comunism`. This is an intentional naming choice by the developer.

Turn order is tracked via two complementary maps:
```java
HashMap<Integer, PlayerWrper>  turnOrderPlayer    // turn# → player
HashMap<PlayerWrper, Integer>  playersTurnOrder   // player → turn#
```
`currentTurn` increments each round.

---

### 7.4 MonopolyActions

**Package:** `MonopolyActions/`  
**Base class:** `Actn.java` (abstract action)

Every game event in Monopoly is modeled as an `Actn` subclass. This is the Strategy/Command pattern applied to board game rules.

| Action Class | Monopoly Event |
|--------------|----------------|
| `Move` | Player moves N spaces |
| `GoAction` | Player lands on or passes GO, collects $200 |
| `Collect` | Generic income collection |
| `Buy` | Purchase a property |
| `Sell` | Sell a property |
| `Rent` | Pay rent to owner |
| `IncomeTax` | Pay income tax |
| `LuxeryTax` | Pay luxury tax (note: misspelling preserved) |
| `Morgatge` | Mortgage a property (note: misspelling preserved) |
| `Chance` | Draw a chance card |
| `Chest` | Draw a community (commie) chest card |
| `CCSquare` | Community chest square event |
| `Utilities` | Pay utility bill |
| `RailRoad` | Pay railroad rent |
| `FreeParking` | Land on free parking |
| `GTJail` | Go to jail |
| `Visiting` | Visiting jail (just passing) |
| `RoundAction` | Composite record of a full round's actions |

`EventAction.java` is an event-driven action that fires in response to game events rather than being directly invoked.

```
Actn (abstract)
├── Move
├── Buy / Sell
├── Rent
├── Chance / Chest / CCSquare
├── IncomeTax / LuxeryTax
├── Morgatge
├── FreeParking
├── GTJail / Visiting
├── GoAction / Collect
├── Utilities / RailRoad
├── EventAction
└── RoundAction
```

---

### 7.5 Rules Engine

**Package:** `Rules/`

| Class | Description |
|-------|-------------|
| `BoardRules` | Data class holding all rule definitions |
| `PropertyRule` | Rules specific to property ownership |
| `RuleInterpreter` | Executes rules against game state |
| `TheRules` | Master rule set compilation |

`RuleInterpreter` is constructed with a `BoardRules` reference and called by `Board` to validate and apply actions.

---

### 7.6 Island System

**Package:** `Island/`

The Island system represents Monopoly's geographic groupings (color groups, etc.) as "islands."

| Class | Description |
|-------|-------------|
| `Island` | Data structure for a group of related board spaces |
| `IslandIndex` | Registry/index of all islands |
| `Feature` | A feature belonging to an island |
| `Interactable` | Interface for elements the player can interact with |

---

## 8. Actions System

**Package:** `Actions/` (17 files)

The Actions system is a command-object framework for movement and visual operations. It is separate from the Monopoly actions.

```
Actions (abstract base)
├── ActionsInterface (interface)
├── ActionStatus (enum: PENDING, IN_PROGRESS, COMPLETE, CANCELLED)
├── ActionStack (queue/stack of pending actions)
├── Physics (physics action)
├── DrawACircle
├── DrawArc
├── DrawArcFasterVersion1     (optimized arc variant)
├── DrawABlob
├── DrawAProtoCloud
├── GoInAStraightLineFor      (linear movement for N frames)
├── MoveBetween               (interpolated movement between two points)
├── MoveByXY                  (direct displacement)
├── Wonder                    (random wandering behavior)
├── AVaugeSenseOfHavingDoneSomething  (placeholder/null action with intentional name)
├── ClearFlatLand             (clear the screen/world)
└── NoAction                  (explicit null object pattern)
```

`AVaugeSenseOfHavingDoneSomething` is an intentionally named placeholder — it represents the "I ran but did nothing meaningful" state. `NoAction` is the formal null object.

`DrawArcFasterVersion1` is an interesting naming choice: it documents an explicit performance optimization attempt in the class name itself.

`Wonder` implements wandering AI behavior — an entity using this action will move in random directions over time.

`ActionStack` allows entities to queue sequences of actions. This supports scripted sequences: push actions onto the stack and they execute in order.

---

## 9. Object Classification System (fication)

**Package:** `fication/`

The `fication` package provides a classification hierarchy for game objects. The name is a suffix: "classifi-cation", "objecti-fication", "personifi-cation" — only the suffix is kept as the package name.

```
fication/
├── ENV/
│   ├── ENV.java          (interface)
│   ├── A_ENV.java        (abstract base)
│   ├── Ground.java       (concrete) ← A_Ground.java (abstract)
│   ├── Platform.java     (concrete) ← A_Platform.java (abstract)
│   ├── Structure.java    (concrete) ← A_Structure.java (abstract)
│   └── Scenery.java      (concrete) ← A_Scenery.java (abstract)
├── Objectification/
│   ├── OBJ.java          (interface)
│   └── A_OBJ.java        (abstract base)
└── Personification/
    ├── PPL.java           (interface)
    └── A_PPL.java         (abstract base)
```

The pattern throughout is: `interface → abstract A_Class → concrete Class`. The `A_` prefix signals an abstract implementation.

This is a parallel classification system to `FlatLanderClassification` — the enum classifies entities by role (PLAYER, ENEMY, NPC), while `fication` classifies objects by physical nature (is it ground? a platform? a person?).

---

## 10. Entity State Systems

These packages follow a consistent pattern: each has an interface, an abstract implementation (`A_` prefix), and a `Token` class for state persistence.

### 10.1 DEG — Degradation

**Package:** `DEG/`

```
Degrades.java         (interface: can degrade over time)
A_Degrades.java       (abstract base implementation)
DegradesToken.java    (state token for recording degradation)
```

An entity that implements `Degrades` can reduce in quality/health over time. `DegradesToken` is a snapshot of degradation state used for serialization or history.

### 10.2 DES — Destruction

**Package:** `DES/`

```
Destruction.java      (interface: can be destroyed)
A_Destruction.java    (abstract base)
DestructionToken.java (state token)
```

Separate from degradation: destruction is binary (destroyed/not) while degradation is graduated.

### 10.3 SP_SUD — Spontaneous Spawn/Destroy

**Package:** `SP_SUD/`

```
SpontaniousSpawn.java       (interface: can appear spontaneously)
A_SSpawn.java               (abstract base)
SpontaniousDestruction.java (interface: can vanish spontaneously)
A_SDestruction.java         (abstract base)
[Token variants for each]
```

Spontaneous events are triggered by game conditions rather than player input. Note the consistent misspelling: "Spontanious" (should be "Spontaneous") is preserved throughout.

### 10.4 PRG — Programmable Objects

**Package:** `PRG/`

```
Programmable.java    (interface: can have PCB scripts injected at runtime)
ProgToken.java       (runtime token for the injected program)
```

The `Programmable` interface is the bridge between the state system and the PCB interpreter. An entity implementing `Programmable` can have PCB source code attached to it, which is executed in response to game events. This is the planned mechanism for user-scriptable game objects.

### 10.5 PER — Permanent Objects

**Package:** `PER/`

```
Perminant.java        (interface: object persists between levels)
PerminantToken.java   (persistence token)
```

Note: "Perminant" is a consistent misspelling of "Permanent" throughout this package.

### 10.6 UPGRD — Upgradable Objects

**Package:** `UPGRD/`

```
Upgradable.java     (interface: can be upgraded)
UPGRADABLE.java     (variant interface)
A_Upgradable.java   (abstract base)
```

The presence of both `Upgradable` and `UPGRADABLE` suggests an in-progress refactor where a second interface was created before the first was removed.

### 10.7 ITM — Item System

**Package:** `ITM/`

```
ITM.java       (interface: is an item)
A_ITM.java     (abstract base)
```

Generic item interface. Any entity implementing `ITM` can be picked up, carried, or used.

---

## 11. Audio System (audiolizer)

**Package:** `audiolizer/`

```
InterpreterAudio.java   (entry point)
MidiAudioSink.java      (MIDI synthesis output)
PcmAudioSink.java       (PCM/raw audio output)
```

The `InterpreterAudio` class drives audio output based on interpreter events. Two sinks are available:

- **MIDI:** Generates musical note events via Java's MIDI API
- **PCM:** Generates raw PCM audio samples

This allows the PCB interpreter or game events to trigger audio. The exact mapping of interpreter events to audio events is in `InterpreterAudio`.

A legacy copy exists in `java.src/audiolizer/` — an earlier implementation preserved as reference.

---

## 12. Computer Vision (CV)

**Package:** `CV/`

Dependencies:
- `com.github.sarxos:webcam-capture:0.3.10` — webcam access
- `org.bytedeco:javacv-platform:1.5.10` — OpenCV wrapper for Java

The CV package provides webcam capture and face detection capabilities. The `WebcamUpdater` in `theStart/theView/` runs this on its own thread, continuously updating webcam frames.

This feature is running in the live game loop (started in `BANG.main()`), suggesting the webcam feed is intended to be visible in the game UI or influence game state based on player presence/expression.

---

## 13. Neuron System (Nuron)

**Package:** `Nuron/` (note misspelling of "Neuron")  
**Also:** `theStart/theStuff/Synapse*.java`

```
Synapse.java         (a single synapse connection)
SynapsePair.java     (pair of connected synapses)
SynapseType.java     (enum: excitatory, inhibitory, etc.)
SynapseFaceBook.java (singleton registry of all synapses)
```

This is a neural network data structure implementation. Synapses connect entities, and the `SynapseFaceBook` (following the "Facebook" registry naming pattern) tracks all active connections. The exact usage — whether this is for NPC AI, procedural content generation, or something else — is not fully implemented but the structure suggests it could drive emergent entity behavior.

The neuron count is one of the four parameters prompted from the user at startup (`BANG.main`), indicating this system is active even in the current state.

---

## 14. Utility & Supporting Systems

### Constructs

**Package:** `Constructs/`

```
Point.java      (2D coordinate: int x, int y)
Construct.java  (geometric construct, wraps Points)
```

`Point` and `Construct` are the base geometry types used throughout the engine for positions, paths, and memory.

### Math

**Package:** `Math/`

```
GameDice.java        (Singleton dice roller)
[probability utilities]
```

`GameDice` is the Monopoly game's random number source. It is a Singleton retrieved via `GameDice.getInstance()`.

### Logging

The project depends on an external `LOGGING:LOGGING:0.0.1-SNAPSHOT` Maven artifact. `Logging.LOG` is an abstract class that appears throughout the codebase as a base class. It requires implementing `some_awesome_function_that_is_totaly_finished_and_not_made_up_oh_hey_look_over_there(...)` — a stub method with an intentionally absurd name and signature. Every class extending `LOG` carries this method stub.

### Randomization

`theStart/theStuff/FlatLanderRandom.java` — A seeded random number generator. The seed is provided at startup (user input in BANG), enabling deterministic game runs. The seed range 0–16777215 corresponds to 24-bit color space (2^24 - 1).

### Animation

**Package:** `animation/`

```
Animations.java   (animation state controller)
Asset.java        (a single animation frame or asset)
```

`Asset` is a wrapper for `BufferedImage` frames used in sprite animations. `Animations` controls which frame is currently displayed based on entity state.

### Visualization

**Package:** `visualizer/`

JSON visualization utilities. Likely used for debugging PCB interpreter state — serializing AST or environment state to JSON for external inspection. A legacy copy exists in `java.src/visualizer/`.

### Dialog Management

**Package:** `dialogManagement/`

A dialogue tree system for NPC conversations. Not detailed in source exploration but present as a package, suggesting NPC interaction scripting is planned.

### FSM — Finite State Machine

**Package:** `FSM/`

A generic finite state machine implementation for game mode management. Game states (title screen, playing, paused, game over) transition through this FSM.

---

## 15. Design Patterns

### Observer Pattern

Used heavily throughout the project, in two distinct contexts:

1. **PCB output notification:** `Box/Box/Observer.java` → `PromptObserver.java` — when the PCB interpreter produces output, it notifies the `PromptObserver` which pushes it to the game terminal UI
2. **Game event observation:** `View/Observable.java` → `View/Observer.java` — `FPSObserver`, `TimeObserver`, `GeneralFieldObserver` observe game loop events

### Singleton Pattern

| Singleton | Purpose |
|-----------|---------|
| `GameStatus.getInstance()` | Global game state enum |
| `FlatLandFacebook.getInstance()` | Entity registry |
| `GameDice.getInstance()` | Dice roller |
| `UpdateTimeSingleton.getInstance()` | Global game clock |
| `SynapseFaceBook.getInstance()` | Synapse registry |

### Strategy / Command Pattern

Both `Actions` (game movement) and `MonopolyActions` (board game events) use the Strategy/Command pattern: an abstract base (`Actions`, `Actn`) with many concrete implementations.

### Visitor Pattern

Used in the PCB interpreter: `Declaration.Visitor<Object>` is implemented by `Interpreter`. Every AST node type accepts a visitor, enabling the interpreter to walk the tree without the nodes knowing how they are processed.

### Null Object Pattern

`Actions/NoAction.java` is a formal Null Object — "do nothing" that can be put on the action stack without null checks.

### Template Method Pattern

`FlatLander` defines the `update()` template method that calls `moveX()` and `moveY()`. Subclasses can override these to change movement behavior. `MonopolyActions/Actn` defines the abstract action template.

### Factory via Enum

`SpriteType` and `FlatLanderClassification` enums contain factory-like semantics for creating the right type of sprite or classifying entities.

### MVC (loose)

- **Model:** `FlatLander`, `Board`, `GameStatus`, `Environment`
- **View:** `FlatLandWindow`, `TheStartCamera`, `GameScreen`
- **Controller:** `KeyBoardHandler`, `EventHandler`, `RuleInterpreter`

### Composite Pattern

`ImagePile` (layered image stack) and `Board` (collection of `BoardSpace` objects) use Composite.

### Adapter Pattern

`PlayerWrper` and `FlatLanderWrper` (XMLLEVELLOADER) adapt XML-loaded data into the FlatLander entity system.

---

## 16. Full Data Flow Diagrams

### Game Initialization Flow

```
User runs BANG.main()
    │
    ├── User inputs: width, height, seed, neuron count
    │
    ├── new ViewableFlatLand(width, height, true)
    │       └── world container with dimensions + time counter
    │
    ├── new FlatLandWindow(canvas)
    │       └── Swing window setup
    │
    ├── new TheStartCamera(w, h, 0, 0, flatland, seed, nroncount, canvas)
    │       ├── Sets up keyboard bindings
    │       ├── Initializes sprite renderer
    │       └── Connects to FlatLandFacebook
    │
    ├── camera.setKeyBindingsForPlayer(flatLandWindow)
    │
    ├── new WebcamUpdater() → thread.start()
    │       └── Continuous webcam frame capture
    │
    └── while(true):
            camera.takePictureOfFlatLand()
                    ├── Iterate FlatLandFacebook entities
                    ├── Call sprite.update(flatLander) per entity
                    ├── Compose ImagePile layers
                    └── Repaint canvas
            flatland.setTime(t + 1)
```

### PCB Interpreter Pipeline

```
Source Code String
    │
    ▼
Scanner.scanTokensFirstPass()
    ├── Character-by-character scan
    ├── Keyword lookup (HashMap)
    ├── Track line + column
    └── Emit: List<Token>
    │
    ▼
Grouper.scanTokensSecondPass()
    ├── Stack-based bracket matching
    ├── Group container open/close pairs
    └── Emit: ArrayList<Token> (with ContainerIndexes)
    │
    ▼
ParserTest.parse()
    ├── Recursive descent parsing
    ├── Build Declaration / Expr / Stmt / Fun AST nodes
    └── Emit: List<Declaration>
    │
    ▼
Resolver.resolve(statements)
    ├── Walk AST, resolve variable references to scope depths
    ├── Store depths in interpreter.locals map
    └── Modifies: Interpreter's locals map (in place)
    │
    ▼
Interpreter.interpret(statements)
    ├── Visit each Declaration
    ├── Environment manages variable scopes
    ├── BoxInstance / CupInstance / PocketInstance hold container values
    ├── Returns / Snruter for non-local returns
    └── FL* statements call into FlatLandFacebook
    │
    ▼
Output to ByteArrayOutputStream
    └── Observer.notify(output) → Game terminal UI
```

### Monopoly Turn Flow

```
Board.addPlayers()
    └── Initialize player positions at GO, create portfolios

Roll Dice (GameDice.getInstance().roll())
    │
    ▼
Calculate new position (Move action)
    │
    ▼
Determine BoardSpace at position
    └── Check Status enum (GO, PROPERTY, CHANCE, etc.)
    │
    ▼
RuleInterpreter.apply(rule, player, board)
    │
    ├── PROPERTY: Buy (if unowned) or Rent (if owned)
    ├── GO: GoAction (collect $200)
    ├── CHANCE: Draw Chance card → any action
    ├── COMMUNITY_CHEST: Draw CommieChest card → any action
    ├── INCOME_TAX / LUXURY_TAX: Collect tax
    ├── RAILROAD: Pay railroad rent
    ├── UTILITY: Pay utility
    ├── FREE_PARKING: Nothing
    ├── GOTO_JAIL: Move to jail
    └── JAIL: Visiting (nothing) or pay to leave
    │
    ▼
Record RoundAction in roundActionHistory
    │
    ▼
currentTurn++, next player
```

### PCBServer Request Flow

```
HTTP POST /run { source, direction }
    │
    ├── Read body JSON
    ├── Extract "source" and "direction" fields
    ├── Build seed preamble (inject box variables for loaded seeds)
    ├── Concatenate preamble + source
    ├── Redirect System.out/err to ByteArrayOutputStream
    ├── sandbox.runJson(fullSource, forward)
    │       └── Full PCB pipeline (see above)
    ├── Restore System.out/err
    ├── Capture output string
    ├── parseBindings(source) → extract container declarations
    └── Return JSON: { ok, output, executionTime, direction, interfaceSeedId, bindings }
```

---

## 17. Build Configuration & Dependencies

**File:** `pom.xml`

```xml
<groupId>FlatLand</groupId>
<artifactId>trainingGround</artifactId>
<version>0.0.1-SNAPSHOT</version>
<packaging>jar</packaging>
```

Java version: **21** (via `maven.compiler.release`)  
Source encoding: **UTF-8**  
Source directory: `src` (non-standard; Maven default is `src/main/java` — this points to `src` directly)

### External Dependencies

| Artifact | Version | Purpose |
|----------|---------|---------|
| `com.github.sarxos:webcam-capture` | 0.3.10 | Webcam access for CV system |
| `org.bytedeco:javacv-platform` | 1.5.10 | OpenCV Java bindings for computer vision |
| `com.fasterxml.jackson.core:jackson-databind` | 2.15.2 | JSON serialization (PCBServer, visualizer) |
| `org.mockito:mockito-core` | 5.20.0 | Unit testing mocks |

### Local/Internal Maven Dependencies

These are local projects installed in the local Maven repo:

| Artifact | Version | Purpose |
|----------|---------|---------|
| `AntAnimation:AntAnimation` | 0.0.1-SNAPSHOT | Animation library (external project) |
| `LOGGING:LOGGING` | 0.0.1-SNAPSHOT | Logging framework (LOG abstract class) |
| `the:VectorServer` | 0.0.1-SNAPSHOT | Network/vector server |
| `BoardTemplate:BoardTemplate` | 0.0.1-SNAPSHOT | Monopoly board template |
| `ScreenIntegration:ScreenIntegration` | 0.0.1-SNAPSHOT | UI screen integration |

All local dependencies are `SNAPSHOT` versions, meaning they are expected to be built and installed locally before this project can build.

**Build note:** `javacv-platform` is a large dependency (includes native OpenCV libraries for all platforms). It will significantly increase the build artifact size.

---

## 18. Notable Design Choices & Quirks

These are design decisions that are unusual or surprising and worth calling out explicitly:

### 1. Reverse Keywords Throughout The Language

Every PCB keyword has a reversed spelling (`print`/`tnirp`, `run`/`nur`, `if`/`fi`). This is not a bug — it is the language's bidirectional execution system. The reversed forms are systematically defined in the scanner keyword map, have their own token types, and the interpreter has separate visit methods for them. It is an ambitious and unusual language design choice.

### 2. Brute-Force Case-Insensitivity for `#HATTAG`

Rather than using `String.equalsIgnoreCase()`, the scanner maps all 16+ capitalisation variants of `#HATTAG` individually. This works but produces a very verbose keyword map. The word `#GATTAH` (HATTAG backward) is also registered.

### 3. `getMass()` Returns a Constant

All entities return `100.0` for their mass. This makes air resistance uniform across all entity types. The parameter exists in the Physics constructor but its only effect is dividing `airResistance` by 100.

### 4. Global Game Time in Physics

`fallDistance()` uses the global `UpdateTimeSingleton` time, meaning all entities are at the same "moment in time" relative to physics. An entity that has been in the world for 1000 frames falls at the same acceleration as one just spawned.

### 5. Double-Increment Velocity Bug

In `applyPhysics()`: `changeMoveYBy(getMoveY() + fallDistance)` where `changeMoveYBy` *adds* to the existing value. This means each frame the velocity increases by both its current value plus gravity, creating quadratic growth in Y velocity.

### 6. Static Board Spaces

`Board.gameSpaces` and `Board.freeSpaces` are `static`. Multiple `Board` instances would share these lists — board state is effectively global.

### 7. Two Parallel Entity Systems

`FlatLander/FlatLander.java` and `theStart/thePeople/FlatLander.java` are two separate classes with similar but not identical implementations. This duplication appears to be historical — the bootstrap copy was created early and not yet unified with the main package.

### 8. The Absurd Method Signature in Physics & BANG

`some_awesome_function_that_is_totaly_finished_and_not_made_up_oh_hey_look_over_there(...)` exists in `Physics.java`, `BANG.java`, and wherever `LOG` is extended. Its parameter names include `somefuckingnumberthatisjustfuckingmadeupbyheywhoare_you_what_are_you_doing_arrrrrrrrgh` and `mytotalbankedXXX_user_ACCESS_RESTRICTED_XXX`. This is an Eclipse auto-generated stub that the developer gave up naming seriously. It is required by the abstract `LOG` class contract and is a `// TODO` stub in every implementation.

### 9. "Commie Chest" Naming

Community Chest is renamed "Commie Chest" (`CommieChest`, `PlayerCommieChest`) and the field holding it is `comunism`. This is an intentional developer humor choice preserved consistently throughout the Monopoly implementation.

### 10. The PCB `PCB` and `PCBDEFINITION` Packages Are Empty

Two packages — `PCB/` and `PCBDEFINITION/` — exist in the directory tree but contain no source files. They are placeholder namespaces for future PCB definition types.

### 11. `BootStrap.main()` Is Entirely Commented Out

`BootStrap.java` exists and has a `main` method, but its only content is commented-out multi-world experiment code. It is not the current entry point.

### 12. `ParserOLD.java` and `*OLD` Pattern

Files with `OLD` suffix (`ParserOLD.java`, `DeclarationOLD.java`, `ExprOLD.java`, `StmtOLD.java`, `FunOLD.java`) are preserved old implementations alongside their replacements. This gives a historical view of how the parser evolved.

### 13. WesMap

`WesMap.java` is a custom map named after the developer. Its exact behavioral differences from standard Java maps are not documented in comments.

### 14. Magic Seed Range 0–16777215

The user-input random seed range `0–16777215` corresponds exactly to 24-bit color space (2²⁴ - 1 = 16,777,215). The developer likely chose this range because it maps directly to RGB color values, potentially seeding visual as well as behavioral randomness.

---

## 19. Developer Notes Context

The file `Notes/content.txt` is a developer journal, not technical documentation. It provides important context for understanding the project's motivation and direction.

Key points from the journal (reproduced here for context with sensitive material omitted):

**On the project's purpose:**
> "this game does have a recovery focus. i mean its a little more then a focus. E><3 is recovery renewal and for me at least hope."

**On the release timeline:**
> "i have 790 days until october 8th 2026 which is the release date."
(Written August 2024)

**On the interpreter system:**
> "really this is my exploration into interpreters so this will walk up to the edge of what is active. words words words."

**On the liminal aesthetic:**
> "welcome to liminal space this was repurposed from a bad dream"

**On planned terminal features:**
> "terminal features: story mode, code mode, scripting mode"

**On the environment:**
> "ok so there is an environment that is the level. the environment has flatland and flatlanders and events"

This context explains several otherwise puzzling design choices:
- The PCB language is not just a game scripting tool — it is the developer's primary exploration vehicle for understanding interpreters
- The "liminal space" aesthetic explains the Monopoly + platformer combination in an unexpected space
- "Story mode / code mode / scripting mode" maps to the three terminal states represented by `TerminalSprite` and the PCB server's multiple execution modes
- The personal and therapeutic context explains why the project is ambitious, exploratory, and sometimes structurally unconventional — it is built to learn, not to ship a polished architecture

---

**Test suite status (as of 2026-05-01):** 22 suites, 913 tests total. New suites added since last update: `VectorMatrixTest` (119 tests), `AssertTest` (16), `FresnelCTest` (16), `PocketCascadeDeathTest` (23).

*Document last updated 2026-05-01. Covers all 298+ Java source files across 47 packages in `src/main/java/`. New file since last update: `Box/Interpreter/VecMatHelper.java`.*
