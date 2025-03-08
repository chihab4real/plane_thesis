package put.plane.boarding.service.orchestrator;

import lombok.AllArgsConstructor;
import lombok.Data;
import put.plane.boarding.service.agent.Agent;

import java.util.List;

@Data
@AllArgsConstructor
public class OrchestratorResponse {

    private List<List<Agent>> queueEnterOrder;
    private int time;
}
