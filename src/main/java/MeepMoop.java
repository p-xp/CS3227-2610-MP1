import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * The main entry point for the MeepMoop travel itinerary chatbot.
 */
public class MeepMoop {
    private static final Path DATA_FILE = Path.of("data", "meepmoop.txt");
    private final Storage storage;
    private final Itinerary itinerary;
    private final Parser parser;
    private final Ui ui;
    private final boolean hasCorruptedRecords;

    /**
     * Creates the application and loads the itinerary stored at the supplied path.
     * If the saved data cannot be read, the application reports the problem and
     * does not start its command loop.
     */
    public MeepMoop(Path dataFile) {
        this.storage = new Storage(dataFile);
        this.parser = new Parser();
        this.ui = new Ui();

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
    MeepMoop(Itinerary itinerary, Parser parser, Storage storage, Ui ui) {
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
        new MeepMoop(DATA_FILE).run();
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
        try {
            Parser.ParsedCommand command = parser.parse(input);
            switch (command.getType()) {
            case ACTIVITY:
                return executeCommand(new ActivityCommand(command.getDescription(), command.getDateTime()));
            case STAY:
                addAccommodation(command);
                break;
            case TRANSPORT:
                addTransport(command);
                break;
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
            case EXIT:
                return executeCommand(new ExitCommand());
            }
        } catch (MeepException exception) {
            ui.showError(exception.getMessage());
        }
        return true;
    }

    /** Executes a command object and reports whether the command loop should continue. */
    private boolean executeCommand(Command command) throws MeepException {
        command.execute(itinerary, ui, storage);
        return !command.isExit();
    }

    /** Adds an accommodation from validated parsed fields. */
    private void addAccommodation(Parser.ParsedCommand command) {
        addPlan(new Accommodation(command.getDescription(), command.getFrom(), command.getTo()));
    }

    /** Adds transport from validated parsed fields. */
    private void addTransport(Parser.ParsedCommand command) {
        addPlan(new Transport(command.getDescription(), command.getFromLocation(), command.getToLocation()));
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
