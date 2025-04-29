package put.plane.boarding.service.orchestrator;

import lombok.AllArgsConstructor;
import lombok.Data;
import put.plane.boarding.service.problem.DeplainingProblem;

@Data
@AllArgsConstructor
public class OrchestratorRequest {

    private DeplainingProblem problem;
}
