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
                addActivity(command.getDescription(), command.getDateTime());
                break;
            case STAY:
                addAccommodation(command);
                break;
            case TRANSPORT:
                addTransport(command);
                break;
            case BOOK:
                updateBooking(command.getItemNumber(), true);
                break;
            case UNBOOK:
                updateBooking(command.getItemNumber(), false);
                break;
            case DELETE:
                deletePlan(command.getItemNumber());
                break;
            case LIST:
                return executeCommand(new ListCommand());
            case VIEW:
                ui.showPlansOn(command.getFrom(), itinerary);
                break;
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

    /** Adds an activity when a non-empty description was supplied. */
    private void addActivity(String description, java.time.LocalDateTime scheduledAt) {
        addPlan(new Activity(description, scheduledAt));
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

    /** Updates the booked state for the requested plan number. */
    private void updateBooking(int planNumber, boolean shouldBook) throws MeepException {
        Plan plan = itinerary.get(planNumber);
        if (plan == null) {
            throw new MeepException("Invalid item number");
        } else if (plan.isBooked() == shouldBook) {
            throw new MeepException("Item is already " + (shouldBook ? "booked" : "unbooked"));
        } else {
            plan.setBooked(shouldBook);
            try {
                storage.save(itinerary);
            } catch (IOException exception) {
                plan.setBooked(!shouldBook);
                ui.showSaveError();
                return;
            }
            ui.showBookingUpdated(plan, shouldBook);
        }
    }

    /** Removes the requested itinerary item and reports the updated item count. */
    private void deletePlan(int planNumber) throws MeepException {
        Plan removedPlan = itinerary.remove(planNumber);
        if (removedPlan == null) {
            throw new MeepException("Invalid item number");
        }
        try {
            storage.save(itinerary);
        } catch (IOException exception) {
            itinerary.restore(planNumber, removedPlan);
            ui.showSaveError();
            return;
        }
        ui.showPlanDeleted(removedPlan, itinerary.getCount());
    }
}
