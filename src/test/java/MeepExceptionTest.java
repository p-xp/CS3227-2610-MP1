import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Tests propagation of user-facing messages through {@link MeepException}. */
class MeepExceptionTest {
    @Test
    void constructor_messageProvided_preservesMessage() {
        MeepException exception = new MeepException("Invalid input");

        assertEquals("Invalid input", exception.getMessage());
    }
}
