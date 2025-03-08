package put.plane.boarding.service.orchestrator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import put.plane.boarding.service.agent.Agent;

import java.util.*;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
public final class Orchestrator {

    public OrchestratorResponse orchestrate(OrchestratorRequest request) {

        var agentsOutOfQueue = new ArrayList<>(request.getProblem().getAgents());
        var strategy = request.getStrategy();
        var rows = request.getProblem().getRows();
        var agentQueue = new ArrayList<Agent>(Collections.nCopies(rows, null));

        var resultList = new ArrayList<List<Agent>>();
        var resultTime = 0;

        while (!agentsOutOfQueue.isEmpty() || !isAllNull(agentQueue)) {
            agentQueue.set(0, null);

            var resultIteration = new ArrayList<Agent>();

            IntStream.range(0, rows).forEach(row -> {
                var agentsFromSeats = agentsOutOfQueue
                        .stream()
                        .filter(agent -> agent.getSeat().row() == row)
                        .toList();

                var agentFromQueue = row + 1 == rows ? null : agentQueue.get(row + 1);

                var nextToMakeStep = strategy.selectNext(agentsFromSeats, agentFromQueue);

                if (nextToMakeStep != null) {
                    if (nextToMakeStep.equals(agentFromQueue)) {
                        agentQueue.set(row, agentFromQueue);
                        agentQueue.set(row + 1, null);
                    } else {
                        agentQueue.set(row, nextToMakeStep);
                        resultIteration.add(nextToMakeStep);
                        agentsOutOfQueue.remove(nextToMakeStep);
                    }
                }
            });

            if (!resultIteration.isEmpty()) {
                resultList.add(resultIteration);
            }
            resultTime += 1;
        }

        return new OrchestratorResponse(resultList, resultTime);
    }

    private boolean isAllNull(List<Agent> agents) {
        return agents.stream().allMatch(Objects::isNull);
    }
}
