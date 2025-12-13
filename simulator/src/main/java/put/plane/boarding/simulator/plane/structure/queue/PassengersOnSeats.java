package put.plane.boarding.simulator.plane.structure.queue;

import lombok.ToString;
import put.plane.boarding.simulator.passenger.SimulatorPassenger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ToString
public class PassengersOnSeats {
    private final Map<Integer, Set<SimulatorPassenger>> passengersOnSeatsByRows = new HashMap<>();

    public void boardPassengers(List<SimulatorPassenger> passengers) {
        passengers.forEach(passenger -> passengersOnSeatsByRows
                .computeIfAbsent(passenger.seat().row(), k -> new java.util.HashSet<>())
                .add(passenger));
    }

    public void onPassengerOffSeat(SimulatorPassenger passenger) {
        Set<SimulatorPassenger> rowPassengers = passengersOnSeatsByRows.get(passenger.seat().row());
        rowPassengers.remove(passenger);
    }

    public boolean isPassengerInFrontSeat(SimulatorPassenger passenger) {
        List<SimulatorPassenger> passengersInFront = passengersOnSeatsByRows.get(passenger.seat().row())
                .stream()
                .filter(p -> p.seat().file().distance() < passenger.seat().file().distance())
                .toList();
        return passengersInFront.isEmpty();
    }
}
