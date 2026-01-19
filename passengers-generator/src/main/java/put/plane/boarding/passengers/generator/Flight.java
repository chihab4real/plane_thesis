package put.plane.boarding.passengers.generator;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class Flight {
    // Getters
    private final String flightNumber;
    private final int planeRows;
    private final int planeColumns;
    private final int totalPassengers;
    private final int luggagePercentage;
    private final boolean doubleExit;
    private final List<Passenger> passengers;


}
