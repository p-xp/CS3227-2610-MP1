import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Scanner;

/**
 * The main entry point for the MeepMoop travel itinerary chatbot.
 */
public class MeepMoop {
    private static final String SEPARATOR = "____________________________________________________________";
    private static final Path DATA_FILE = Path.of("data", "meepmoop.txt");
    private static final DateTimeFormatter DISPLAY_DATE =
            DateTimeFormatter.ofPattern("d MMM uuuu", Locale.ENGLISH);

    /**
     * Starts the chatbot and processes itinerary commands until the user exits.
     *
     * @param args command-line arguments, which are not used by this application
     */
    public static void main(String[] args) {
        Storage storage = new Storage(DATA_FILE);
        Storage.LoadResult loadResult;
        try {
            loadResult = storage.load();
        } catch (IOException exception) {
            System.out.println("Unable to load saved data.");
            System.out.println(SEPARATOR);
            return;
        }

        Itinerary itinerary = loadResult.getItinerary();
        Parser parser = new Parser();
        Scanner scanner = new Scanner(System.in);
        boolean isRunning = true;

        System.out.println("Hello! I'm MeepMoop. How can I assist you today?");
        System.out.println(SEPARATOR);
        if (loadResult.hasCorruptedRecords()) {
            System.out.println("Warning: Some saved data could not be loaded.");
            System.out.println(SEPARATOR);
        }

        while (isRunning && scanner.hasNextLine()) {
            isRunning = handleCommand(scanner.nextLine(), itinerary, parser, storage);
        }

        System.out.println("Goodbye! Have a great day!");
        System.out.println(SEPARATOR);
    }

    /** Interprets and performs one command entered by the user. */
    static boolean handleCommand(String input, Itinerary itinerary, Parser parser, Storage storage) {
        try {
            Parser.ParsedCommand command = parser.parse(input);
            switch (command.getType()) {
            case ACTIVITY:
                addActivity(command.getDescription(), command.getDateTime(), itinerary, storage);
                break;
            case STAY:
                addAccommodation(command, itinerary, storage);
                break;
            case TRANSPORT:
                addTransport(command, itinerary, storage);
                break;
            case BOOK:
                updateBooking(command.getItemNumber(), itinerary, true, storage);
                break;
            case UNBOOK:
                updateBooking(command.getItemNumber(), itinerary, false, storage);
                break;
            case DELETE:
                deletePlan(command.getItemNumber(), itinerary, storage);
                break;
            case LIST:
                printList(itinerary);
                break;
            case VIEW:
                printView(command.getFrom(), itinerary);
                break;
            case EXIT:
                return false;
            }
        } catch (MeepException exception) {
            System.out.println(exception.getMessage());
            System.out.println(SEPARATOR);
        }
        return true;
    }

    /** Adds an activity when a non-empty description was supplied. */
    private static void addActivity(String description, java.time.LocalDateTime scheduledAt,
                                    Itinerary itinerary, Storage storage) {
        addPlan(new Activity(description, scheduledAt), itinerary, storage);
    }

    /** Adds an accommodation from validated parsed fields. */
    private static void addAccommodation(Parser.ParsedCommand command, Itinerary itinerary, Storage storage) {
        addPlan(new Accommodation(command.getDescription(), command.getFrom(), command.getTo()),
                itinerary, storage);
    }

    /** Adds transport from validated parsed fields. */
    private static void addTransport(Parser.ParsedCommand command, Itinerary itinerary, Storage storage) {
        addPlan(new Transport(command.getDescription(), command.getFromLocation(), command.getToLocation()),
                itinerary, storage);
    }

    /** Adds a plan and prints the standard type-specific confirmation. */
    private static void addPlan(Plan plan, Itinerary itinerary, Storage storage) {
        if (!itinerary.add(plan)) {
            System.out.println("Itinerary is full");
            System.out.println(SEPARATOR);
            return;
        }
        try {
            storage.save(itinerary);
        } catch (IOException exception) {
            itinerary.remove(itinerary.getCount());
            printSaveError();
            return;
        }
        System.out.println("Got it. I've added this " + plan.getType().getDisplayName() + ":");
        System.out.println(plan);
        System.out.println("Now you have " + itinerary.getCount() + " items in your itinerary.");
        System.out.println(SEPARATOR);
    }

    /** Prints every plan in the itinerary using its one-based list number. */
    private static void printList(Itinerary itinerary) {
        System.out.println("Here are the items in your itinerary:");
        for (int index = 0; index < itinerary.getCount(); index++) {
            System.out.println((index + 1) + ". " + itinerary.get(index + 1));
        }
        System.out.println(SEPARATOR);
    }

    /** Prints plans which occur on a requested date; any supplied view time is ignored. */
    private static void printView(java.time.LocalDate date, Itinerary itinerary) {
        System.out.println("Here are the items in your itinerary on "
                + date.format(DISPLAY_DATE) + ":");
        for (Plan plan : itinerary.getPlansOn(date)) {
            System.out.println(plan);
        }
        System.out.println(SEPARATOR);
    }

    /** Updates the booked state for the requested plan number. */
    private static void updateBooking(int planNumber, Itinerary itinerary, boolean shouldBook,
                                      Storage storage) throws MeepException {
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
                printSaveError();
                return;
            }
            System.out.println((shouldBook ? "Booked: " : "Unbooked: ") + plan);
            System.out.println(SEPARATOR);
        }
    }

    /** Removes the requested itinerary item and reports the updated item count. */
    private static void deletePlan(int planNumber, Itinerary itinerary, Storage storage) throws MeepException {
        Plan removedPlan = itinerary.remove(planNumber);
        if (removedPlan == null) {
            throw new MeepException("Invalid item number");
        }
        try {
            storage.save(itinerary);
        } catch (IOException exception) {
            itinerary.restore(planNumber, removedPlan);
            printSaveError();
            return;
        }
        System.out.println("Noted. I've removed this item:");
        System.out.println(removedPlan);
        System.out.println("Now you have " + itinerary.getCount() + " items in your itinerary.");
        System.out.println(SEPARATOR);
    }

    /** Prints the standard error used when a state change cannot be persisted. */
    private static void printSaveError() {
        System.out.println("Unable to save data.");
        System.out.println(SEPARATOR);
    }
}
