package put.plane.boarding.simulator.plane.structure.queue;

import put.plane.boarding.simulator.passenger.SimulatorPassenger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PassengersOnSeats {
    private final Map<Integer, Set<SimulatorPassenger>> passengersOnSeatsByRows = new HashMap<>();

    public void boardPassengers(List<SimulatorPassenger> passengers) {
        passengers.forEach(passenger -> passengersOnSeatsByRows
                .computeIfAbsent(passenger.seat().row(), k -> new java.util.HashSet<>())
                .add(passenger));
    }

    public void onPassengerOffSeat(SimulatorPassenger passenger) {
        passengersOnSeatsByRows
                .get(passenger.seat().row())
                .removeIf(p -> p.rootPassenger().equals(passenger));
    }

    public boolean isPassengerInFrontSeat(SimulatorPassenger passenger) {
        List<SimulatorPassenger> passengersInFront = getPassengersInFront(passenger);
        return passengersInFront.isEmpty();
    }

    public boolean isPassengerStuckInSeat(SimulatorPassenger passenger) {
        List<SimulatorPassenger> passengersInFront = getPassengersInFront(passenger);
        if (passengersInFront.isEmpty()) {
            return false;
        }
        int smallestGroupInFront = passengersInFront
                .stream()
                .mapToInt(SimulatorPassenger::toGroup)
                .min().orElseThrow();
        return smallestGroupInFront > passenger.toGroup();
    }

    private List<SimulatorPassenger> getPassengersInFront(SimulatorPassenger passenger) {
        return passengersOnSeatsByRows.get(passenger.seat().row())
                .stream()
                .filter(p -> p.seat().file().distance() < passenger.seat().file().distance()
                        && p.seat().file().side() == passenger.seat().file().side())
                .toList();
    }
}
