package put.plane.boarding.simulator.simulator;

import lombok.AllArgsConstructor;
import lombok.Data;
import put.plane.boarding.simulator.problem.DeplainingProblem;

@Data
@AllArgsConstructor
public class SimulatorRequest {

    private DeplainingProblem problem;
}
