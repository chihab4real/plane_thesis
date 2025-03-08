package put.plane.boarding.service.problem.factory.agent;

import put.plane.boarding.service.agent.Agent;
import put.plane.boarding.service.agent.File;
import put.plane.boarding.service.agent.Seat;
import put.plane.boarding.service.agent.impl.DefaultAgent;

public class AgentFactory {

    public Agent create(int row, File file) {

        var seat = new Seat(row, file);

        return new DefaultAgent(seat, null);
    }
}
