package put.plane.boarding.service.agent;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public abstract class Agent {

    private final Seat seat;
    private final JsonNode spec;

    public abstract int timeToGetOut();
    public abstract int timeToMove();
}
