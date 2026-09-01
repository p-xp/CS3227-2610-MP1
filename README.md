# MeepMoop project

This is a greenfield Java project for the MeepMoop chatbot. Given below are instructions on how to use it.

## Running from the terminal

Prerequisite: JDK 25. The Gradle Wrapper included in this repository downloads
the required Gradle version automatically, so a separate Gradle installation
is not needed.

From the project root, compile the Java source files and start the chatbot with:

```bash
./gradlew classes
./gradlew --quiet run
```

Gradle writes compiled files under `build/`, keeping generated `.class` files
separate from the Java source files.

## Running automated tests

Run the JUnit test suite from the project root with:

```bash
./gradlew test
```

JUnit test classes follow Gradle conventions and are stored under
`src/test/java`. The HTML test report is generated at
`build/reports/tests/test/index.html`.

Enter commands at the prompt, such as `activity Museum /at 2026-09-01 1800`,
`stay Hotel /from 2026-09-01 /to 2026-09-03`, `view 2026-09-01`, `find book`, `list`, or
`delete 1`. Activity date-times use `YYYY-MM-DD HHmm`; displays use a readable
12-hour format. `view` accepts a date and ignores an optional supplied time.
Enter `exit` to quit. To provide commands non-interactively, pipe them into the program:

```bash
printf '%s\n' 'activity Museum' 'list' 'exit' | ./gradlew --quiet run
```

## Setting up in Intellij

Prerequisites: JDK 25, update Intellij to the most recent version.

1. Open Intellij (if you are not in the welcome screen, click `File` > `Close Project` to close the existing project first)
1. Open the project into Intellij as follows:
   1. Click `Open`.
   1. Select the project directory, and click `OK`.
   1. If there are any further prompts, accept the defaults.
1. Configure the project to use **JDK 25** (not other versions) as explained in [here](https://www.jetbrains.com/help/idea/sdk.html#set-up-jdk).<br>
   In the same dialog, set the **Project language level** field to the `SDK default` option.
1. After that, locate the `src/main/java/MeepMoop.java` file, right-click it, and choose `Run MeepMoop.main()` (if the code editor is showing compile errors, try restarting the IDE). If the setup is correct, you should see `MeepMoop` as the output.
   ```
    ____        _        
   |  _ \ _   _| | _____ 
   | | | | | | | |/ / _ \
   | |_| | |_| |   <  __/
   |____/ \__,_|_|\_\___|
   ```

**Warning:** Keep the `src\main\java` folder as the root folder for Java files (i.e., don't rename those folders or move Java files to another folder outside of this folder path), as this is the default location some tools (e.g., Gradle) expect to find Java files.
