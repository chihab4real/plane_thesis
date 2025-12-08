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
        if (passengersOnSeatsByRows == null) {
            passengersOnSeatsByRows = passengers.stream()
                    .collect(Collectors.groupingBy(
                            passenger -> passenger.seat().row(),
                            Collectors.toSet()
                    ));
        } else {
            // Add to existing map instead of replacing it
            passengers.forEach(passenger -> {
                passengersOnSeatsByRows
                        .computeIfAbsent(passenger.seat().row(), k -> new java.util.HashSet<>())
                        .add(passenger);
            });
        }
    }

    public void onPassengerOffSeat(SimulatorPassenger passenger) {
        Set<SimulatorPassenger> rowPassengers = passengersOnSeatsByRows.get(passenger.seat().row());
        if (rowPassengers != null) {
            rowPassengers.remove(passenger);
        }
    }

    public boolean isPassengerInFrontSeat(SimulatorPassenger passenger) {
        Set<SimulatorPassenger> rowPassengers = passengersOnSeatsByRows.get(passenger.seat().row());
        if (rowPassengers == null || rowPassengers.isEmpty()) {
            return true; // No passengers in this row, so passenger is in front
        }

        List<SimulatorPassenger> passengersInFront = rowPassengers.stream()
                .filter(p -> p.seat().file().distance() < passenger.seat().file().distance())
                .toList();
        return passengersInFront.isEmpty();
    }
}
