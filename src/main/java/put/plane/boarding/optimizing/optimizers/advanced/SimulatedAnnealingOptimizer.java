package put.plane.boarding.optimizing.optimizers.advanced;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
@Service
public class SimulatedAnnealingOptimizer extends AdvancedOptimizer {

    public OptimizerResult run(boolean logs, Plane plane, List<Passenger> generatedPassengers,String path, double initialTemp, double coolingRate,
                               int maxIterations) {
        int totalPassengers = generatedPassengers.size();

        if (logs) {
            log.info("Starting Simulated Annealing with {} passengers", totalPassengers);
            log.info("Initial temp: {}, Cooling rate: {}, Max iterations: {}", initialTemp, coolingRate, maxIterations);
        }

        // Generate initial random solution
        InitialSolution initial = generateInitialSolution(totalPassengers, plane, generatedPassengers);
        List<List<Integer>> currentSolution = initial.currentSolution();
        int currentEnergy = initial.currentEnergy();
        List<List<Integer>> bestSolution = initial.bestSolution();
        int bestEnergy = initial.bestEnergy();

        Random random = new Random();
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
        return runOptimization(plane, logs, path, "SimulatedAnnealing",
                "Simulated Annealing Optimization", bestSolution, generatedPassengers, true);
    }

}
