package put.plane.boarding.service.strategy;

import put.plane.boarding.service.agent.Agent;

import java.util.List;

public interface OrderStrategy {

    Agent selectNext(List<Agent> agentsFromSeats, Agent agentFromQueue);
}
