package put.plane.boarding.simulator.plane;

import lombok.Builder;
import lombok.Data;
import put.plane.boarding.simulator.passenger.SimulatorPassenger;
import put.plane.boarding.simulator.plane.structure.File;
import put.plane.boarding.simulator.plane.structure.queue.PassengersOnSeats;
import put.plane.boarding.simulator.plane.structure.queue.Queue;

import java.util.List;

@Data
@Builder
public class Plane {
    private final int rows;
    private final Queue queue;
    private final PassengersOnSeats passengersOnSeats;
    private final List<File> files;

    public int getColumns() {
        return files.size();
    }

    public void boardPassengers(List<SimulatorPassenger> passengers) {
        passengersOnSeats.boardPassengers(passengers);
    }
}
