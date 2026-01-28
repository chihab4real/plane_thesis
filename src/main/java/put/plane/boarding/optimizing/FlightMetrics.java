package put.plane.boarding.optimizing;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FlightMetrics {
    // Identifiers
    private String flightID;
    private String methodName;

    // Basic metrics
    private int totalDeplaningTime;
    private int numberOfGroups;
    private int totalCallsToSimulator;
    private long optimizationDurationMillis;

    // Wait time metrics (per-passenger)
    private double avgWaitTime;
    private double stdDevWaitTime;
    private long maxWaitTime;
    private long minWaitTime;
    private long medianWaitTime;
    private long p25WaitTime;
    private long p75WaitTime;
    private long p90WaitTime;
    private long p95WaitTime;

    // Group size metrics
    private double avgGroupSize;
    private double stdDevGroupSize;
    private int maxGroupSize;
    private int minGroupSize;

    // Distribution metrics
    private double coefficientOfVariation;
    private double skewness;
    private long totalWaitTime;
    private int passengersWithZeroWait;
    private double percentageZeroWait;

    // NEW: Optimization Process Metrics
    private int bestFitness;           // Best boarding time found
    private double avgFitness;         // Mean boarding time across iterations
    private int worstFitness;          // Worst boarding time encountered
    private int fitnessImprovement;    // Difference between worst and best
    private double fitnessStdDev;      // Spread of fitness values
    private int iterationsToBest;      // When best solution was found
    private double convergenceRate;    // How fast it converged (best_iteration / total_iterations)
    private int plateauIterations;     // Iterations without improvement
    private double diversityScore;     // Population diversity (for GA)
    private double convergenceStability; // Coefficient of variation in final iterations
}
