package put.plane.boarding.simulator.plane.structure.queue;

import put.plane.boarding.simulator.passenger.PassengerDecorator;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SeatQueue {
    private Map<Integer, Set<PassengerDecorator>> passengersOnSeatsByRows;

    public void boardPassengers(List<PassengerDecorator> passengers) {
        passengersOnSeatsByRows = passengers.stream()
                .collect(Collectors.groupingBy(
                        passenger -> passenger.toSeat().row(),
                        Collectors.toSet()
                ));
    }

    public void onPassengerOffSeat(PassengerDecorator passenger) {
        passengersOnSeatsByRows
                .get(passenger.toSeat().row())
                .remove(passenger);
    }

    public boolean isPassengerInFrontOfQueue(PassengerDecorator passenger) {
        var passengersInFront = passengersOnSeatsByRows.get(passenger.toSeat().row())
                .stream()
                .filter(p -> p.toSeat().file().distance() < passenger.toSeat().file().distance())
                .toList();
        return passengersInFront.isEmpty();
    }
}
