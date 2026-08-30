import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The main entry point for the MeepMoop travel itinerary chatbot.
 */
public class MeepMoop {
    private static final String SEPARATOR = "____________________________________________________________";
    private static final Pattern THREE_PART_FORMAT =
            Pattern.compile("^(.+?)\\s+/from\\s+(.+?)\\s+/to\\s+(.+?)$", Pattern.CASE_INSENSITIVE);

    /**
     * Starts the chatbot and processes itinerary commands until the user exits.
     *
     * @param args command-line arguments, which are not used by this application
     */
    public static void main(String[] args) {
        Itinerary itinerary = new Itinerary();
        Scanner scanner = new Scanner(System.in);
        boolean isRunning = true;

        System.out.println("Hello! I'm MeepMoop. How can I assist you today?");
        System.out.println(SEPARATOR);

        while (isRunning && scanner.hasNextLine()) {
            isRunning = handleCommand(scanner.nextLine().trim(), itinerary);
        }

        System.out.println("Goodbye! Have a great day!");
        System.out.println(SEPARATOR);
    }

    /** Interprets and performs one command entered by the user. */
    private static boolean handleCommand(String input, Itinerary itinerary) {
        if (input.equalsIgnoreCase("exit")) {
            return false;
        }
        if (input.equalsIgnoreCase("list")) {
            printList(itinerary);
            return true;
        }

        String keyword = input.isEmpty() ? "" : input.split("\\s+", 2)[0].toLowerCase();
        String details = input.length() > keyword.length() ? input.substring(keyword.length()).trim() : "";
        switch (keyword) {
        case "activity":
            addActivity(details, itinerary);
            break;
        case "stay":
            addAccommodation(details, itinerary);
            break;
        case "transport":
            addTransport(details, itinerary);
            break;
        case "book":
            updateBooking(details, itinerary, true);
            break;
        case "unbook":
            updateBooking(details, itinerary, false);
            break;
        default:
            System.out.println("Invalid input");
            break;
        }
        return true;
    }

    /** Adds an activity when a non-empty description was supplied. */
    private static void addActivity(String description, Itinerary itinerary) {
        if (description.isEmpty()) {
            System.out.println("Invalid activity format. Use: activity <description>");
            return;
        }
        addPlan(new Activity(description), "activity", itinerary);
    }

    /** Adds an accommodation after extracting its name and date range. */
    private static void addAccommodation(String details, Itinerary itinerary) {
        String[] parts = extractThreeParts(details);
        if (parts == null) {
            System.out.println("Invalid stay format. Use: stay <name> /from <date> /to <date>");
            return;
        }
        addPlan(new Accommodation(parts[0], parts[1], parts[2]), "accommodation", itinerary);
    }

    /** Adds transport after extracting its description, origin, and destination. */
    private static void addTransport(String details, Itinerary itinerary) {
        String[] parts = extractThreeParts(details);
        if (parts == null) {
            System.out.println("Invalid transport format. Use: transport <name> /from <location> /to <location>");
            return;
        }
        addPlan(new Transport(parts[0], parts[1], parts[2]), "transport", itinerary);
    }

    /** Extracts text before /from, between the markers, and after /to. */
    private static String[] extractThreeParts(String details) {
        Matcher matcher = THREE_PART_FORMAT.matcher(details);
        if (!matcher.matches()) {
            return null;
        }
        return new String[] {
            matcher.group(1).trim(),
            matcher.group(2).trim(),
            matcher.group(3).trim()
        };
    }

    /** Adds a plan and prints the standard type-specific confirmation. */
    private static void addPlan(Plan plan, String planType, Itinerary itinerary) {
        if (!itinerary.add(plan)) {
            System.out.println("Itinerary is full");
            return;
        }
        System.out.println(SEPARATOR);
        System.out.println("Got it. I've added this " + planType + ":");
        System.out.println(plan);
        System.out.println("Now you have " + itinerary.getCount() + " items in your itinerary.");
        System.out.println(SEPARATOR);
    }

    /** Prints every plan in the itinerary using its one-based list number. */
    private static void printList(Itinerary itinerary) {
        System.out.println(SEPARATOR);
        System.out.println("Here are the items in your itinerary:");
        for (int index = 0; index < itinerary.getCount(); index++) {
            System.out.println((index + 1) + ". " + itinerary.get(index + 1));
        }
        System.out.println(SEPARATOR);
    }

    /** Updates the booked state for the requested plan number. */
    private static void updateBooking(String details, Itinerary itinerary, boolean shouldBook) {
        if (!details.matches("\\d+")) {
            System.out.println("Invalid item number");
            return;
        }
        try {
            int planNumber = Integer.parseInt(details);
            Plan plan = itinerary.get(planNumber);
            if (plan == null) {
                System.out.println("Invalid item number");
            } else if (plan.isBooked() == shouldBook) {
                System.out.println("Item is already " + (shouldBook ? "booked" : "unbooked"));
            } else {
                plan.setBooked(shouldBook);
                System.out.println((shouldBook ? "Booked: " : "Unbooked: ") + plan);
            }
        } catch (NumberFormatException exception) {
            System.out.println("Invalid item number");
        }
    }
}
