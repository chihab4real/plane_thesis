package put.plane.boarding.optimizing;

import java.util.List;

public record OptimizerResult(
        String methodName,
        int bestTime,
        List<Integer> bestSolution,
        List<List<Integer>> passengerGroups
) {
    public OptimizerResult(int bestTime, List<Integer> bestSolution) {
        this(null, bestTime, bestSolution, List.of());
    }

    public OptimizerResult(int bestTime, List<Integer> bestSolution, List<List<Integer>> passengerGroups) {
        this(null, bestTime, bestSolution, passengerGroups);
    }
}
