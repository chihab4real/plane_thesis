package put.plane.boarding.simulator.problem.factory.passenger;

import lombok.experimental.UtilityClass;
import put.plane.boarding.passengers.generator.Passenger;
import put.plane.boarding.simulator.passenger.SimulatorPassenger;
import put.plane.boarding.simulator.passenger.PassengerBuilder;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.structure.Seat;
import put.plane.boarding.simulator.passenger.impl.DefaultPassenger;
import put.plane.boarding.simulator.problem.PassengerGroup;

import java.util.Arrays;
import java.util.List;

/**
 * This class is used to map Passengers {@link Passenger} from passengers-generator
 * to {@link SimulatorPassenger} from simulator.
 */
@UtilityClass
public class PassengerFactory {

    /**
     * Maps {@link Passenger} to {@link SimulatorPassenger}, and then groups them using {@link PassengerGroup}
     * @param plane - plane for passengers {@link Plane}
     * @param passengerList - list of lists of passengers
     * @return list of {@link PassengerGroup}, made of passengers from respective lists
     */
    public static List<PassengerGroup> createSimulatorPassengers(Plane plane, List<List<Passenger>> passengerList) {
        return passengerList.stream()
                .map(passengers -> create(plane, passengers))
                .toList();
    }

    private static PassengerGroup create(Plane plane, List<Passenger> passengers) {
        List<SimulatorPassenger> simulatorPassengers = passengers
                .stream()
                .map(passenger -> create(plane, passenger))
                .toList();
        return new PassengerGroup(simulatorPassengers);
    }

    private static SimulatorPassenger create(Plane plane, Passenger passenger) {
        Seat seat = extractSeat(plane, passenger);
        DefaultPassenger defaultPassenger = new DefaultPassenger(seat, passenger.getSpeedQueue(), passenger.getSpeedExiting());
        PassengerBuilder result = new PassengerBuilder(defaultPassenger);
        if (passenger.isHasLuggage()) {
            result = result.withBaggage(extractBaggageLocation(passenger), 2);
        }
        return result.build();
    }

    private static Seat extractSeat(Plane plane, Passenger passenger) {
        String passengerLocation = passenger.getSeatLocation();
        List<Integer> positions = Arrays.stream(passengerLocation.split("_"))
                .map(Integer::parseInt)
                .toList();
        return new Seat(positions.get(0)-1, plane.getFiles().get(positions.get(1)-1));
    }

    private static int extractBaggageLocation(Passenger passenger) {
        String baggageLocation = passenger.getLuggageLocation();
        List<Integer> positions = Arrays.stream(baggageLocation.split("_"))
                .map(Integer::parseInt)
                .toList();
        return positions.get(0);
    }
}