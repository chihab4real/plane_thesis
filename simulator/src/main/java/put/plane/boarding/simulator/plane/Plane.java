package put.plane.boarding.simulator.plane;

import lombok.Builder;
import lombok.Data;
import put.plane.boarding.simulator.passenger.Passenger;
import put.plane.boarding.simulator.plane.structure.File;
import put.plane.boarding.simulator.plane.structure.queue.Queue;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class Plane {
    private final int rows;
    private final Queue queue;
    private final List<File> files;

    public int getColumns() {
        return files.size();
    }

    public void boardPassengers(List<Passenger> passengers) {
        queue.boardPassengers(passengers);
    }
}
