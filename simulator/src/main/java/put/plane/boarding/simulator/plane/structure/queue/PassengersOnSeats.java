package put.plane.boarding.simulator.plane.structure.queue;

import lombok.ToString;
import put.plane.boarding.simulator.passenger.SimulatorPassenger;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ToString
public class PassengersOnSeats {
    private Map<Integer, Set<SimulatorPassenger>> passengersOnSeatsByRows;

    public void boardPassengers(List<SimulatorPassenger> passengers) {
        passengersOnSeatsByRows = passengers.stream()
                .collect(Collectors.groupingBy(
                        passenger -> passenger.seat().row(),
                        Collectors.toSet()
                ));
    }

    public void onPassengerOffSeat(SimulatorPassenger passenger) {
        passengersOnSeatsByRows
                .get(passenger.seat().row())
                .remove(passenger);
    }

    public boolean isPassengerInFrontSeat(SimulatorPassenger passenger) {
        List<SimulatorPassenger> passengersInFront = passengersOnSeatsByRows.get(passenger.seat().row())
                .stream()
                .filter(p -> p.seat().file().distance() < passenger.seat().file().distance())
                .toList();
        return passengersInFront.isEmpty();
    }
}
