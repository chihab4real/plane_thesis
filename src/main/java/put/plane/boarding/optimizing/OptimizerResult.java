package put.plane.boarding.optimizing;

import java.util.List;
import java.util.Map;

public record OptimizerResult(
        String methodName,
        int bestTime,
        List<Integer> bestSolution,
        List<List<Integer>> passengerGroups,
        Map<Integer, Long> waitCountPerPassenger
) {
    public OptimizerResult(int bestTime, List<Integer> bestSolution) {
        this(null, bestTime, bestSolution, List.of(), Map.of());
    }

    public OptimizerResult(int bestTime, List<Integer> bestSolution, List<List<Integer>> passengerGroups) {
        this(null, bestTime, bestSolution, passengerGroups, Map.of());
    }
}
