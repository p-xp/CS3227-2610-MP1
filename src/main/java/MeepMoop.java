/**
 * The main entry point for the MeepMoop chatbot.
 */
public class MeepMoop {
    public static void main(String[] args) {
        String separator = "____________________________________________________________";

        String banner = " __  __                    __  __                  \n"
                + "|  \\/  | ___  ___ _ __    |  \\/  | ___   ___  _ __ \n"
                + "| |\\/| |/ _ \\/ _ \\ '_ \\   | |\\/| |/ _ \\ / _ \\| '_ \\\n"
                + "| |  | |  __/  __/ |_) |  | |  | | (_) | (_) | |_) |\n"
                + "|_|  |_|\\___|\\___| .__/   |_|  |_|\\___/ \\___/| .__/\n"
                + "                 |_|                          |_|  \n"
                + "              ~ Welcome to MeepMoop! ~\n";
        
        String welcomeMessage = "Hello! I'm MeepMoop. How can I assist you today?";
        String goodbyeMessage = "Goodbye! Have a great day!";
        
        System.out.println(banner);
        System.out.println(separator);
        System.out.println(welcomeMessage);
        System.out.println(separator);
        System.out.println(goodbyeMessage);
        System.out.println(separator);

    }
}
