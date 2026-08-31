/** Represents a user-facing error specific to the MeepMoop chatbot. */
public class MeepException extends Exception {
    private static final long serialVersionUID = 1L;

    /** Creates an exception with the message that should be shown to the user. */
    public MeepException(String message) {
        super(message);
    }
}
