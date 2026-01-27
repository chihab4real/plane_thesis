package put.plane.boarding.optimizing;

import lombok.extern.slf4j.Slf4j;
import put.plane.boarding.passengers.generator.Passenger;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.structure.Seat;
import put.plane.boarding.simulator.plane.factory.PlaneFactory;
import put.plane.boarding.simulator.problem.DeplainingProblem;
import put.plane.boarding.simulator.problem.PassengerGroup;
import put.plane.boarding.simulator.problem.factory.passenger.PassengerFactory;
import put.plane.boarding.simulator.simulator.Simulator;
import put.plane.boarding.simulator.simulator.SimulatorRequest;
import put.plane.boarding.simulator.simulator.SimulatorResponse;
import put.plane.boarding.simulator.simulator.frame.XMLService;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class AdvancedOptimizer extends Optimizer {


    @Override
    public OptimizerResult runOptimization(Plane plane, boolean logs, String path, String methodName, String description,
                                           List<List<Integer>> allGroups, List<Passenger> generatedPassengers,
                                           boolean saveVisualization, int totalCallsToSimulator) {
        List<List<Passenger>> mappedPassengers = allGroups
            .stream()
            .map(passengers -> passengers
                    .stream()
                    .map(generatedPassengers::get)
                    .toList())
            .collect(Collectors.toCollection(ArrayList::new));

//        mappedPassengers = sortGroupsBySeatOrder(mappedPassengers, plane.getColumns());

        SimulatorResponse simulatorResponse = getTimeForOrder(plane, mappedPassengers, path, methodName, saveVisualization);
        int time = simulatorResponse.time();

        // Map wait count from Seat to passenger index
        Map<Integer, Long> waitCountPerPassenger = new HashMap<>();
        for (int i = 0; i < generatedPassengers.size(); i++) {
            Passenger p = generatedPassengers.get(i);
            String seatLocation = p.getSeatLocation();
            String[] parts = seatLocation.split("_");
            int row = Integer.parseInt(parts[0]) - 1;
            int fileIndex = Integer.parseInt(parts[1]) - 1;
            Seat seat = new Seat(row, plane.getFiles().get(fileIndex));
            Long waitTime = simulatorResponse.waitCount().get(seat);
            if (waitTime != null) {
                waitCountPerPassenger.put(i, waitTime);
            }
        }

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

        return new OptimizerResult(methodName, time, flattenedSolution, allGroups, waitCountPerPassenger, totalCallsToSimulator);
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

    protected int evaluateFitness(Plane plane, List<List<Integer>> indexGroups, List<Passenger> generatedPassengers) {
//        log.info("Evaluating individual with {} groups", indexGroups.size());

        List<List<Integer>> splitGroups = splitSameRowPassengers(indexGroups, generatedPassengers);

        List<List<Passenger>> passengerGroups = splitGroups.stream()
                .map(group -> group.stream()
                        .map(generatedPassengers::get)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());

        passengerGroups = sortGroupsBySeatOrder(passengerGroups, plane.getColumns());

        long startTime = System.currentTimeMillis();
//        log.info("Starting simulation...");

        int time = getTimeForOrder(plane, passengerGroups, null, null, false).time();

        long elapsed = System.currentTimeMillis() - startTime;
//        log.info("Simulation completed: {} ticks in {} ms", time, elapsed);

        return time;
    }

    private List<List<Integer>> splitSameRowPassengers(List<List<Integer>> indexGroups, List<Passenger> generatedPassengers) {
        List<List<Integer>> result = new ArrayList<>();

        for (List<Integer> group : indexGroups) {
            Map<Integer, List<Integer>> byRow = new LinkedHashMap<>();

            for (Integer idx : group) {
                Passenger p = generatedPassengers.get(idx);
                int row = Integer.parseInt(p.getSeatLocation().split("_")[0]);

                byRow.computeIfAbsent(row, k -> new ArrayList<>()).add(idx);
            }

            // Each row becomes a separate group
            result.addAll(byRow.values());
        }

        return result;
    }


    protected List<List<Integer>> deepCopyIndices(List<List<Integer>> original) {
        return original.stream()
                .map(ArrayList::new)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    // Generate random initial solution
    protected List<List<Integer>> createRandomGrouping(int numPassengers, Random random) {
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


    protected List<List<Integer>> generateRandomSolution(int numPassengers, Random random) {
        return createRandomGrouping(numPassengers, random);
    }

    protected InitialSolution generateInitialSolution(int totalPassengers, Plane plane, List<Passenger> generatedPassengers) {
        Random random = new Random();
        List<List<Integer>> currentSolution = generateRandomSolution(totalPassengers, random);
        int currentEnergy = evaluateFitness(plane, currentSolution, generatedPassengers);

        List<List<Integer>> bestSolution = deepCopyIndices(currentSolution);
        int bestEnergy = currentEnergy;

        return new InitialSolution(currentSolution, currentEnergy, bestSolution, bestEnergy);
    }

    protected record InitialSolution(
            List<List<Integer>> currentSolution,
            int currentEnergy,
            List<List<Integer>> bestSolution,
            int bestEnergy
    ) {}



    protected List<List<Integer>> generateNeighbor(List<List<Integer>> current, Random random) {
        List<List<Integer>> neighbor = deepCopyIndices(current);

        if (neighbor.isEmpty()) {
            return neighbor;
        }

        // Choose random neighborhood operator
        double operationType = random.nextDouble();

        if (operationType < 0.20 && neighbor.size() > 5) {
            // Merge two adjacent groups
            int idx = random.nextInt(neighbor.size() - 1);
            neighbor.get(idx).addAll(neighbor.get(idx + 1));
            neighbor.remove(idx + 1);
            // Split more aggressively
        } else if (operationType < 0.50 && !neighbor.isEmpty()) {
            // Split a random group
            int idx = random.nextInt(neighbor.size());
            List<Integer> group = neighbor.get(idx);
            if (group.size() >= 2) {  // Can split
                int splitPoint = 1 + random.nextInt(group.size() - 1);
                List<Integer> newGroup = new ArrayList<>(group.subList(splitPoint, group.size()));
                neighbor.set(idx, new ArrayList<>(group.subList(0, splitPoint)));
                neighbor.add(idx + 1, newGroup);
            }

        } else if (operationType < 0.75 && neighbor.size() > 1) {
            // 3. Swap two different random groups
            int idx1 = random.nextInt(neighbor.size());
            int idx2;
            do {
                idx2 = random.nextInt(neighbor.size());
            } while (idx1 == idx2 && neighbor.size() > 1);
            Collections.swap(neighbor, idx1, idx2);

        } else if (neighbor.size() > 2) {
            // 4. Move random group to different position
            int fromIdx = random.nextInt(neighbor.size());
            int toIdx;
            do {
                toIdx = random.nextInt(neighbor.size());
            } while (fromIdx == toIdx);
            List<Integer> group = neighbor.remove(fromIdx);
            neighbor.add(toIdx, group);
        }

        return neighbor;
    }

    protected boolean shouldStop(long startTime, long maxTimeMillis, int iteration, int maxIterations) {
        long elapsed = System.currentTimeMillis() - startTime;
        return elapsed >= maxTimeMillis || iteration >= maxIterations;
    }

    protected List<List<Passenger>> sortGroupsBySeatOrder(List<List<Passenger>> passengerGroups, int planeColumns) {
//        log.info("Sorting {} groups by seat order (plane has {} columns)", passengerGroups.size(), planeColumns);

        List<List<Passenger>> sorted = new ArrayList<>();

        for (List<Passenger> group : passengerGroups) {
            List<Passenger> sortedGroup = new ArrayList<>(group);

            // Log BEFORE sorting
            if (group.size() > 1) {
                log.debug("Before sort: {}", group.stream()
                    .map(p -> p.getSeatLocation() + "(p" + getSeatPriority(
                        Integer.parseInt(p.getSeatLocation().split("_")[1]), planeColumns) + ")")
                    .collect(Collectors.joining(", ")));
            }

            sortedGroup.sort((p1, p2) -> {
                String[] loc1 = p1.getSeatLocation().split("_");
                String[] loc2 = p2.getSeatLocation().split("_");

                int row1 = Integer.parseInt(loc1[0]);
                int row2 = Integer.parseInt(loc2[0]);

                // Different rows: sort by row number
                if (row1 != row2) {
                    return Integer.compare(row1, row2);
                }

                // Same row: sort by seat priority (aisle → middle → window)
                int col1 = Integer.parseInt(loc1[1]);
                int col2 = Integer.parseInt(loc2[1]);

                int priority1 = getSeatPriority(col1, planeColumns);
                int priority2 = getSeatPriority(col2, planeColumns);

                return Integer.compare(priority1, priority2);
            });

            // Log AFTER sorting
            if (group.size() > 1) {
                log.debug("After sort:  {}", sortedGroup.stream()
                    .map(p -> p.getSeatLocation() + "(p" + getSeatPriority(
                        Integer.parseInt(p.getSeatLocation().split("_")[1]), planeColumns) + ")")
                    .collect(Collectors.joining(", ")));
            }

            sorted.add(sortedGroup);
        }

//        log.info("Sorting completed");
        return sorted;
    }



    private int getSeatPriority(int column, int totalColumns) {
        if (totalColumns == 4) {
            // 20 rows × 4 columns: W-A-A-W
            if (column == 2 || column == 3) return 0; // Aisle
            return 2; // Window (1 or 4)
        } else if (totalColumns == 6) {
            // 30 rows × 6 columns: W-M-A-A-M-W
            if (column == 3 || column == 4) return 0; // Aisle
            if (column == 2 || column == 5) return 1; // Middle
            return 2; // Window (1 or 6)
        }

        // Fallback: assume center columns are aisles
        int halfCols = totalColumns / 2;
        if (column == halfCols || column == halfCols + 1) return 0; // Aisle
        if (column < halfCols) return column; // Left side: closer to aisle = lower priority
        return totalColumns - column + 1; // Right side: closer to aisle = lower priority
    }



}
