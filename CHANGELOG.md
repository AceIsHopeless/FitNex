# Changelog

All notable changes to FitNex are documented here.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0] - 2026-11-25

First public release.

### Added

- Real-time push-up detection with the front or back camera using on-device
  MediaPipe pose tracking — no video ever leaves the phone.
- Rep counting with elbow-angle validation, so partial or bouncing reps are not
  counted as clean push-ups.
- Boss campaign with five encounters of rising difficulty: Slime (20 HP),
  Zombie (40 HP), Skeleton (60 HP), Vampire (80 HP) and the final Dark Knight
  (100 HP), each with its own time limit and lore.
- Battle camera view with live pose skeleton, health bars, hit feedback and a
  briefing screen before every fight.
- Hero ranks unlocked by level: No Rank, C-Class, B-Class, A-Class and S-Class.
- Five equippable titles unlocked by defeating each boss for the first time
  (Slime Slayer, Zombie Breaker, Skeleton Crusher, Vampire Hunter,
  Dark Knight's Bane).
- Stats screen with lifetime push-ups, battles won, bosses defeated, win rate
  and a seven-day rep chart.
- Sound effects (rep tick, hit, victory, defeat), haptic feedback per counted
  rep, and toggles for both in the profile.
- Progress persistence with DataStore: level, XP, history, titles and history
  survive app restarts.
- Profile settings for camera facing (front/back) and a guarded progress reset
  that requires typing `RESET`.
- Unit tests for battle math, progression, pose math and the push-up counter.
