import org.junit.jupiter.api.Test;
import put.plane.boarding.passengers.generator.Passenger;
import put.plane.boarding.passengers.generator.PassengerGenerator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PassengerGeneratorTest {

    @Test
    public void testGeneratePassengers(){
        int rowsPlane = 10;
        int columnsPlane = 2;
        int totalPassengers = 20;
        int luggagePassengersPercentage = 20;

        List<Passenger> passengers = PassengerGenerator.generatePassengers(
                rowsPlane,
                columnsPlane,
                totalPassengers,
                luggagePassengersPercentage,
                0);

        assertEquals(totalPassengers, passengers.size(), "Incorrect number of passengers generated");


        long uniqueSeats = passengers.stream()
                .map(Passenger::getSeatLocation)
                .distinct()
                .count();
        assertEquals(totalPassengers, uniqueSeats, "Seat locations are not unique");


        for (Passenger p : passengers) {
            assertTrue(p.getSpeedQueue() >= 1 && p.getSpeedQueue() <= 10, "Invalid queue speed");
            assertTrue(p.getSpeedExiting() >= 1 && p.getSpeedExiting() <= 10, "Invalid exiting speed");

            if (p.isHasLuggage()) {
                assertNotNull(p.getLuggageLocation(), "Passenger with luggage has null luggage location");
            } else {
                assertNull(p.getLuggageLocation(), "Passenger without luggage has non-null luggage location");
            }
        }
    }



}
