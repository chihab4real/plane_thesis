package put.plane.boarding.simulator.simulator;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import put.plane.boarding.simulator.problem.DeplainingProblem;

@Data
@RequiredArgsConstructor
public class SimulatorRequest {

    private final DeplainingProblem problem;
    private boolean saveVisualization = false;
}
