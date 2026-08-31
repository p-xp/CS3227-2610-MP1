import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * The main entry point for the MeepMoop travel itinerary chatbot.
 */
public class MeepMoop {
    private static final Path DATA_FILE = Path.of("data", "meepmoop.txt");

    /**
     * Starts the chatbot and processes itinerary commands until the user exits.
     *
     * @param args command-line arguments, which are not used by this application
     */
    public static void main(String[] args) {
        Ui ui = new Ui();
        Storage storage = new Storage(DATA_FILE);
        Storage.LoadResult loadResult;
        try {
            loadResult = storage.load();
        } catch (IOException exception) {
            ui.showLoadingError();
            return;
        }

        Itinerary itinerary = loadResult.getItinerary();
        Parser parser = new Parser();
        Scanner scanner = new Scanner(System.in);
        boolean isRunning = true;

        ui.showWelcome();
        if (loadResult.hasCorruptedRecords()) {
            ui.showCorruptedDataWarning();
        }

        while (isRunning && scanner.hasNextLine()) {
            isRunning = handleCommand(scanner.nextLine(), itinerary, parser, storage, ui);
        }

        ui.showGoodbye();
    }

    /** Interprets and performs one command entered by the user. */
    static boolean handleCommand(String input, Itinerary itinerary, Parser parser, Storage storage) {
        return handleCommand(input, itinerary, parser, storage, new Ui());
    }

    /** Interprets and performs one command using the supplied user interface. */
    static boolean handleCommand(String input, Itinerary itinerary, Parser parser, Storage storage, Ui ui) {
        try {
            Parser.ParsedCommand command = parser.parse(input);
            switch (command.getType()) {
            case ACTIVITY:
                addActivity(command.getDescription(), command.getDateTime(), itinerary, storage, ui);
                break;
            case STAY:
                addAccommodation(command, itinerary, storage, ui);
                break;
            case TRANSPORT:
                addTransport(command, itinerary, storage, ui);
                break;
            case BOOK:
                updateBooking(command.getItemNumber(), itinerary, true, storage, ui);
                break;
            case UNBOOK:
                updateBooking(command.getItemNumber(), itinerary, false, storage, ui);
                break;
            case DELETE:
                deletePlan(command.getItemNumber(), itinerary, storage, ui);
                break;
            case LIST:
                ui.showList(itinerary);
                break;
            case VIEW:
                ui.showPlansOn(command.getFrom(), itinerary);
                break;
            case EXIT:
                return false;
            }
        } catch (MeepException exception) {
            ui.showError(exception.getMessage());
        }
        return true;
    }

    /** Adds an activity when a non-empty description was supplied. */
    private static void addActivity(String description, java.time.LocalDateTime scheduledAt,
                                    Itinerary itinerary, Storage storage, Ui ui) {
        addPlan(new Activity(description, scheduledAt), itinerary, storage, ui);
    }

    /** Adds an accommodation from validated parsed fields. */
    private static void addAccommodation(Parser.ParsedCommand command, Itinerary itinerary, Storage storage, Ui ui) {
        addPlan(new Accommodation(command.getDescription(), command.getFrom(), command.getTo()),
                itinerary, storage, ui);
    }

    /** Adds transport from validated parsed fields. */
    private static void addTransport(Parser.ParsedCommand command, Itinerary itinerary, Storage storage, Ui ui) {
        addPlan(new Transport(command.getDescription(), command.getFromLocation(), command.getToLocation()),
                itinerary, storage, ui);
    }

    /** Adds a plan and prints the standard type-specific confirmation. */
    private static void addPlan(Plan plan, Itinerary itinerary, Storage storage, Ui ui) {
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
    private static void updateBooking(int planNumber, Itinerary itinerary, boolean shouldBook,
                                      Storage storage, Ui ui) throws MeepException {
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
    private static void deletePlan(int planNumber, Itinerary itinerary, Storage storage, Ui ui) throws MeepException {
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
