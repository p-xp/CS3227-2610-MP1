# MeepMoop Travel Planner

MeepMoop is a JavaFX chatbot for planning and managing a travel itinerary.
Add activities, accommodation, and transport, then find, view, book, or remove
them through simple chat commands.

See the published [MeepMoop User Guide](https://p-xp.github.io/CS3227-2610-MP1/)
for command examples and feature details.

## Run the application

Prerequisite: JDK 25. The included Gradle Wrapper downloads the required Gradle
version automatically.

From the project root, start the graphical application with:

```bash
./gradlew --quiet run
```

Enter a command in the chat field, then press <kbd>Enter</kbd> or select
**Send**. For example:

```text
activity Visit the museum /at 2026-09-01 1800
stay City Hotel /from 2026-09-01 /to 2026-09-03
```

MeepMoop saves your itinerary automatically in `data/meepmoop.txt`.

## Run tests

Run the JUnit test suite from the project root:

```bash
./gradlew test
```

The HTML test report is generated at `build/reports/tests/test/index.html`.
