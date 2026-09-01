package voyager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

import voyager.command.ActivityCommand;
import voyager.command.BookingCommand;
import voyager.command.Command;
import voyager.command.DeleteCommand;
import voyager.command.ExitCommand;
import voyager.command.FindCommand;
import voyager.command.ListCommand;
import voyager.command.StayCommand;
import voyager.command.TransportCommand;
import voyager.command.ViewCommand;
import voyager.exception.VoyagerException;
import voyager.model.Itinerary;
import voyager.model.Plan;
import voyager.parser.Parser;
import voyager.storage.Storage;
import voyager.ui.Ui;

/**
 * The main entry point for the Voyager travel itinerary chatbot.
 */
public class Voyager {
    /** The default location used to persist the itinerary. */
    public static final Path DEFAULT_DATA_FILE = Path.of("data", "voyager.txt");
    private final Storage storage;
    private final Itinerary itinerary;
    private final Parser parser;
    private final Ui ui;
    private final boolean hasCorruptedRecords;
    private boolean wasLastCommandValid = true;

    /**
     * Creates the application and loads the itinerary stored at the supplied path.
     * If the saved data cannot be read, the application reports the problem and
     * does not start its command loop.
     */
    public Voyager(Path dataFile) {
        this(dataFile, new Ui());
    }

    /**
     * Creates the application using a supplied interface to display its responses.
     *
     * @param dataFile the file used to persist the itinerary
     * @param ui the user interface that displays application responses
     */
    public Voyager(Path dataFile, Ui ui) {
        this.storage = new Storage(dataFile);
        this.parser = new Parser();
        this.ui = ui;

        Itinerary loadedItinerary = null;
        boolean loadedCorruptedRecords = false;
        try {
            Storage.LoadResult loadResult = storage.load();
            loadedItinerary = loadResult.getItinerary();
            loadedCorruptedRecords = loadResult.hasCorruptedRecords();
        } catch (IOException exception) {
            ui.showLoadingError();
        }
        this.itinerary = loadedItinerary;
        this.hasCorruptedRecords = loadedCorruptedRecords;
    }

    /** Creates an application with supplied collaborators for command-level tests. */
    Voyager(Itinerary itinerary, Parser parser, Storage storage, Ui ui) {
        this.itinerary = itinerary;
        this.parser = parser;
        this.storage = storage;
        this.ui = ui;
        this.hasCorruptedRecords = false;
    }

    /**
     * Starts the chatbot and processes itinerary commands until the user exits.
     *
     * @param args command-line arguments, which are not used by this application
     */
    public static void main(String[] args) {
        new Voyager(DEFAULT_DATA_FILE).run();
    }

    /** Starts the user interface and processes commands until the user exits. */
    public void run() {
        if (itinerary == null) {
            return;
        }

        ui.showWelcome();
        if (hasCorruptedRecords) {
            ui.showCorruptedDataWarning();
        }

        Scanner scanner = new Scanner(System.in);
        boolean isRunning = true;
        while (isRunning && scanner.hasNextLine()) {
            isRunning = handleCommand(scanner.nextLine());
        }

        ui.showGoodbye();
    }

    /** Interprets and performs one command entered by the user. */
    boolean handleCommand(String input) {
        ui.clearResponseStatus();
        wasLastCommandValid = true;
        try {
            Parser.ParsedCommand command = parser.parse(input);
            switch (command.getType()) {
                case ACTIVITY:
                    return executeCommand(new ActivityCommand(command.getDescription(), command.getDateTime()));
                case STAY:
                    return executeCommand(new StayCommand(command.getDescription(), command.getFrom(),
                            command.getTo()));
                case TRANSPORT:
                    return executeCommand(new TransportCommand(command.getDescription(),
                            command.getFromLocation(), command.getToLocation()));
                case BOOK:
                    return executeCommand(new BookingCommand(command.getItemNumber(), true));
                case UNBOOK:
                    return executeCommand(new BookingCommand(command.getItemNumber(), false));
                case DELETE:
                    return executeCommand(new DeleteCommand(command.getItemNumber()));
                case LIST:
                    return executeCommand(new ListCommand());
                case VIEW:
                    return executeCommand(new ViewCommand(command.getFrom()));
                case FIND:
                    return executeCommand(new FindCommand(command.getDescription()));
                case EXIT:
                    return executeCommand(new ExitCommand());
                default:
                    throw new AssertionError("unsupported command type");
            }
        } catch (VoyagerException exception) {
            ui.showError(exception.getMessage());
        }
        wasLastCommandValid = !ui.isLastResponseError();
        return true;
    }

    /** Returns whether the most recently processed command completed without an error. */
    boolean wasLastCommandValid() {
        return wasLastCommandValid;
    }

    /** Returns the itinerary currently managed by this application. */
    Itinerary getItinerary() {
        return itinerary;
    }

    /** Executes a command object and reports whether the command loop should continue. */
    private boolean executeCommand(Command command) throws VoyagerException {
        command.execute(itinerary, ui, storage);
        wasLastCommandValid = !ui.isLastResponseError();
        return !command.isExit();
    }

    /** Adds a plan and prints the standard type-specific confirmation. */
    private void addPlan(Plan plan) {
        if (!itinerary.add(plan)) {
            ui.showItineraryFull();
            return;
        }
        try {
            storage.save(itinerary);
        } catch (IOException exception) {
            itinerary.remove(itinerary.getCount());
            ui.showSaveError();
            return;
        }
        ui.showPlanAdded(plan, itinerary.getCount());
    }

}
