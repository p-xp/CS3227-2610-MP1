import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Tests the console display representation of a transport arrangement. */
class TransportTest {
    @Test
    void toString_unbookedTransport_returnsLocationsAndUnbookedMarker() {
        Transport transport = new Transport("Flight", "Singapore", "Tokyo");

        assertEquals("[T] [ ] Flight (from: Singapore to: Tokyo)", transport.toString());
    }

    @Test
    void toString_bookedTransport_returnsLocationsAndBookedMarker() {
        Transport transport = new Transport("Flight", "Singapore", "Tokyo");
        transport.setBooked(true);

        assertEquals("[T] [X] Flight (from: Singapore to: Tokyo)", transport.toString());
    }
}
