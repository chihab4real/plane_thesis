package put.plane.boarding.service.orchestrator;

import lombok.Builder;
import lombok.Data;
import put.plane.boarding.service.problem.DeplainingProblem;
import put.plane.boarding.service.strategy.OrderStrategy;

@Data
@Builder
public class OrchestratorRequest {

    private DeplainingProblem problem;
    private OrderStrategy strategy;
}
