package put.plane.boarding.optimizing;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MetricsCalculator {

    public FlightMetrics calculateMetrics(OptimizerResult result, String flightID) {
        Map<Integer, Long> waitTimes = result.getWaitCountPerPassenger();
        List<List<Integer>> groups = result.getPassengerGroups();
        List<Integer> fitnessHistory = result.getFitnessHistory();

        List<Long> waitTimesList = new ArrayList<>(waitTimes.values());
        Collections.sort(waitTimesList);

        List<Integer> groupSizes = groups.stream()
                .map(List::size)
                .collect(Collectors.toList());

        // Calculate optimization process metrics
        OptimizationMetrics optMetrics = calculateOptimizationMetrics(fitnessHistory,result.getIterationOfBestSolution(),result.getDiversityHistory());


        return FlightMetrics.builder()
                .flightID(flightID)
                .methodName(result.getMethodName())
                .totalDeplaningTime(result.getBestTime())
                .numberOfGroups(groups.size())
                .totalCallsToSimulator(result.getTotalCallsToSimulator())
                .optimizationDurationMillis(result.getOptimizationDurationMillis())

                // Wait time metrics
                .avgWaitTime(calculateMean(waitTimesList))
                .stdDevWaitTime(calculateStdDev(waitTimesList))
                .maxWaitTime(waitTimesList.isEmpty() ? 0 : Collections.max(waitTimesList))
                .minWaitTime(waitTimesList.isEmpty() ? 0 : Collections.min(waitTimesList))
                .medianWaitTime(calculatePercentile(waitTimesList, 50))

                // Percentiles
                .p25WaitTime(calculatePercentile(waitTimesList, 25))
                .p75WaitTime(calculatePercentile(waitTimesList, 75))
                .p90WaitTime(calculatePercentile(waitTimesList, 90))
                .p95WaitTime(calculatePercentile(waitTimesList, 95))

                // Group metrics
                .avgGroupSize(calculateMean(groupSizes.stream().map(Long::valueOf).collect(Collectors.toList())))
                .stdDevGroupSize(calculateStdDev(groupSizes.stream().map(Long::valueOf).collect(Collectors.toList())))
                .maxGroupSize(groupSizes.isEmpty() ? 0 : Collections.max(groupSizes))
                .minGroupSize(groupSizes.isEmpty() ? 0 : Collections.min(groupSizes))

                // Distribution metrics
                .coefficientOfVariation(calculateCV(waitTimesList))
                .skewness(calculateSkewness(waitTimesList))
                .totalWaitTime(waitTimesList.stream().mapToLong(Long::longValue).sum())
                .passengersWithZeroWait(countZeroWaits(waitTimesList))
                .percentageZeroWait(calculatePercentageZeroWait(waitTimesList))


                .bestFitness(optMetrics.bestFitness)
                .avgFitness(optMetrics.avgFitness)
                .worstFitness(optMetrics.worstFitness)
                .fitnessImprovement(optMetrics.fitnessImprovement)
                .fitnessStdDev(optMetrics.fitnessStdDev)
                .iterationsToBest(optMetrics.iterationsToBest)
                .convergenceRate(optMetrics.convergenceRate)
                .plateauIterations(optMetrics.plateauIterations)
                .diversityScore(optMetrics.avgDiversity)
                .convergenceStability(optMetrics.convergenceStability)
                .build();
    }

    private OptimizationMetrics calculateOptimizationMetrics(List<Integer> fitnessHistory,
                                                             int iterationOfBest,
                                                             List<Double> diversityHistory) {
        if (fitnessHistory.isEmpty()) {
            return new OptimizationMetrics(0, 0.0, 0, 0, 0.0, 0, 0.0, 0, 0.0, 0.0);
        }

        int best = Collections.min(fitnessHistory);
        int worst = Collections.max(fitnessHistory);
        double avg = fitnessHistory.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double stdDev = calculateStdDevInt(fitnessHistory);

        // Convergence rate: how quickly it found the best
        double convergenceRate = fitnessHistory.size() > 0
                ? (double) iterationOfBest / fitnessHistory.size()
                : 0.0;

        // Plateau detection: consecutive iterations without improvement
        int plateau = calculatePlateauLength(fitnessHistory);

        // Convergence stability: variance in final 10% of iterations
        double stability = calculateConvergenceStability(fitnessHistory);

        // Average diversity (if tracked)
        double avgDiversity = diversityHistory.isEmpty()
                ? 0.0
                : diversityHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        return new OptimizationMetrics(
                best,
                avg,
                worst,
                worst - best,
                stdDev,
                iterationOfBest,
                convergenceRate,
                plateau,
                avgDiversity,
                stability
        );
    }
    private double calculateStdDevInt(List<Integer> values) {
        if (values.size() < 2) return 0.0;
        double mean = values.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0.0);
        return Math.sqrt(variance);
    }

    private int calculatePlateauLength(List<Integer> fitnessHistory) {
        if (fitnessHistory.size() < 2) return 0;

        int currentBest = fitnessHistory.get(0);
        int plateauCount = 0;
        int maxPlateau = 0;

        for (int fitness : fitnessHistory) {
            if (fitness <= currentBest) {
                currentBest = fitness;
                maxPlateau = Math.max(maxPlateau, plateauCount);
                plateauCount = 0;
            } else {
                plateauCount++;
            }
        }
        return Math.max(maxPlateau, plateauCount);
    }

    private double calculateConvergenceStability(List<Integer> fitnessHistory) {
        if (fitnessHistory.size() < 10) return 0.0;

        int startIdx = (int) (fitnessHistory.size() * 0.9); // Last 10%
        List<Integer> finalPhase = fitnessHistory.subList(startIdx, fitnessHistory.size());

        return calculateStdDevInt(finalPhase);
    }

    private static class OptimizationMetrics {
        int bestFitness;
        double avgFitness;
        int worstFitness;
        int fitnessImprovement;
        double fitnessStdDev;
        int iterationsToBest;
        double convergenceRate;
        int plateauIterations;
        double avgDiversity;
        double convergenceStability;

        OptimizationMetrics(int bestFitness, double avgFitness, int worstFitness,
                            int fitnessImprovement, double fitnessStdDev,
                            int iterationsToBest, double convergenceRate,
                            int plateauIterations, double avgDiversity,
                            double convergenceStability) {
            this.bestFitness = bestFitness;
            this.avgFitness = avgFitness;
            this.worstFitness = worstFitness;
            this.fitnessImprovement = fitnessImprovement;
            this.fitnessStdDev = fitnessStdDev;
            this.iterationsToBest = iterationsToBest;
            this.convergenceRate = convergenceRate;
            this.plateauIterations = plateauIterations;
            this.avgDiversity = avgDiversity;
            this.convergenceStability = convergenceStability;
        }
    }





    private double calculateMean(List<Long> values) {
        if (values.isEmpty()) return 0.0;
        return values.stream().mapToLong(Long::longValue).average().orElse(0.0);
    }

    private double calculateStdDev(List<Long> values) {
        if (values.size() < 2) return 0.0;
        double mean = calculateMean(values);
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0.0);
        return Math.sqrt(variance);
    }

    private long calculatePercentile(List<Long> sortedValues, int percentile) {
        if (sortedValues.isEmpty()) return 0;
        int index = (int) Math.ceil(percentile / 100.0 * sortedValues.size()) - 1;
        index = Math.max(0, Math.min(index, sortedValues.size() - 1));
        return sortedValues.get(index);
    }

    private double calculateCV(List<Long> values) {
        double mean = calculateMean(values);
        if (mean == 0) return 0.0;
        double stdDev = calculateStdDev(values);
        return stdDev / mean;
    }

    private double calculateSkewness(List<Long> values) {
        if (values.size() < 3) return 0.0;
        double mean = calculateMean(values);
        double stdDev = calculateStdDev(values);
        if (stdDev == 0) return 0.0;

        double sumCubed = values.stream()
                .mapToDouble(v -> Math.pow((v - mean) / stdDev, 3))
                .sum();

        int n = values.size();
        return (n * sumCubed) / ((n - 1) * (n - 2));
    }

    private int countZeroWaits(List<Long> values) {
        return (int) values.stream().filter(v -> v == 0).count();
    }

    private double calculatePercentageZeroWait(List<Long> values) {
        if (values.isEmpty()) return 0.0;
        return (countZeroWaits(values) * 100.0) / values.size();
    }
}
