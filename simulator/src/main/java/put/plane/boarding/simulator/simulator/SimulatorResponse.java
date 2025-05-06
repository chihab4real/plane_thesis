package put.plane.boarding.simulator.simulator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
public record SimulatorResponse(
        int time
) {}
