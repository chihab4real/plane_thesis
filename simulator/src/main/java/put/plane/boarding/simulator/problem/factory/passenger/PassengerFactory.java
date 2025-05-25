package put.plane.boarding.simulator.problem.factory.passenger;

import org.springframework.stereotype.Service;
import put.plane.boarding.passengers.generator.Passenger;
import put.plane.boarding.simulator.passenger.SimulatorPassenger;
import put.plane.boarding.simulator.passenger.PassengerBuilder;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.structure.Seat;
import put.plane.boarding.simulator.passenger.impl.DefaultPassenger;

import java.util.Arrays;
import java.util.List;

@Service
public class PassengerFactory {

    public static List<SimulatorPassenger> createSimulatorPassenger(Plane plane, List<Passenger> passengers) {
        return passengers.stream()
                .map(passenger -> create(plane, passenger))
                .toList();
    }

    private static SimulatorPassenger create(Plane plane, Passenger passenger) {

        var seat = extractSeat(plane, passenger);

        var defaultPassenger = new DefaultPassenger(seat, passenger.getSpeedQueue(), passenger.getSpeedExiting());
        var result = new PassengerBuilder(defaultPassenger);
        if (passenger.isHasLuggage()) {
            result = result.withBaggage(extractBaggageLocation(passenger), 2);
        }
        return result.build();
    }

    private static Seat extractSeat(Plane plane, Passenger passenger) {
        var passengerLocation = passenger.getSeatLocation();
        var positions = Arrays.stream(passengerLocation.split("_"))
                .map(Integer::parseInt)
                .toList();
        return new Seat(positions.get(0)-1, plane.getFiles().get(positions.get(1)-1));
    }

    private static int extractBaggageLocation(Passenger passenger) {
        var baggageLocation = passenger.getLuggageLocation();
        var positions = Arrays.stream(baggageLocation.split("_"))
                .map(Integer::parseInt)
                .toList();
        return positions.getFirst();
    }
}