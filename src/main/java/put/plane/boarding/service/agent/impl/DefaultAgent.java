package put.plane.boarding.service.agent.impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.EqualsAndHashCode;
import put.plane.boarding.service.agent.Agent;
import put.plane.boarding.service.agent.Seat;

@EqualsAndHashCode(callSuper=true)
public class DefaultAgent extends Agent {

    public DefaultAgent(Seat seat, JsonNode spec) {
        super(seat, spec);
    }

    @Override
    public int timeToGetOut() {
        return 1;
    }

    @Override
    public int timeToMove() {
        return 1;
    }
}
