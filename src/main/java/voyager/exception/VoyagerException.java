package voyager.exception;

/** Represents a user-facing error specific to the Voyager chatbot. */
public class VoyagerException extends Exception {
    private static final long serialVersionUID = 1L;

    /** Creates an exception with the message that should be shown to the user. */
    public VoyagerException(String message) {
        super(message);
    }
}
