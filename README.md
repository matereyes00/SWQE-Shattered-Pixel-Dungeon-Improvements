# ELEC5618: Shattered Pixel Dungeon Quality Engineering

This repository contains our group’s work for **ELEC5618 Software Quality Engineering — Assignment 3**.  
We are extending **Shattered Pixel Dungeon v3.0.2** through three source-code-level quality improvements, supported by testing, verification, and a structured GitHub workflow.

---

## Project Overview

The objective of this assignment is to improve the quality of an existing open-source Java project using the **ISO/IEC 25010 software quality model**.

Our group is implementinggg:

1. **In-Game Screenshot Shortcut**  
   Improves **usability** and **operability**.

2. **Software Quality Bug Fixes and Behaviour Corrections**  
   Improves **functional suitability**, **reliability**, and **maintainability**.

3. **Crash-Safe Save Backup and Recovery**  
   Improves **reliability**, **fault tolerance**, and **recoverability**.

Each improvement is supported by implementation work, testing evidence, and verification activities.

---

## Repository Structure

```text
SWQE-Shattered-Pixel-Dungeon-Improvements/
│
├── target_project/                  # Active Shattered Pixel Dungeon source code
│   ├── core/                        # Main gameplay logic
│   ├── desktop/                     # Desktop launcher
│   ├── SPD-classes/                 # Shared engine and utility classes
│   └── services/                    # News/update services
│
├── .github/workflows/              # CI build workflow
├── .gitignore                       # Ignores generated/local development files
├── README.md                        # Project overview and implementation summary
└── docs/                            # Optional project/testing documentation
```

---

## Local Setup

### Requirements

- **Java 17** recommended
- Git
- VS Code, IntelliJ IDEA, or another Java IDE
- macOS, Windows, or Linux

---

## Running the Project

From the repository root:

```bash
cd target_project
chmod +x gradlew
./gradlew desktop:build
./gradlew desktop:debug
```

### macOS Note

The desktop Gradle run configuration includes the macOS JVM requirement:

```text
-XstartOnFirstThread
```

Running through:

```bash
./gradlew desktop:debug
```

is the recommended way to launch the game on macOS.

---

# Quality Improvement 1: In-Game Screenshot Shortcut

## Overview

This improvement adds an in-game screenshot function triggered through a keyboard shortcut. It allows players and testers to capture the current game screen during gameplay or menu interaction without interrupting the game flow.

## ISO/IEC 25010 Quality Attributes

- **Usability**
- **Operability**
- **Functional suitability**

## Key Features

- Press **F12** to capture the current game scene
- On macOS laptops, use **fn + F12** when required
- Captures the active screen using LibGDX frame buffer utilities
- Corrects the vertical orientation of captured pixels
- Saves screenshots as timestamped `.png` files
- Uses background file writing to reduce interruption to the render loop

## Technical Implementation

### Input Handling

The screenshot shortcut is intercepted through the game’s input handling layer.  
The screenshot is triggered on key release so one press results in one capture.

### Capture Pipeline

1. Read the current game frame buffer.
2. Convert the pixel data into a correctly oriented image.
3. Pass the image to a background file-writing process.
4. Save the image as a timestamped PNG file.

## Main Files Modified

```text
target_project/SPD-classes/src/main/java/com/watabou/input/InputHandler.java
target_project/SPD-classes/src/main/java/com/watabou/utils/Screenshot.java
```

## Verification

The screenshot utility was manually verified by:

- Triggering a screenshot during active gameplay
- Triggering a screenshot from the menu/interface
- Confirming a `.png` file was generated
- Confirming repeated captures generate separate timestamped files
- Confirming gameplay remains responsive after screenshot capture

---

# Quality Improvement 2: Actor/Damage System Quality Refactoring

## Overview

This improvement targets the **Maintainability** and **Reliability** of the core combat logic (`Char.java` and related classes). It addresses scattered numeric literals, hardcoded type checks, and tight coupling in the damage calculation system, all of which violate fundamental software engineering principles.

## ISO/IEC 25010 Quality Attributes

- **Maintainability** (Modifiability, Readability, Modularity)
- **Reliability** (Maturity, Fault Tolerance)
- **Functional Suitability** (Functional Correctness)

## Technical Summary

1.  **Magic Number Extraction** — Replaced 24+ scattered numeric literals (e.g., `1.5f`, `0.67f`) with named `private static final` constants (e.g., `BERSERK_DAMAGE_MULTIPLIER`). This centralizes game balance tuning and significantly improves readability.

2.  **`DamageProperty` Enum System** - Introduced an extensible `DamageProperty` enum with pre-built `EnumSet` constants to replace hardcoded `instanceof` checks. Currently applied to `Hunger` and `Electricity` blobs, this system explicitly declares shield-bypass behavior at the call site, adhering to the **Open/Closed Principle (OCP)**.

3.  **`DamageCalculator` Utility** - Established a centralized utility class for property-checking logic (`bypassesShields`, `bypassesResistance`, etc.). This decouples `Char.java` from specific damage source classes, providing a modular, contract-based foundation for the damage system.

4.  **Defensive Guard Clauses** - Added input validation at the entry of `Char.damage(int, Object, Set<DamageProperty>)` to reject negative damage, null sources, and zero-damage no-ops. This prevents cascading `NullPointerException` failures and improves fault tolerance.

5.  **Architectural Decomposition** - Reduced tight coupling by separating state management, property contracts, and calculation rules into three focused classes (`Char`, `DamageCalculator`, `DamageProperty`). This decomposition adheres to the **Single Responsibility Principle (SRP)** and reduces the modification surface area of the main character class.

## Scope

The implementation includes targeted refactoring across the actor and damage calculation pipeline. These changes were made to improve correctness, maintainability, and extensibility without altering existing gameplay balance.

## Verification

Each change was verified by:

- Confirming `./gradlew :core:compileJava` passes cleanly after all modifications.
- Reviewing diff to ensure no gameplay logic was altered with only structural improvements.
- Confirming that the `DamageProperty` contract correctly replaces the previous `instanceof` checks for `Hunger` and `Electricity`
- Verifying that guard clauses prevent null-pointer failures at the method entry point
- Confirming that the refactored `Char.java` remains functionally identical to the original (no regression in damage values, multipliers, or buffs)

## Main Files Modified

```text
target_project/core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/Char.java
target_project/core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/DamageCalculator.java (new)
target_project/core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/DamageProperty.java (new)
target_project/core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/buffs/Hunger.java
target_project/core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/blobs/Electricity.java
```

---

# Quality Improvement 3: Crash-Safe Save Backup and Recovery

## Overview

This improvement strengthens the game’s save system by allowing recovery from **corrupted**, **missing**, or **unreadable** primary save files.

The system now creates `.bak` backup files during bundle-based save writes and automatically attempts recovery when a primary save cannot be loaded.

This protects player progress and allows interrupted or damaged save states to be recovered instead of becoming immediately unusable.

## ISO/IEC 25010 Quality Attributes

- **Reliability**
- **Fault tolerance**
- **Recoverability**

## Problem

A save game may become unusable if a primary save file is:

- Corrupted
- Missing
- Empty
- No longer readable due to malformed contents

Without recovery support, the save slot may fail to load or may appear unavailable.

## Solution

The implementation introduces automatic backup recovery through the save/load pipeline.

### Backup Creation

When bundle data is written, the system maintains a backup file using the `.bak` extension.

Examples:

```text
game.dat.bak
depth1.dat.bak
```

### Recovery Behaviour

When loading a save:

1. The game first attempts to read the primary file.
2. If the primary file is unreadable, corrupted, or missing:
   - the system attempts to load the corresponding `.bak` backup
3. If the backup is valid:
   - the primary file is restored from the backup
   - loading proceeds normally
4. If the backup is also unavailable or invalid:
   - the original error behaviour is preserved

### Supported Recovery Paths

The recovery system was wired into:

- Save-slot preview/loading
- Main game save loading
- Dungeon level save loading

This ensures recovery occurs not only when the player resumes a run, but also when the menu first checks whether a save slot is still valid.

## Main Files Modified

```text
target_project/SPD-classes/src/main/java/com/watabou/utils/FileUtils.java
target_project/core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Dungeon.java
target_project/core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/GamesInProgress.java
```

## Recovery Logs

When recovery occurs, the system prints trace messages such as:

```text
[SAVE RECOVERY] Primary file unreadable: game1/game.dat
[SAVE RECOVERY] Attempting backup recovery from: game1/game.dat.bak
[SAVE RECOVERY] Backup restored successfully: game1/game.dat
```

These logs support debugging, verification, and demonstration during testing.

## Verification and Test Evidence

### Test Case 1: Corrupted Main Save File

**Action:**  
Manually overwrite `game.dat` with invalid text.

**Expected Result:**  
The game restores `game.dat` from `game.dat.bak`.

**Result:**  
Passed. The file was restored and the save remained visible in the menu.

---

### Test Case 2: Corrupted Level Save File

**Action:**  
Manually overwrite `depth1.dat` with invalid text.

**Expected Result:**  
The game restores `depth1.dat` from `depth1.dat.bak`.

**Result:**  
Passed. The game successfully resumed the run and returned to the dungeon floor.

---

### Test Case 3: Missing Primary Save Files

**Action:**  
Delete:

```text
game.dat
depth1.dat
```

while keeping:

```text
game.dat.bak
depth1.dat.bak
```

**Expected Result:**  
The game restores both missing primary files from backups and resumes gameplay.

**Result:**  
Passed. Terminal logs confirmed both files were recovered:

```text
[SAVE RECOVERY] Primary file unreadable: game1/game.dat
[SAVE RECOVERY] Attempting backup recovery from: game1/game.dat.bak
[SAVE RECOVERY] Backup restored successfully: game1/game.dat

[SAVE RECOVERY] Primary file unreadable: game1/depth1.dat
[SAVE RECOVERY] Attempting backup recovery from: game1/depth1.dat.bak
[SAVE RECOVERY] Backup restored successfully: game1/depth1.dat
```

The game then resumed successfully:

```text
[GAME] @@ You return to floor 1 of the dungeon.
```

---

## Build Verification

After each implementation milestone, the desktop project was rebuilt using:

```bash
./gradlew desktop:build
```

All feature changes compiled successfully before further testing.

---

# Testing and SQA Approach

Our group uses a combination of:

- Manual functional testing
- Regression testing
- Fault-injection testing
- Edge-case verification
- Build verification through Gradle
- Pull Request review before integration

Each quality improvement is tested against its intended ISO/IEC 25010 quality attributes.

## Example SQA Methods Used

| Improvement          | Verification Method                                                                            |
| -------------------- | ---------------------------------------------------------------------------------------------- |
| Screenshot Shortcut  | Manual feature verification, repeated capture testing, regression check                        |
| Bug Fixes            | Issue reproduction, corrected behaviour verification, regression check                         |
| Save Backup Recovery | Fault injection, corrupted-file testing, missing-file recovery testing, load-flow verification |

---

# CI/CD and Build Validation

The repository uses a GitHub Actions build workflow to support continuous integration.

The CI process is intended to:

- Run automatically on Pull Requests into the integration branch
- Verify that the project compiles successfully
- Prevent unbuildable code from being merged without review

Local verification is also performed before submitting or merging feature branches:

```bash
cd target_project
./gradlew desktop:build
```

---

# Collaboration Workflow

## Branch Strategy

The repository uses `target-project` as the stable integration branch for the assignment.

Developers work on separate branches:

```text
feature/in-game-screenshot
feature/crash-safe-save-recovery
feature/...
fix/...
chore/...
```

## Pull Request Flow

1. A developer creates a dedicated feature branch.
2. The feature is implemented and built locally.
3. A Pull Request is opened into `target-project`.
4. The assigned tester reviews the feature through defined test cases.
5. The PR is reviewed by another team member.
6. Once verified, the PR is merged into `target-project`.

## Repository Hygiene

Generated files and local configuration files should not be committed.

Examples include:

```text
.gradle/
build/
bin/
.idea/
local.properties
.DS_Store
*.class
*.jar
*.bin
*.lock
```

These are excluded through `.gitignore` to keep Pull Requests clean and focused on source-code changes.

---

# Group Members

- **Karan Singh Biswakarma** — MPE Software Accelerated
- **Martina Therese Reyes** — Software Engineering Accelerated
- **Amogh Ranganatha Gowda** — MPE Software Accelerated
- **[Add Member Name]**
- **[Add Member Name]**

---

# Assignment Deliverables

The final submission will include:

- Modified Shattered Pixel Dungeon source code
- One video demonstration of implemented improvements and testing activities
- Optional presentation slides, if used in the final video

---

# Deadline

**Sunday, 24 May 2026 at 23:59**
