# CLAUDE.md — LePendu (Bonhomme Pendu)

This file provides guidance for AI assistants working on this codebase.

---

## Project Overview

**LePendu** is a French-Canadian Hangman word-guessing game built as a native Android application (Java). It was developed as a student assignment (TP1) at Collège Rosemount in April 2023.

- **Language:** Java 8
- **Platform:** Android (minSdk 21 / targetSdk 33)
- **Build system:** Gradle 7.4.1 with Android Gradle Plugin
- **Package:** `com.example.lependu`

---

## Repository Structure

```
Bonhomme-pendu/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml          # App manifest (2 activities)
│   │   │   ├── java/com/example/lependu/
│   │   │   │   ├── Pendu.java               # Main activity (UI + game loop)
│   │   │   │   ├── Jeu.java                 # Game logic (word selection, scoring)
│   │   │   │   └── Status.java              # Results activity (win/loss screen)
│   │   │   └── res/
│   │   │       ├── drawable/                # 8 JPG images: accueil + err01–err06
│   │   │       ├── layout/                  # activity_main.xml, activity_status.xml
│   │   │       ├── mipmap-*/                # App launcher icons (multiple densities)
│   │   │       ├── values/                  # colors.xml, strings.xml, themes.xml
│   │   │       └── values-fr/              # French-Canadian string overrides
│   │   ├── test/
│   │   │   └── java/com/example/lependu/
│   │   │       └── ExampleUnitTest.java     # JUnit 4 unit tests for Jeu.java
│   │   └── androidTest/
│   │       └── java/com/example/lependu/
│   │           └── ExampleInstrumentedTest.java  # Espresso UI tests
│   ├── build.gradle                         # App-level build config
│   └── proguard-rules.pro
├── build.gradle                             # Root Gradle config
├── gradle.properties                        # JVM args, AndroidX flags
├── settings.gradle                          # Project name + module config
└── gradlew / gradlew.bat                    # Gradle wrapper scripts
```

---

## Architecture

The app uses a simple 3-class MVC-like structure:

### `Jeu.java` — Model / Game Logic
- Receives an array of words in its constructor; throws `IllegalArgumentException` if empty
- Randomly selects a word to guess
- Tracks: current revealed letters, error count (`nbErreurs`, max 6), score (`pointage`)
- Key methods:
  - `essayerUneLettre(char)` → `int[]` of positions where the letter appears (empty if wrong)
  - `getPointage()` → current score
  - `getNbErreurs()` → number of wrong guesses
  - `estReussi()` → `true` when all letters have been guessed
  - `resetPointage()` / `resetnbrErreur()` → reset state

### `Pendu.java` — Main Activity (Controller/View)
- Implements `View.OnClickListener` for 26 letter buttons (A–Z) and a reset button
- On each letter click: calls `jeu.essayerUneLettre()`, updates UI, checks win/loss
- Launches `Status` activity via Intent on game end (passes result string and score)
- Default word list (French, Western theme):
  ```java
  {"chapeau", "revolver", "cheval", "bottes", "cowboy", "vache", "lasso", "indien", "desert", "cactus"}
  ```

### `Status.java` — Results Activity (View)
- Receives `String` result message and `int` score via Intent extras
- Displays outcome and "Play Again" button that navigates back to `Pendu`

---

## Build & Run

### Prerequisites
- Android SDK with API 21–33 installed
- Java 8 (JDK 8+)
- Android device or emulator for instrumented tests

### Common Gradle Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run JVM unit tests (no device needed)
./gradlew test

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Clean build artifacts
./gradlew clean

# Full clean build
./gradlew clean assembleDebug
```

### Build Outputs
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Test results: `app/build/reports/tests/testDebugUnitTest/index.html`

---

## Testing

Tests live in `app/src/test/java/com/example/lependu/ExampleUnitTest.java`.

Current unit test coverage (`Jeu.java`):
| Test | What it verifies |
|------|-----------------|
| `testConstructeurListeVide()` | Empty word array throws `IllegalArgumentException` |
| `testMotADeviner()` | Selected word is lowercase letters only |
| `testEssayerUneLettre()` | Correct positions returned for a letter in "bonjour" |
| `testEstReussi()` | Win condition triggers after guessing all letters |

When adding new tests, follow JUnit 4 style (`@Test` annotation) and place them in the same package.

---

## Code Conventions

- **Language mix:** Code and variable names use both French (`motAdeviner`, `nbErreurs`, `essayerUneLettre`) and English (Android/Java idioms). Preserve this style.
- **Naming:**
  - Classes: PascalCase (`Pendu`, `Jeu`, `Status`)
  - Variables/methods: camelCase, often French words
  - Resources: snake_case XML IDs and filenames
- **UI pattern:** `findViewById()` — no ViewBinding or DataBinding used; keep it consistent
- **Activities:** Extend `AppCompatActivity`; use `startActivity(Intent)` for navigation
- **No dependency injection:** All construction is manual

---

## Known Issues / Quirks

1. **Letter display bug:** Letter-by-letter reveal in `Pendu.java` is not fully implemented (see lines ~86–87). The word display logic is partial.
2. **Score halved intentionally:** Score is divided by 2 to compensate for a double-counting bug (see `Pendu.java` line ~234). Do not "fix" the division without also fixing the root cause.
3. **Large switch statement:** The 26-case switch for letter buttons in `Pendu.java` is intentional given the constraints of the assignment. Refactoring to a loop/map is welcome but not required.
4. **Debug output:** `System.out.println` statements exist in the code for debugging. These are harmless but could be removed.

---

## Localization

- Default strings: `app/src/main/res/values/strings.xml` (English)
- French-Canadian overrides: `app/src/main/res/values-fr/values_fr-rCA.xml`
- Keys: `app_name`, `game_start`, `word_label`, `result_message`, `replay_button`
- When adding new user-facing strings, add entries in both files.

---

## Git History

| Commit | Date | Message |
|--------|------|---------|
| `1dffd9b` | 2023-04-23 | remise tp1 (main implementation) |
| `318def6` | 2023-03-14 | commit initial (project scaffold) |

---

## Key Constraints for AI Assistants

- **Do not upgrade the target/compile SDK** without verifying all deprecated API usages first.
- **Do not switch to Kotlin** unless explicitly requested; this is a Java project.
- **Preserve French naming** in game-logic code (`Jeu.java`); it matches the original assignment spec.
- **Test before committing:** Always run `./gradlew test` to ensure unit tests pass after any change to `Jeu.java`.
- **Hangman images are fixed:** The drawable folder contains exactly 7 images (accueil + 6 error states). The game logic is coupled to exactly 6 errors max (`nbErreurs == 6` → loss).
