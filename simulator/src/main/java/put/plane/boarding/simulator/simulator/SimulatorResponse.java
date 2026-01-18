package put.plane.boarding.simulator.simulator;

import lombok.Builder;
import put.plane.boarding.simulator.simulator.frame.dto.VisualizationDto;

@Builder
public record SimulatorResponse(
        int time,
        VisualizationDto visualizationDto,
        long totalTimeWastedWithoutMove
) {
}
