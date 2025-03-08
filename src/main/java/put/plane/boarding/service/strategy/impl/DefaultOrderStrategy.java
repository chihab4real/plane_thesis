package put.plane.boarding.service.strategy.impl;

import lombok.RequiredArgsConstructor;
import put.plane.boarding.service.agent.Agent;
import put.plane.boarding.service.strategy.OrderStrategy;

import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
public class DefaultOrderStrategy implements OrderStrategy {

    @Override
    public Agent selectNext(List<Agent> agentsFromSeats, Agent agentFromQueue) {

        if (agentFromQueue != null) {
            return agentFromQueue;
        }

        return agentsFromSeats
                .stream()
                .min(Comparator.comparing(agent -> agent.getSeat().file().distance()))
                .orElse(null);
    }
}
