package put.plane.boarding.simulator.simulator;

import lombok.Builder;
import put.plane.boarding.simulator.plane.structure.Seat;
import put.plane.boarding.simulator.simulator.frame.dto.VisualizationDto;

import java.util.Map;

@Builder
public record SimulatorResponse(
        int time,
        VisualizationDto visualizationDto,
        long totalTimeWastedWithoutMove,
        Map<Seat, Long> waitCount
) {
}
