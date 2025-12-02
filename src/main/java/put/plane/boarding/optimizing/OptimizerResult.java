package put.plane.boarding.optimizing;

import java.util.List;

public record OptimizerResult(
        float bestTime,
        List<Integer> bestSolution
) {}
