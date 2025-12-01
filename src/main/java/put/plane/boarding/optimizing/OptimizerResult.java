package put.plane.boarding.optimizing;

import java.util.List;

public record OptimizerResult(
        int bestTime,
        List<Integer> bestSolution
) {}
