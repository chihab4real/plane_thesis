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
public class TabuSearchOptimizer extends AdvancedOptimizer {


    public OptimizerResult run(boolean logs, Plane plane, List<Passenger> generatedPassengers, String path, int maxIterations, int tabuTenure,
                               int neighborhoodSize) {
        int totalPassengers = generatedPassengers.size();

        if (logs) {
            log.info("Starting Tabu Search with {} passengers", totalPassengers);
            log.info("Max iterations: {}, Tabu tenure: {}, Neighborhood size: {}",
                    maxIterations, tabuTenure, neighborhoodSize);
        }

        InitialSolution initial = generateInitialSolution(totalPassengers, plane, generatedPassengers);
        List<List<Integer>> currentSolution = initial.currentSolution();
        int currentEnergy = initial.currentEnergy();
        List<List<Integer>> bestSolution = initial.bestSolution();
        int bestEnergy = initial.bestEnergy();

        // Tabu list - stores hash codes of recent solutions
        List<NeighborSolution> tabuList = new ArrayList<>();

        Random random = new Random();
        int iteration = 0;
        int iterationsWithoutImprovement = 0;
        final int DIVERSIFICATION_THRESHOLD = 50; // Reset if stuck

        if (logs) {
            log.info("Initial solution: {} ticks, {} groups", currentEnergy, currentSolution.size());
        }

        long startTime = System.currentTimeMillis();
        long MAX_TIME_MS = 150_000;


        while (iteration < maxIterations && !shouldStop(startTime, MAX_TIME_MS, iteration, maxIterations)) {
            // Generate neighborhood
            List<NeighborSolution> neighbors = new ArrayList<>();

            for (int i = 0; i < neighborhoodSize; i++) {
                List<List<Integer>> neighbor = generateNeighbor(currentSolution, random);

                if (!isValidIndividual(neighbor, totalPassengers)) {
                    continue;
                }


                int neighborEnergy = evaluateFitness(plane, neighbor, generatedPassengers);

                neighbors.add(new NeighborSolution(neighbor, neighborEnergy));
            }

            if (neighbors.isEmpty()) {
                log.warn("No valid neighbors generated at iteration {}", iteration);
                iteration++;
                continue;
            }

            // Find best non-tabu neighbor (aspiration criterion: accept if better than best ever)
            NeighborSolution bestNeighbor = null;

            for (NeighborSolution neighbor : neighbors) {
                boolean isTabu = tabuList.contains(neighbor);
                boolean aspirationCriterion = neighbor.energy() < bestEnergy; // Override tabu if global best

                if (!isTabu || aspirationCriterion) {
                    if (bestNeighbor == null || neighbor.energy() < bestNeighbor.energy()) {
                        bestNeighbor = neighbor;
                    }
                }
            }

            // If all neighbors are tabu and none meet aspiration, take least tabu
            if (bestNeighbor == null) {
                bestNeighbor = neighbors.stream()
                        .min((a, b) -> Integer.compare(a.energy(), b.energy()))
                        .orElse(null);

                if (logs) {
                    log.info("Iter {}: All neighbors tabu, forcing move", iteration);
                }
            }

            if (bestNeighbor == null) {
                log.error("No neighbor found at iteration {}", iteration);
                break;
            }

            // Move to best neighbor
            currentSolution = bestNeighbor.solution();
            currentEnergy = bestNeighbor.energy();

            // Update tabu list
            tabuList.add(bestNeighbor);
            if (tabuList.size() > tabuTenure) {
                tabuList.remove(0); // Remove oldest tabu
            }

            // Update best solution
            if (currentEnergy < bestEnergy) {
                bestSolution = deepCopyIndices(currentSolution);
                bestEnergy = currentEnergy;
                iterationsWithoutImprovement = 0;

                if (logs) {
                    log.info("Iter {}: New best = {} ticks ({} groups)",
                            iteration, bestEnergy, bestSolution.size());
                }
            } else {
                iterationsWithoutImprovement++;
            }

            // Diversification: restart from random solution if stuck
            if (iterationsWithoutImprovement >= DIVERSIFICATION_THRESHOLD) {
                if (logs) {
                    log.info("Iter {}: No improvement for {} iterations, diversifying",
                            iteration, DIVERSIFICATION_THRESHOLD);
                }

                currentSolution = generateRandomSolution(totalPassengers, random);
                currentEnergy = evaluateFitness(plane, currentSolution, generatedPassengers);
                tabuList.clear(); // Clear tabu list on restart
                iterationsWithoutImprovement = 0;
            }

            iteration++;

            // Log progress
            if (logs && iteration % 50 == 0) {
                log.info("Iter {}/{} - Best: {} ticks, Current: {} ticks, Tabu size: {}",
                        iteration, maxIterations, bestEnergy, currentEnergy, tabuList.size());
            }
        }

        if (logs) {
            log.info("Tabu Search completed. Best time: {} ticks", bestEnergy);
            log.info("Best solution has {} groups", bestSolution.size());
            log.info("Running final simulation WITH visualization...");
        }

        return runOptimization(plane, logs, path, "TabuSearch",
                "Tabu Search Optimization", bestSolution, generatedPassengers, false);
    }


    @Override
    public OptimizerResult runOptimization(Plane plane, boolean logs, String path, String methodName, String description,
                                            List<List<Integer>> allGroups, List<Passenger> generatedPassengers,
                                            boolean saveVisualization) {
        List<List<Passenger>> mappedPassengers = allGroups
            .stream()
            .map(passengers -> passengers
                    .stream()
                    .map(generatedPassengers::get)
                    .toList())
            .collect(Collectors.toCollection(ArrayList::new));

        mappedPassengers = sortGroupsBySeatOrder(mappedPassengers, plane.getColumns());


        if ("Zigzag".equals(methodName)) {
            Collections.reverse(mappedPassengers);
        }

        int time = getTimeForOrder(plane, mappedPassengers, path, methodName, saveVisualization);

        if (logs) {
            log.info(description);
            log.info("Total groups: {}", allGroups.size());
            for (int i = 0; i < allGroups.size(); i++) {
                log.info("Group {} size: {}", i + 1, allGroups.get(i).size());
            }
        }

        List<Integer> flattenedSolution = allGroups.stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());

        return new OptimizerResult(methodName, time, flattenedSolution, allGroups);
    }




}
