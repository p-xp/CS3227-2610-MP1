package voyager.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Tests propagation of user-facing messages through {@link VoyagerException}. */
class VoyagerExceptionTest {
    @Test
    void constructor_messageProvided_preservesMessage() {
        VoyagerException exception = new VoyagerException("Invalid input");

        assertEquals("Invalid input", exception.getMessage());
    }
}
