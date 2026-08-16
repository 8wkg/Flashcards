# Flashcards

An Android app for learning foreign-language vocabulary — starting with Italian, built so any
language works the same way. Decks live entirely on the device (Room/SQLite), and new decks are
created by uploading a word list rather than typing cards in one at a time.

## Features

- **Decks** — each deck has a name and a language (e.g. "Italian Basics" / "Italian").
- **Import word lists** — pick a `.txt` or `.csv` file (Storage Access Framework), one word pair
  per line: `italian,english`, tab-separated also works, and an optional third column becomes a
  note. A header row like `word,translation` is detected and skipped. Import either creates a new
  deck or adds cards to an existing one.
- **Flip-card review** — tap a card to flip it from the Italian word to its translation.
- **Spaced repetition** — after revealing the answer, grade yourself Again / Hard / Good / Easy.
  A simplified SM-2 algorithm (like Anki's) schedules when each card comes back: new cards move
  through short learning steps (1 min, 10 min) before graduating to day-based intervals that grow
  or shrink based on how well you know the card. See
  `app/src/main/java/com/flashcards/vocab/srs/SpacedRepetitionScheduler.kt`.

A sample word list is included at `samples/italian_basics.csv` — import it to try the app out.

## Project structure

```
app/src/main/java/com/flashcards/vocab/
├── data/           Room entities, DAOs, database, repository, CSV/TXT importer
├── srs/            Spaced-repetition scheduling (Grade, SpacedRepetitionScheduler)
├── ui/
│   ├── decklist/   Deck list screen
│   ├── deckdetail/ Cards in a deck, due count, start review / add cards
│   ├── review/     Flip-card + grading review session
│   ├── importdeck/ File picker + import preview
│   └── theme/      Material 3 theme
├── AppContainer.kt, FlashcardsApp.kt, ViewModelFactory.kt   manual DI (no framework)
```

Everything is a single `:app` module, plain Kotlin + Jetpack Compose + Room — no backend, no
network permission requested.

## Building

Open the project root in **Android Studio** (Koala or newer) and let it sync, or from the command
line:

```
./gradlew assembleDebug
```

Requirements: JDK 17+, Android SDK with platform 34 and build-tools installed (Android Studio's
SDK Manager handles this automatically on sync).

> **Note:** this project was scaffolded in a sandboxed environment with no Android SDK and no
> network access to `dl.google.com`, so the code has been written carefully but could not be
> compiled or run here. Please build it locally and let me know if anything doesn't compile —
> happy to fix it.

## Import file format

```
word,translation
ciao,hello
grazie,thank you
casa,house,noun about "home"
```

- Delimiter can be a comma, tab, or semicolon.
- Blank lines and lines starting with `#` are ignored.
- A header row is auto-detected (e.g. `word,translation`) and skipped.
- The optional third column is stored as a note (not shown on the review screen, but visible if
  you inspect the database).
