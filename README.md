# Backgammon PlyGround for desktop

Play a world-class backgammon engine, review every game, and learn from every mistake. This is the desktop version of
[Backgammon PlyGround](https://github.com/BoldBlot/backgammon-plyground) for Android: a separate project that started as a copy
of the Android app and has its own desktop interface, in the style of eXtreme Gammon and GNU Backgammon.

**Status:** version 0.2.1, in development, on Linux (x86-64) only. For now you build it from
source. Windows and macOS may follow.

## What it does

- **Opens on the board**, with the new game options beside it. One main window holds play, review, position analysis and the quiz.
- **Play** PureTD, GNU Backgammon or Open Sage at a chosen Performance Rating, in money sessions or matches, with hints and
  feedback on your mistakes.
- **Review** every decision, yours and the computer's, with the engine's candidates, equities and chances.
- **Analyze any position**: set it up on the board, or paste an XGID or GNU ID.
- **Match Library**: import and export `.mat` files, for eXtreme Gammon and GNU Backgammon.
- **My positions**: your errors and saved positions in folders, with labels, notes and a quiz.
- **Statistics**: PR, error counts, rating, luck and dice.

`.mat` and position files are compatible with the Android app. See [CHANGELOG.md](CHANGELOG.md) for the changes in each version.

## Build and run

You need a JDK (tested with 25; the code targets Java 17), CMake, Ninja and a C/C++ compiler.

```
./gradlew run                     # builds the engines and the app, then opens it
./gradlew test --console=plain    # the JVM tests; those that need the engines are skipped without -PbgHostLib
```

Your games and settings are kept in `$XDG_DATA_HOME/BackgammonPlyGround`, or `~/.local/share/BackgammonPlyGround` if that isn't set.

## Credits

Backgammon PlyGround is made by [Bold Blot](https://boldblot.com). It stands on the work of these people and projects:

**Engines**

- **PureTD** by Alexander Strehl ([backgammon-ai-engine](https://github.com/alexstrehl/backgammon-ai-engine)): its neural networks
  and move generator, with Bold Blot's own search, bear-off database, cube handling and match equity table. MIT licence, see
  [`engine/puretd/LICENSE-PureTD`](engine/puretd/LICENSE-PureTD).
- **[GNU Backgammon](https://www.gnu.org/software/gnubg/)** by Gary Wong and the GNU Backgammon authors, among them Joseph Heled,
  Oystein Johansen, Jonathan Kinsey, David Montgomery, Jim Segrave, Joern Thyssen, Christian Anthon, Michael Petch and Philippe
  Michel: its evaluation code, neural networks, bear-off database and cubeful evaluation, and the Kazaross XG2 match equity table
  distributed with it. GPL v3 or later, see [`engine/gnubg/COPYING`](engine/gnubg/COPYING) and
  [`engine/gnubg/AUTHORS`](engine/gnubg/AUTHORS).
- **Open Sage** by Mark Higgins ([bgsage](https://github.com/markbgsage/bgsage)): its code, production networks, bear-off database,
  cubeful search and match equity table. Mozilla Public License 2.0. One file is changed for the desktop build, as noted at the end
  of [`engine/sage/LICENSE-OpenSage`](engine/sage/LICENSE-OpenSage).

**Definitions**

- Performance Rating and the error and blunder categories follow the definitions published in the eXtreme Gammon 2 documentation.
  eXtreme Gammon is a trademark of GameSite 2000 Ltd.; this project is not affiliated with it.
- The rating uses the FIBS formula.

**Typeface and libraries**

- The [Archivo](https://github.com/Omnibus-Type/Archivo) typeface by The Archivo Project Authors, SIL Open Font License 1.1, see
  [`engine-data/licenses/archivo-OFL.txt`](engine-data/licenses/archivo-OFL.txt).
- Kotlin, Compose Multiplatform, kotlinx.coroutines and kotlinx.serialization by JetBrains and their contributors; Room, DataStore,
  Navigation and Lifecycle from AndroidX by Google and the Android Open Source Project (Apache License 2.0); and SQLite (public
  domain). Gradle downloads these when it builds.

## Licence

Free software under the GNU General Public License, version 3 or later: see [LICENSE](LICENSE). The parts above keep their own
licences.
