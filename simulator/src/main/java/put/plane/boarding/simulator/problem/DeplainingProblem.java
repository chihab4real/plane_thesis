package put.plane.boarding.simulator.problem;

import lombok.Builder;
import lombok.Data;
import put.plane.boarding.simulator.passenger.SimulatorPassenger;
import put.plane.boarding.simulator.plane.Plane;

import java.util.List;

@Data
@Builder
public class DeplainingProblem {

    private List<SimulatorPassenger> passengers;
    private Plane plane;
}
