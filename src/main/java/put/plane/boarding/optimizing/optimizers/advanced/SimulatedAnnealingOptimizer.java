package put.plane.boarding.optimizing.optimizers.advanced;

import lombok.extern.slf4j.Slf4j;
import put.plane.boarding.optimizing.AdvancedOptimizer;
import put.plane.boarding.optimizing.OptimizerResult;
import put.plane.boarding.passengers.generator.Passenger;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.factory.PlaneFactory;
import put.plane.boarding.simulator.simulator.Simulator;
import put.plane.boarding.simulator.simulator.frame.XMLService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
public class SimulatedAnnealingOptimizer extends AdvancedOptimizer {
    public SimulatedAnnealingOptimizer(Simulator simulator, XMLService xmlservice, PlaneFactory planeFactory) {
        super(simulator, xmlservice, planeFactory);
    }

    public OptimizerResult run(boolean logs, Plane plane, double initialTemp, double coolingRate,
                               int maxIterations) {
        List<Passenger> generatedPassengers = generatePassengers(plane);
        int totalPassengers = generatedPassengers.size();

        if (logs) {
            log.info("Starting Simulated Annealing with {} passengers", totalPassengers);
            log.info("Initial temp: {}, Cooling rate: {}, Max iterations: {}", initialTemp, coolingRate, maxIterations);
        }

        // Generate initial random solution
        Random random = new Random();
        List<List<Integer>> currentSolution = generateRandomSolution(totalPassengers, random);
        int currentEnergy = evaluateFitness(plane, currentSolution, generatedPassengers);

        List<List<Integer>> bestSolution = deepCopyIndices(currentSolution);
        int bestEnergy = currentEnergy;

        double temperature = initialTemp;
        int iteration = 0;
        int acceptedMoves = 0;
        int rejectedMoves = 0;

        if (logs) {
            log.info("Initial solution: {} ticks, {} groups", currentEnergy, currentSolution.size());
        }

        while (iteration < maxIterations && temperature > 0.01) {
            // Generate neighbor solution
            List<List<Integer>> neighbor = generateNeighbor(currentSolution, random);

            if (!isValidIndividual(neighbor, totalPassengers)) {
                log.warn("Generated invalid neighbor, skipping");
                iteration++;
                continue;
            }

            // Evaluate neighbor
            int neighborEnergy = evaluateFitness(plane, neighbor, generatedPassengers);

            // Calculate energy difference (lower is better)
            int deltaE = neighborEnergy - currentEnergy;

            // Acceptance probability
            boolean accept = false;
            if (deltaE < 0) {
                // Always accept better solutions
                accept = true;
            } else {
                // Accept worse solutions with probability e^(-deltaE/T)
                double acceptanceProbability = Math.exp(-deltaE / temperature);
                accept = random.nextDouble() < acceptanceProbability;
            }

            if (accept) {
                currentSolution = neighbor;
                currentEnergy = neighborEnergy;
                acceptedMoves++;

                // Update best solution if improved
                if (neighborEnergy < bestEnergy) {
                    bestSolution = deepCopyIndices(neighbor);
                    bestEnergy = neighborEnergy;

                    if (logs) {
                        log.info("Iter {}: New best = {} ticks (temp: {:.2f})", iteration, bestEnergy, temperature);
                    }
                }
            } else {
                rejectedMoves++;
            }

            // Cool down
            temperature *= coolingRate;
            iteration++;

            // Log progress every 100 iterations
            if (logs && iteration % 100 == 0) {
                double acceptanceRate = (double) acceptedMoves / (acceptedMoves + rejectedMoves) * 100;
                log.info("Iter {}/{} - Best: {} ticks, Current: {} ticks, Temp: {:.2f}, Acceptance: {:.1f}%",
                        iteration, maxIterations, bestEnergy, currentEnergy, temperature, acceptanceRate);
                acceptedMoves = 0;
                rejectedMoves = 0;
            }
        }

        if (logs) {
            log.info("Simulated Annealing completed. Best time: {} ticks", bestEnergy);
            log.info("Best solution has {} groups", bestSolution.size());
            log.info("Running final simulation WITH visualization...");
        }

        // Save visualization for best solution
        return runOptimization(plane, logs, "SimulatedAnnealing",
                "Simulated Annealing Optimization", bestSolution, generatedPassengers, true);
    }

    @Override
    public OptimizerResult runOptimization(Plane plane, boolean logs, String methodName, String description,
                                            List<List<Integer>> allGroups, List<Passenger> generatedPassengers,
                                            boolean saveVisualization) {
        List<List<Passenger>> mappedPassengers = allGroups
                .stream()
                .map(passengers -> passengers
                        .stream()
                        .map(generatedPassengers::get)
                        .toList())
                .collect(Collectors.toCollection(ArrayList::new));

        if ("Zigzag".equals(methodName)) {
            Collections.reverse(mappedPassengers);
        }

        int time = getTimeForOrder(plane, mappedPassengers, methodName, saveVisualization);

        if (logs) {
            log.info(description);
            log.info("Total groups: {}", allGroups.size());
            for (int i = 0; i < allGroups.size(); i++) {
                log.info("Group {} size: {}", i + 1, allGroups.get(i).size());
            }
        }

        // Flatten groups into a single list of passenger IDs
        List<Integer> flattenedSolution = allGroups.stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        return new OptimizerResult(time, flattenedSolution);
    }



}
