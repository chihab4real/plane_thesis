package put.plane.boarding.optimizing;

import lombok.extern.slf4j.Slf4j;
import put.plane.boarding.passengers.generator.Passenger;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.factory.PlaneFactory;
import put.plane.boarding.simulator.problem.DeplainingProblem;
import put.plane.boarding.simulator.problem.PassengerGroup;
import put.plane.boarding.simulator.problem.factory.passenger.PassengerFactory;
import put.plane.boarding.simulator.simulator.Simulator;
import put.plane.boarding.simulator.simulator.SimulatorRequest;
import put.plane.boarding.simulator.simulator.SimulatorResponse;
import put.plane.boarding.simulator.simulator.frame.XMLService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
public class AdvancedOptimizer extends Optimizer {
    public AdvancedOptimizer(Simulator simulator, XMLService xmlservice, PlaneFactory planeFactory) {
        super(simulator, xmlservice, planeFactory);
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


        int time = getTimeForOrder(plane, mappedPassengers, path, methodName, saveVisualization);

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

    @Override
    public OptimizerResult run(Plane plane, List<Passenger> generatedPassengers, String path, boolean logs) {
        return null;
    }

    protected boolean isValidIndividual(List<List<Integer>> individual, int totalPassengers) {
        if (individual == null || individual.isEmpty()) {
            return false;
        }

        boolean[] seen = new boolean[totalPassengers];
        int count = 0;

        for (List<Integer> group : individual) {
            for (Integer idx : group) {
                if (idx < 0 || idx >= totalPassengers || seen[idx]) {
                    return false; // Invalid index or duplicate
                }
                seen[idx] = true;
                count++;
            }
        }

        return count == totalPassengers; // All passengers present
    }

    // Evaluate fitness WITHOUT saving visualization
    protected int evaluateFitness(Plane plane, List<List<Integer>> indexGroups, List<Passenger> generatedPassengers) {
        log.info("Evaluating individual with {} groups", indexGroups.size());

        List<List<Passenger>> passengerGroups = indexGroups.stream()
                .map(group -> group.stream()
                        .map(generatedPassengers::get)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());

        Plane freshPlane = planeFactory.create(plane.getRows(), plane.getColumns());
        List<PassengerGroup> currPassengers = PassengerFactory.createSimulatorPassengers(freshPlane, passengerGroups);
        freshPlane.boardPassengers(currPassengers);

        DeplainingProblem problem = DeplainingProblem.builder()
                .plane(freshPlane)
                .passengers(currPassengers)
                .build();

        SimulatorRequest request = new SimulatorRequest(problem);

        long startTime = System.currentTimeMillis();
        log.info("Starting simulation...");

        SimulatorResponse response = simulator.simulate(request);

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("Simulation completed: {} ticks in {} ms", response.time(), elapsed);

        return response.time();
    }


    protected List<List<Integer>> deepCopyIndices(List<List<Integer>> original) {
        return original.stream()
                .map(ArrayList::new)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    // Generate random initial solution
    protected List<List<Integer>> generateRandomSolution(int numPassengers, Random random) {
        List<Integer> allIndices = new ArrayList<>();
        for (int i = 0; i < numPassengers; i++) {
            allIndices.add(i);
        }
        Collections.shuffle(allIndices, random);

        int minGroupSize = 2;
        int maxGroupSize = 8;
        List<List<Integer>> solution = new ArrayList<>();
        int idx = 0;

        while (idx < numPassengers) {
            int groupSize = minGroupSize + random.nextInt(maxGroupSize - minGroupSize + 1);
            groupSize = Math.min(groupSize, numPassengers - idx);

            List<Integer> group = new ArrayList<>(allIndices.subList(idx, idx + groupSize));
            solution.add(group);
            idx += groupSize;
        }

        return solution;
    }


    protected List<List<Integer>> generateNeighbor(List<List<Integer>> current, Random random) {
        List<List<Integer>> neighbor = deepCopyIndices(current);

        if (neighbor.isEmpty()) {
            return neighbor;
        }

        // Choose random neighborhood operator
        double operationType = random.nextDouble();

        if (operationType < 0.25 && neighbor.size() > 1) {
            // 1. Merge two adjacent groups
            int idx = random.nextInt(neighbor.size() - 1);
            neighbor.get(idx).addAll(neighbor.get(idx + 1));
            neighbor.remove(idx + 1);

        } else if (operationType < 0.5 && !neighbor.isEmpty()) {
            // 2. Split random group
            int idx = random.nextInt(neighbor.size());
            List<Integer> group = neighbor.get(idx);
            if (group.size() > 1) {
                int splitPoint = 1 + random.nextInt(group.size() - 1);
                List<Integer> newGroup = new ArrayList<>(group.subList(splitPoint, group.size()));
                neighbor.set(idx, new ArrayList<>(group.subList(0, splitPoint)));
                neighbor.add(idx + 1, newGroup);
            }

        } else if (operationType < 0.75 && neighbor.size() > 1) {
            // 3. Swap two random groups
            int idx1 = random.nextInt(neighbor.size());
            int idx2 = random.nextInt(neighbor.size());
            Collections.swap(neighbor, idx1, idx2);

        } else if (neighbor.size() > 2) {
            // 4. Move random group to different position
            int fromIdx = random.nextInt(neighbor.size());
            int toIdx = random.nextInt(neighbor.size());
            List<Integer> group = neighbor.remove(fromIdx);
            neighbor.add(toIdx, group);
        }

        return neighbor;
    }


}
