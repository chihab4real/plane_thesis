package put.plane.boarding.optimizing;

import java.util.List;

public record OptimizerResult(
        String methodName,
        int bestTime,
        List<Integer> bestSolution,
        List<List<Integer>> passengerGroups
) {}
