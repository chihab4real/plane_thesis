package put.plane.boarding.simulator.plane.structure.queue;

import put.plane.boarding.simulator.passenger.Passenger;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SeatQueue {
    private Map<Integer, Set<Passenger>> passengersOnSeatsByRows;

    public void boardPassengers(List<Passenger> passengers) {
        passengersOnSeatsByRows = passengers.stream()
                .collect(Collectors.groupingBy(
                        passenger -> passenger.toSeat().row(),
                        Collectors.toSet()
                ));
    }

    public void onPassengerOffSeat(Passenger passenger) {
        passengersOnSeatsByRows
                .get(passenger.toSeat().row())
                .remove(passenger);
    }

    public boolean isPassengerInFrontOfQueue(Passenger passenger) {
        var passengersInFront = passengersOnSeatsByRows.get(passenger.toSeat().row())
                .stream()
                .filter(p -> p.toSeat().file().distance() < passenger.toSeat().file().distance())
                .toList();
        return passengersInFront.isEmpty();
    }
}
