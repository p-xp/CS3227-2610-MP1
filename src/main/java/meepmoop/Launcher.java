package meepmoop;

import javafx.application.Application;

/**
 * Launches the JavaFX application separately to avoid JavaFX classpath issues.
 */
public final class Launcher {
    private Launcher() {
    }

    /** Launches the graphical MeepMoop application. */
    public static void main(String[] args) {
        Application.launch(MainWindow.class, args);
    }
}
