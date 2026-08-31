import java.util.Scanner;

/**
 * The main entry point for the MeepMoop travel itinerary chatbot.
 */
public class MeepMoop {
    private static final String SEPARATOR = "____________________________________________________________";

    /**
     * Starts the chatbot and processes itinerary commands until the user exits.
     *
     * @param args command-line arguments, which are not used by this application
     */
    public static void main(String[] args) {
        Itinerary itinerary = new Itinerary();
        Parser parser = new Parser();
        Scanner scanner = new Scanner(System.in);
        boolean isRunning = true;

        System.out.println("Hello! I'm MeepMoop. How can I assist you today?");
        System.out.println(SEPARATOR);

        while (isRunning && scanner.hasNextLine()) {
            isRunning = handleCommand(scanner.nextLine(), itinerary, parser);
        }

        System.out.println("Goodbye! Have a great day!");
        System.out.println(SEPARATOR);
    }

    /** Interprets and performs one command entered by the user. */
    private static boolean handleCommand(String input, Itinerary itinerary, Parser parser) {
        try {
            Parser.ParsedCommand command = parser.parse(input);
            switch (command.getType()) {
            case ACTIVITY:
                addActivity(command.getDescription(), itinerary);
                break;
            case STAY:
                addAccommodation(command, itinerary);
                break;
            case TRANSPORT:
                addTransport(command, itinerary);
                break;
            case BOOK:
                updateBooking(command.getItemNumber(), itinerary, true);
                break;
            case UNBOOK:
                updateBooking(command.getItemNumber(), itinerary, false);
                break;
            case DELETE:
                deletePlan(command.getItemNumber(), itinerary);
                break;
            case LIST:
                printList(itinerary);
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
    private static void addActivity(String description, Itinerary itinerary) {
        addPlan(new Activity(description), "activity", itinerary);
    }

    /** Adds an accommodation from validated parsed fields. */
    private static void addAccommodation(Parser.ParsedCommand command, Itinerary itinerary) {
        addPlan(new Accommodation(command.getDescription(), command.getFrom(), command.getTo()),
                "accommodation", itinerary);
    }

    /** Adds transport from validated parsed fields. */
    private static void addTransport(Parser.ParsedCommand command, Itinerary itinerary) {
        addPlan(new Transport(command.getDescription(), command.getFrom(), command.getTo()),
                "transport", itinerary);
    }

    /** Adds a plan and prints the standard type-specific confirmation. */
    private static void addPlan(Plan plan, String planType, Itinerary itinerary) {
        if (!itinerary.add(plan)) {
            System.out.println("Itinerary is full");
            System.out.println(SEPARATOR);
            return;
        }
        System.out.println("Got it. I've added this " + planType + ":");
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

    /** Updates the booked state for the requested plan number. */
    private static void updateBooking(int planNumber, Itinerary itinerary, boolean shouldBook) throws MeepException {
        Plan plan = itinerary.get(planNumber);
        if (plan == null) {
            throw new MeepException("Invalid item number");
        } else if (plan.isBooked() == shouldBook) {
            throw new MeepException("Item is already " + (shouldBook ? "booked" : "unbooked"));
        } else {
            plan.setBooked(shouldBook);
            System.out.println((shouldBook ? "Booked: " : "Unbooked: ") + plan);
            System.out.println(SEPARATOR);
        }
    }

    /** Removes the requested itinerary item and reports the updated item count. */
    private static void deletePlan(int planNumber, Itinerary itinerary) throws MeepException {
        Plan removedPlan = itinerary.remove(planNumber);
        if (removedPlan == null) {
            throw new MeepException("Invalid item number");
        }
        System.out.println("Noted. I've removed this item:");
        System.out.println(removedPlan);
        System.out.println("Now you have " + itinerary.getCount() + " items in your itinerary.");
        System.out.println(SEPARATOR);
    }
}
