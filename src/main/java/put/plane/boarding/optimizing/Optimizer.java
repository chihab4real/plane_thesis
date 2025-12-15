package put.plane.boarding.optimizing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import put.plane.boarding.passengers.generator.Passenger;
import put.plane.boarding.passengers.generator.PassengerGenerator;
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
@Service
@RequiredArgsConstructor
public class Optimizer {

    private final Simulator simulator;
    private final XMLService xmlservice;
    private final PlaneFactory planeFactory;

   /* public OptimizerResult randomOptimization(int numRepetitions, boolean logs, List<Integer> order, Plane plane) {
        int bestTime = -1;
        List<Integer> bestOrder = new ArrayList<>(order);

        for (int i = 0; i < numRepetitions; i++) {
            Collections.shuffle(order);

            int time = getTimeForOrder(order, plane);

            if (time < bestTime || bestTime < 0) {
                bestTime = time;
                Collections.copy(bestOrder, order);

//                if (time < this.bestTime) {
//                    this.bestTime = time;
//                    Collections.copy(this.bestOrder, order);
//                }

                if (logs) {
                    log.info("TIME({}): {}\nORDER: {}", i, bestTime, bestOrder);
                }
            }
        }

        return new OptimizerResult(bestTime, bestOrder);
    }*/

    /*public OptimizerResult pairSwapOptimization(int numRepetitions, int noChangeLimit, boolean logs, List<Integer> order, Plane plane) {
        int bestTime = -1;

        Collections.shuffle(order);
        List<Integer> lastOrder = new ArrayList<>(order);
        List<Integer> bestOrder = new ArrayList<>(lastOrder);
        Collections.copy(lastOrder, order);
        int lastTime = getTimeForOrder(lastOrder, plane);

        int noChange = 0;
        for (int i = 0; i < numRepetitions; i++) {
            order = swapTwoRand(lastOrder);
            int time = getTimeForOrder(order, plane);

            if (time < lastTime) {
                Collections.copy(lastOrder, order);
                lastTime = time;

                if (time < bestTime || bestTime < 0) {
                    bestTime = time;
                    Collections.copy(bestOrder, order);
                }

                if (logs) {
                    log.info("TIME({}): {}\nORDER: {}", i, lastTime, lastOrder);
                }
            }
            else {
                noChange++;

                if (noChange >= noChangeLimit) {
                    noChange = 0;

                    Collections.shuffle(lastOrder);
                    lastTime = getTimeForOrder(lastOrder, plane);
                    if (logs) {
                        log.info("Started from random point\nTIME({}): {}\nORDER: {}", i, lastTime, lastOrder);
                    }
                }
            }
        }

        return new OptimizerResult(bestTime, bestOrder);
    }*/

    public OptimizerResult zigzagOptimization(boolean logs, Plane plane) {
        List<Passenger> generatedPassengers = generatePassengers(plane);
        int numColumns = plane.getColumns();
        int columnsPerSide = numColumns / 2;

        List<List<Integer>> allGroups = new ArrayList<>();

        for (int iteration = 0; iteration < columnsPerSide; iteration++) {

            int leftColumn = iteration + 1;
            int rightColumn = numColumns - iteration;

            List<Integer> group1 = new ArrayList<>();
            List<Integer> group2 = new ArrayList<>();

            for (int i = 0; i < generatedPassengers.size(); i++) {
                Passenger passenger = generatedPassengers.get(i);
                String seatLocation = passenger.getSeatLocation();
                String[] parts = seatLocation.split("_");
                int row = Integer.parseInt(parts[0]);
                int column = Integer.parseInt(parts[1]);

                boolean isLeftIterationColumn = (column == leftColumn);
                boolean isRightIterationColumn = (column == rightColumn);

                if (!isLeftIterationColumn && !isRightIterationColumn) {
                    continue;
                }

                if (row % 2 == 1) {

                    if (isRightIterationColumn) {
                        group1.add(i);
                    } else {
                        group2.add(i);
                    }
                } else {
                    if (isLeftIterationColumn) {
                        group1.add(i);
                    } else {
                        group2.add(i);
                    }
                }
            }

            allGroups.add(group1);
            allGroups.add(group2);

        }

        return runOptimization(plane, logs, "Zigzag", "Zigzag Optimization (Iterative alternating sides)", allGroups, generatedPassengers,true);
    }

    public OptimizerResult frontToBackOptimization(boolean logs, Plane plane) {
        List<Passenger> generatedPassengers = generatePassengers(plane);
        int numRows = plane.getRows();

        List<List<Integer>> allGroups = new ArrayList<>();

        for (int i = 0;i<numRows;i++){
            List<Integer> group = new ArrayList<>();
            for (int j = 0;j<generatedPassengers.size();j++){
                Passenger passenger = generatedPassengers.get(j);
                String seatLocation = passenger.getSeatLocation();
                String[] parts = seatLocation.split("_");
                int row = Integer.parseInt(parts[0]);
                if (row == i+1){
                    group.add(j);
                }
            }
            allGroups.add(group);
        }

        return runOptimization(plane, logs, "FrontToBack", "FrontToBack Optimization (Row by row from front to back)", allGroups, generatedPassengers,true);
    }

    public OptimizerResult backToFrontOptimization(boolean logs, Plane plane) {
        List<Passenger> generatedPassengers = generatePassengers(plane);
        int numRows = plane.getRows();

        List<List<Integer>> allGroups = new ArrayList<>();
        for (int i = numRows-1;i>=0;i--){
            List<Integer> group = new ArrayList<>();
            for (int j = 0;j<generatedPassengers.size();j++){
                Passenger passenger = generatedPassengers.get(j);
                String seatLocation = passenger.getSeatLocation();
                String[] parts = seatLocation.split("_");
                int row = Integer.parseInt(parts[0]);
                if (row == i+1){
                    group.add(j);
                }
            }
            allGroups.add(group);
        }
        return runOptimization(plane, logs, "BackToFront", "BackToFront Optimization (Row by row from back to front)", allGroups, generatedPassengers, true);
    }

    public OptimizerResult aisleMiddleWindow(boolean logs, Plane plane) {
        List<Passenger> generatedPassengers = generatePassengers(plane);
        int numColumns = plane.getColumns();
        int centerColumn = numColumns / 2;

        List<List<Integer>> allGroups = new ArrayList<>();
        for (int iteration = 0; iteration < centerColumn; iteration++) {
            int leftColumn = centerColumn - iteration;
            int rightColumn = centerColumn + 1 + iteration;

            List<Integer> leftGroup = new ArrayList<>();
            List<Integer> rightGroup = new ArrayList<>();

            for (int i = 0; i < generatedPassengers.size(); i++) {
                Passenger passenger = generatedPassengers.get(i);
                String seatLocation = passenger.getSeatLocation();
                String[] parts = seatLocation.split("_");
                int row = Integer.parseInt(parts[0]);
                int column = Integer.parseInt(parts[1]);

                if (column == leftColumn) {
                    leftGroup.add(i);
                } else if (column == rightColumn) {
                    rightGroup.add(i);
                }
            }

            if (!leftGroup.isEmpty()) {
                allGroups.add(leftGroup);
            }
            if (!rightGroup.isEmpty()) {
                allGroups.add(rightGroup);
            }
        }

        return runOptimization(plane, logs, "AisleMiddleWindow", "aisleMiddleWindow Optimization", allGroups, generatedPassengers,true);
    }

    private List<Passenger> generatePassengers(Plane plane) {
        return PassengerGenerator.generatePassengers(
                plane.getRows(),
                plane.getColumns(),
                plane.getRows() * plane.getColumns(),
                0
        );
    }

    private OptimizerResult runOptimization(Plane plane, boolean logs, String methodName, String description,
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


    // Modify getTimeForOrder to conditionally save visualization
    private int getTimeForOrder(Plane plane, List<List<Passenger>> passengers, String methodName, boolean saveVisualization) {
        Plane freshPlane = planeFactory.create(plane.getRows(), plane.getColumns());

        List<PassengerGroup> currPassengers = PassengerFactory.createSimulatorPassengers(freshPlane, passengers);
        freshPlane.boardPassengers(currPassengers);

        DeplainingProblem problem = DeplainingProblem.builder()
                .plane(freshPlane)
                .passengers(currPassengers)
                .build();

        SimulatorRequest request = new SimulatorRequest(problem);

        SimulatorResponse response;
        if (saveVisualization) {
            response = simulator.simulate(request);
            xmlservice.saveVisualization(response.visualizationDto(), methodName);
        } else {
            response = simulator.simulateWithoutVisualization(request);
        }

        return response.time();
    }


    private static <T> List<T> permute(List<T> original, List<Integer> scheme) {
        if (original.size() != scheme.size()) {
            throw new IllegalArgumentException("List and scheme must be the same size");
        }
        List<T> result = new ArrayList<>(original);
        for (int i = 0; i < scheme.size(); i++) {
            result.set(i, original.get(scheme.get(i)));
        }
        return result;
    }

    private static List<Integer> swapTwoRand(List<Integer> original) {
        int[] toSwap = new Random()
                .ints(0, original.size())
                .limit(2)
                .toArray();

        List<Integer> result = new ArrayList<>(original);
        result.set(toSwap[0], original.get(toSwap[1]));
        result.set(toSwap[1], original.get(toSwap[0]));

        return result;
    }


    public OptimizerResult geneticAlgorithm(boolean logs, Plane plane, int populationSize, int generations, double mutationRate, double crossoverRate) {
        List<Passenger> generatedPassengers = generatePassengers(plane);
        int totalPassengers = generatedPassengers.size();

        if (logs) {
            log.info("Starting GA with {} passengers", totalPassengers);
        }

        List<List<List<Integer>>> population = initializePopulation(populationSize, totalPassengers);

        for (List<List<Integer>> individual : population) {
            if (!isValidIndividual(individual, totalPassengers)) {
                log.error("Invalid individual in initial population!");
                return new OptimizerResult(Integer.MAX_VALUE, new ArrayList<>());
            }
        }

        int bestTime = Integer.MAX_VALUE;
        List<List<Integer>> bestSolution = null;

        for (int gen = 0; gen < generations; gen++) {
            long genStart = System.currentTimeMillis();
            log.info("Starting generation {}/{}", gen, generations);

            List<IndividualFitness> evaluated = new ArrayList<>();
            for (List<List<Integer>> individual : population) {
                try {
                    int fitness = evaluateFitness(plane, individual, generatedPassengers);
                    evaluated.add(new IndividualFitness(individual, fitness));

                    if (fitness < bestTime) {
                        bestTime = fitness;
                        bestSolution = deepCopyIndices(individual);
                        if (logs) {
                            log.info("Gen {}: New best = {}", gen, bestTime);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error evaluating individual in gen {}: {}", gen, e.getMessage());
                }
            }

            if (evaluated.isEmpty()) {
                log.error("No valid individuals in generation {}", gen);
                break;
            }

            List<List<List<Integer>>> newPopulation = new ArrayList<>();

            if (bestSolution != null) {
                newPopulation.add(deepCopyIndices(bestSolution));
            }

            while (newPopulation.size() < populationSize) {
                List<List<Integer>> parent1 = tournamentSelection(evaluated, 3);
                List<List<Integer>> parent2 = tournamentSelection(evaluated, 3);

                List<List<Integer>> offspring;
                if (Math.random() < crossoverRate) {
                    offspring = orderCrossover(parent1, parent2);
                } else {
                    offspring = deepCopyIndices(parent1);
                }

                if (Math.random() < mutationRate) {
                    mutate(offspring);
                }

                if (isValidIndividual(offspring, totalPassengers)) {
                    newPopulation.add(offspring);
                } else {
                    newPopulation.add(deepCopyIndices(parent1));
                }
            }

            population = newPopulation;

            if (logs && gen % 5 == 0) {
                log.info("Gen {}/{} - Best so far: {}", gen, generations, bestTime);
            }
            long genTime = System.currentTimeMillis() - genStart;
            log.info("Generation {} completed in {} seconds", gen, genTime / 1000.0);
        }

        if (bestSolution == null) {
            log.error("No solution found!");
            return new OptimizerResult(Integer.MAX_VALUE, new ArrayList<>());
        }

        if (logs) {
            log.info("GA completed. Best time: {}", bestTime);
            log.info("Running final simulation WITH visualization...");
        }

        // SAVE VISUALIZATION for the best solution only
        return runOptimization(plane, logs, "GeneticAlgorithm",
                "Genetic Algorithm Optimization", bestSolution, generatedPassengers, true);
    }

    // Validate that individual contains all passengers exactly once
    private boolean isValidIndividual(List<List<Integer>> individual, int totalPassengers) {
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
    private int evaluateFitness(Plane plane, List<List<Integer>> indexGroups, List<Passenger> generatedPassengers) {
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

        SimulatorResponse response = simulator.simulateWithoutVisualization(request);

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("Simulation completed: {} ticks in {} ms", response.time(), elapsed);

        return response.time();
    }




    // Record for fitness tracking
    private record IndividualFitness(List<List<Integer>> individual, int fitness) {}

    // Tournament selection
    private List<List<Integer>> tournamentSelection(List<IndividualFitness> population, int tournamentSize) {
        Random random = new Random();
        IndividualFitness best = null;

        for (int i = 0; i < tournamentSize; i++) {
            IndividualFitness candidate = population.get(random.nextInt(population.size()));
            if (best == null || candidate.fitness < best.fitness) {
                best = candidate;
            }
        }

        return deepCopyIndices(best.individual);
    }

    // Uniform crossover - randomly pick groups from either parent
    private List<List<Integer>> orderCrossover(List<List<Integer>> parent1, List<List<Integer>> parent2) {
        Random random = new Random();

        int totalPassengers = parent1.stream().mapToInt(List::size).sum();
        boolean[] used = new boolean[totalPassengers];
        List<List<Integer>> offspring = new ArrayList<>();

        // Randomly choose to take groups from parent1 or parent2
        List<List<Integer>> source = random.nextBoolean() ? parent1 : parent2;

        // Take groups from chosen parent
        for (List<Integer> group : source) {
            List<Integer> newGroup = new ArrayList<>();
            for (Integer id : group) {
                if (id >= 0 && id < totalPassengers && !used[id]) {
                    newGroup.add(id);
                    used[id] = true;
                }
            }
            if (!newGroup.isEmpty()) {
                offspring.add(newGroup);
            }
        }

        // Fill any missing passengers from the other parent
        List<List<Integer>> otherSource = source == parent1 ? parent2 : parent1;
        for (List<Integer> group : otherSource) {
            List<Integer> newGroup = new ArrayList<>();
            for (Integer id : group) {
                if (id >= 0 && id < totalPassengers && !used[id]) {
                    newGroup.add(id);
                    used[id] = true;
                }
            }
            if (!newGroup.isEmpty()) {
                offspring.add(newGroup);
            }
        }

        return offspring;
    }




    // Mutation - swap two random groups
    // Better mutation - merge or split groups
    private void mutate(List<List<Integer>> individual) {
        if (individual.isEmpty()) return;

        Random random = new Random();
        double mutationType = random.nextDouble();

        if (mutationType < 0.33 && individual.size() > 1) {
            // Merge two random groups
            int idx1 = random.nextInt(individual.size());
            int idx2 = random.nextInt(individual.size());
            if (idx1 != idx2) {
                individual.get(idx1).addAll(individual.get(idx2));
                individual.remove(idx2);
            }
        } else if (mutationType < 0.66 && !individual.isEmpty()) {
            // Split a random group
            int idx = random.nextInt(individual.size());
            List<Integer> group = individual.get(idx);
            if (group.size() > 1) {
                Collections.shuffle(group);
                int splitPoint = group.size() / 2;
                List<Integer> newGroup = new ArrayList<>(group.subList(splitPoint, group.size()));
                individual.set(idx, new ArrayList<>(group.subList(0, splitPoint)));
                individual.add(newGroup);
            }
        } else if (individual.size() > 1) {
            // Swap two groups (original mutation)
            int idx1 = random.nextInt(individual.size());
            int idx2 = random.nextInt(individual.size());
            Collections.swap(individual, idx1, idx2);
        }
    }


    // Deep copy
    private List<List<Integer>> deepCopyIndices(List<List<Integer>> original) {
        return original.stream()
                .map(ArrayList::new)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<List<List<Integer>>> initializePopulation(int populationSize, int numPassengers) {
        List<List<List<Integer>>> population = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < populationSize; i++) {
            List<Integer> allIndices = new ArrayList<>();
            for (int j = 0; j < numPassengers; j++) {
                allIndices.add(j);
            }

            // ONLY shuffle, don't randomly group - groups will be created based on row proximity
            Collections.shuffle(allIndices);

            // Create groups of passengers that are close to each other (better for deplaning)
            int minGroupSize = 2;
            int maxGroupSize = 8; // Smaller groups for better deplaning flow

            List<List<Integer>> individual = new ArrayList<>();
            int idx = 0;

            while (idx < numPassengers) {
                int groupSize = minGroupSize + random.nextInt(maxGroupSize - minGroupSize + 1);
                groupSize = Math.min(groupSize, numPassengers - idx); // Don't exceed remaining passengers

                List<Integer> group = new ArrayList<>(allIndices.subList(idx, idx + groupSize));
                individual.add(group);
                idx += groupSize;
            }

            population.add(individual);
        }

        return population;
    }


    public OptimizerResult simulatedAnnealing(boolean logs, Plane plane, double initialTemp, double coolingRate, int maxIterations) {
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

    // Generate random initial solution
    private List<List<Integer>> generateRandomSolution(int numPassengers, Random random) {
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

    // Generate neighbor solution (perturbation)
    private List<List<Integer>> generateNeighbor(List<List<Integer>> current, Random random) {
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

    public OptimizerResult tabuSearch(boolean logs, Plane plane, int maxIterations, int tabuTenure, int neighborhoodSize) {
        List<Passenger> generatedPassengers = generatePassengers(plane);
        int totalPassengers = generatedPassengers.size();

        if (logs) {
            log.info("Starting Tabu Search with {} passengers", totalPassengers);
            log.info("Max iterations: {}, Tabu tenure: {}, Neighborhood size: {}",
                    maxIterations, tabuTenure, neighborhoodSize);
        }

        Random random = new Random();

        // Generate initial solution
        List<List<Integer>> currentSolution = generateRandomSolution(totalPassengers, random);
        int currentEnergy = evaluateFitness(plane, currentSolution, generatedPassengers);

        // Best solution tracking
        List<List<Integer>> bestSolution = deepCopyIndices(currentSolution);
        int bestEnergy = currentEnergy;

        // Tabu list - stores hash codes of recent solutions
        List<Integer> tabuList = new ArrayList<>();

        int iteration = 0;
        int iterationsWithoutImprovement = 0;
        final int DIVERSIFICATION_THRESHOLD = 50; // Reset if stuck

        if (logs) {
            log.info("Initial solution: {} ticks, {} groups", currentEnergy, currentSolution.size());
        }

        while (iteration < maxIterations) {
            // Generate neighborhood
            List<NeighborSolution> neighbors = new ArrayList<>();

            for (int i = 0; i < neighborhoodSize; i++) {
                List<List<Integer>> neighbor = generateNeighbor(currentSolution, random);

                if (!isValidIndividual(neighbor, totalPassengers)) {
                    continue;
                }

                int neighborHash = computeSolutionHash(neighbor);
                int neighborEnergy = evaluateFitness(plane, neighbor, generatedPassengers);

                neighbors.add(new NeighborSolution(neighbor, neighborEnergy, neighborHash));
            }

            if (neighbors.isEmpty()) {
                log.warn("No valid neighbors generated at iteration {}", iteration);
                iteration++;
                continue;
            }

            // Find best non-tabu neighbor (aspiration criterion: accept if better than best ever)
            NeighborSolution bestNeighbor = null;

            for (NeighborSolution neighbor : neighbors) {
                boolean isTabu = tabuList.contains(neighbor.hash);
                boolean aspirationCriterion = neighbor.energy < bestEnergy; // Override tabu if global best

                if (!isTabu || aspirationCriterion) {
                    if (bestNeighbor == null || neighbor.energy < bestNeighbor.energy) {
                        bestNeighbor = neighbor;
                    }
                }
            }

            // If all neighbors are tabu and none meet aspiration, take least tabu
            if (bestNeighbor == null) {
                bestNeighbor = neighbors.stream()
                        .min((a, b) -> Integer.compare(a.energy, b.energy))
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
            currentSolution = bestNeighbor.solution;
            currentEnergy = bestNeighbor.energy;

            // Update tabu list
            tabuList.add(bestNeighbor.hash);
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

        return runOptimization(plane, logs, "TabuSearch",
                "Tabu Search Optimization", bestSolution, generatedPassengers, true);
    }

    // Compute hash for solution to track in tabu list
    private int computeSolutionHash(List<List<Integer>> solution) {
        // Hash based on group structure (not just passenger order)
        int hash = 0;
        for (int i = 0; i < solution.size(); i++) {
            List<Integer> group = solution.get(i);
            // Weight by position to make order matter
            hash = 31 * hash + (i * 1000 + group.size() * 100 + group.hashCode());
        }
        return hash;
    }

    // Record for neighbor tracking
    private record NeighborSolution(
            List<List<Integer>> solution,
            int energy,
            int hash
    ) {}


}
