# FitNex

**A camera-driven push-up RPG for Android.** Your camera watches your form, every
clean rep damages the boss in front of you, and the reps you actually do level
your hero up.

FitNex is a native Android app written in Kotlin with Jetpack Compose. Push-ups
are detected on-device with ML Kit's accurate pose detector — no video, frames
or workout data are ever uploaded anywhere.

## Download

Grab the newest APK from the [Releases page](https://github.com/AceIsHopeless/FitNex/releases/latest).

| Release | APK |
| --- | --- |
| FitNEX 1.5 | `FitNEX-1.5.apk` |
| FitNEX 1.1 | `FitNEX-1.1.apk` |
| FitNEX 1.0 | `FitNEX-1.0.apk` |

Every version gets its own release and its own APK file named after the version,
so older builds stay downloadable when the app moves on. Local builds are also
archived in the `apk/` folder as `apk/FitNEX-<version>.apk`.

To install: download the `.apk` on your phone and open it. Android will ask you
to allow installs from your browser/file manager the first time — FitNex is
sideloaded, not distributed through Google Play.

## Features

- **Real push-up detection.** CameraX and ML Kit's accurate pose detector track
  your shoulders, elbows and wrists in real time and draw a live skeleton over
  the camera feed.
- **Rep counting that respects form.** An elbow-angle state machine ignores
  bouncing, half reps and jittery landmarks, so only completed reps land damage.
- **Five-boss campaign.** Slime (20 HP), Zombie (40 HP), Skeleton (60 HP),
  Vampire (80 HP) and the final Dark Knight (100 HP), each with its own time
  limit, art and lore.
- **Battle flow.** Briefing → live battle with health bars and hit feedback →
  results screen showing damage dealt, reps and rewards.
- **Hero ranks.** Climb from No Rank to C, B, A and finally S-Class as you level.
- **Titles.** Beat a boss for the first time to unlock its title, then equip it
  on your profile.
- **Stats that mean something.** Lifetime push-ups, battles won, bosses defeated,
  win rate and a seven-day rep chart.
- **Feel.** Hit sounds, victory and defeat stings, a tick and a haptic pulse for
  every counted rep — all toggleable.
- **Progress that sticks.** Level, XP, workout history and unlocked titles are
  persisted locally with DataStore.
- **Camera choice.** Front camera for a selfie-style set-up, back camera if you
  prefer to prop the phone up.
- **Safe reset.** Wiping progress requires typing `RESET` in the confirmation
  dialog.

## Screens

| Screen | What it does |
| --- | --- |
| Home | Hero overview, current rank and equipped title, next boss. |
| Briefing | Boss art, lore, HP and time limit before you commit to the fight. |
| Battle | Live camera, pose skeleton, rep counter, health bars, hit feedback. |
| Result | Win/lose summary with reps, damage and rewards. |
| Stats | Lifetime totals and the seven-day rep chart. |
| Profile | Rank, titles, camera facing, haptics/sound toggles, progress reset. |

## Requirements

- Android 8.0 (API 26) or newer on a 32-bit or 64-bit ARM device — one APK
  covers both.
- A working camera. A front camera is recommended but not required.
- No internet permission is requested, so the app works fully offline.

## Build from source

You need JDK 17 and the Android SDK with platform 36 installed.

```bash
# point Gradle at your local SDK (not committed)
echo "sdk.dir=/path/to/Android/Sdk" > local.properties

./gradlew assembleDebug   # APK: app/build/outputs/apk/debug/app-debug.apk
./gradlew test            # unit tests
```

The release workflow produces the downloadable builds: pushing a tag like `v1.0`
runs the tests, builds the debug APK, renames it to `FitNEX-1.0.apk` and attaches
it to a GitHub release named `FitNEX 1.0` with the matching
[CHANGELOG](CHANGELOG.md) section as the release notes.

## Releasing a new version

1. Bump `versionCode` and `versionName` in `app/build.gradle.kts`.
2. Move the `Unreleased` items in [CHANGELOG.md](CHANGELOG.md) into a new
   `## [X.Y]` section.
3. Build `./gradlew assembleRelease` and copy the APK to `apk/FitNEX-X.Y.apk`.
4. Commit, then tag and push:

   ```bash
   git tag v1.5
   git push origin main --tags
   ```

GitHub Actions builds it and publishes `FitNEX-1.5.apk` on a release titled
`FitNEX 1.5`. Each version is a separate release, so previous APKs remain
available.

## Tech stack

| Layer | Choice |
| --- | --- |
| Language | Kotlin 2.2.20 |
| UI | Jetpack Compose (Material 3), Compose BOM 2025.05.01 |
| Camera | CameraX 1.4.2 |
| Pose detection | ML Kit Pose Detection 18.0.0-beta5 (`pose-detection-accurate`, model bundled in the APK) |
| Persistence | DataStore Preferences + kotlinx-serialization-json |
| Build | AGP 8.13.0, Gradle 8.14.3, minSdk 26, compile/targetSdk 36 (release: universal 32/64-bit ARM APK) |

## Project structure

```text
app/src/main/java/com/fitnex/app/
├── MainActivity.kt              app entry point
├── camera/                      CameraX + ML Kit: pose detection, rep analysis
├── data/                        GameRepository, SettingsRepository (DataStore)
├── domain/                      Battle, Boss, PlayerState, PushUpCounter,
│                                PoseMath, Rank, Stats, Title — pure Kotlin
└── ui/                          Compose screens, ViewModel, theme, components
```

All game rules live in `domain/` as plain Kotlin, which is why they are covered
by fast JVM unit tests in `app/src/test/`.

## Tests

```bash
./gradlew test
```

Covers battle resolution, progression and leveling, pose math and the push-up
counter.

## Privacy

Camera frames are processed in memory on the device and discarded. FitNex has no
network permission, no accounts, no analytics and no ads. Everything you do
stays on your phone and is deleted when you clear the app's data.

## License

No license file is included yet, so all rights are reserved by the author.
