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
public class GeneticAlgorithmOptimizer extends AdvancedOptimizer {

    public OptimizerResult run(boolean logs, Plane plane, List<Passenger> generatedPassengers, String path,
                               int populationSize, int generations, double mutationRate, double crossoverRate) {

        long start = System.currentTimeMillis();
        int totalPassengers = generatedPassengers.size();

        int totalCallsToSimulator = 0;
        int whichIterationBestWasFound = -1;
        if (logs) {
            log.info("Starting GA with {} passengers", totalPassengers);
        }

        List<List<List<Integer>>> population = initializePopulation(populationSize, totalPassengers);

        for (List<List<Integer>> individual : population) {
            if (!isValidIndividual(individual, totalPassengers)) {
                return new OptimizerResult("", Integer.MAX_VALUE, new ArrayList<>(), new ArrayList<>(), new java.util.HashMap<>(), totalCallsToSimulator, whichIterationBestWasFound);
            }
        }

        int bestTime = Integer.MAX_VALUE;
        List<List<Integer>> bestSolution = null;


        List<Integer> fitnessHistory = new ArrayList<>();
        List<Double> diversityHistory = new ArrayList<>();
        int iterationOfBest = 0;
        fitnessHistory.add(bestTime);


        long startTime = System.currentTimeMillis();
        long MAX_TIME_MS = 150_000;

        for (int gen = 0; gen < generations && !shouldStop(startTime, MAX_TIME_MS, gen, generations); gen++) {
            long genStart = System.currentTimeMillis();

            List<IndividualFitness> evaluated = new ArrayList<>();
            for (List<List<Integer>> individual : population) {
                int fitness = evaluateFitness(plane, individual, generatedPassengers);
                totalCallsToSimulator++;
                evaluated.add(new IndividualFitness(individual, fitness));

                if (fitness < bestTime) {
                    bestTime = fitness;
                    whichIterationBestWasFound = totalCallsToSimulator;
                    bestSolution = deepCopyIndices(individual);
                    iterationOfBest = gen;
                }

            }
            fitnessHistory.add(bestTime);

            if (evaluated.isEmpty()) {
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

            double diversity = calculatePopulationDiversity(evaluated);
            diversityHistory.add(diversity);

            if (logs && gen % 5 == 0) {
                log.info("Gen {}/{} - Best so far: {}", gen, generations, bestTime);
            }
        }

        if (bestSolution == null) {
            return new OptimizerResult("", Integer.MAX_VALUE, new ArrayList<>(), new ArrayList<>(), new java.util.HashMap<>(), totalCallsToSimulator);
        }

        if (logs) {
            log.info("GA completed. Best time: {}", bestTime);
            log.info("Running final simulation WITH visualization...");
        }
        long optimizationDuration = System.currentTimeMillis() - start;

        OptimizerResult result = runOptimization(plane, logs, path, "GeneticAlgorithm",
                "Genetic Algorithm Optimization", bestSolution, generatedPassengers, false, totalCallsToSimulator);

        result.setOptimizationDurationMillis(optimizationDuration);
        result.setFitnessHistory(fitnessHistory);
        result.setIterationOfBestSolution(iterationOfBest);
        result.setDiversityHistory(diversityHistory);

        return result;
    }


    private List<List<List<Integer>>> initializePopulation(int populationSize, int numPassengers) {
        List<List<List<Integer>>> population = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < populationSize; i++) {
            population.add(createRandomGrouping(numPassengers, random));
        }

        return population;
    }



    private List<List<Integer>> tournamentSelection(List<IndividualFitness> population, int tournamentSize) {
        if (population.isEmpty() || tournamentSize <= 0) {
            throw new IllegalArgumentException("Population cannot be empty and tournament size must be positive");
        }

        Random random = new Random();
        IndividualFitness best = null;

        for (int i = 0; i < tournamentSize; i++) {
            IndividualFitness candidate = population.get(random.nextInt(population.size()));
            if (best == null || candidate.fitness() < best.fitness()) {
                best = candidate;
            }
        }

        return deepCopyIndices(best.individual());
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
        addUnusedPassengersToOffspring(source, offspring, used, totalPassengers);

        // Fill any missing passengers from the other parent
        List<List<Integer>> otherSource = source == parent1 ? parent2 : parent1;
        addUnusedPassengersToOffspring(otherSource, offspring, used, totalPassengers);

        return offspring;
    }

    private void addUnusedPassengersToOffspring(
        List<List<Integer>> source,
        List<List<Integer>> offspring,
        boolean[] used,
        int totalPassengers
    ) {
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
        } else if (individual.size() > 2) {
            // Reverse a random subsequence of groups
            int start = random.nextInt(individual.size() - 1);
            int end = start + 1 + random.nextInt(individual.size() - start - 1);
            Collections.reverse(individual.subList(start, end + 1));
        }
    }

    private double calculatePopulationDiversity(List<IndividualFitness> population) {
        if (population.size() < 2) return 0.0;

        double sumDistances = 0.0;
        int comparisons = 0;

        for (int i = 0; i < Math.min(10, population.size()); i++) {
            for (int j = i + 1; j < Math.min(10, population.size()); j++) {
                sumDistances += calculateHammingDistance(
                        population.get(i).individual(),
                        population.get(j).individual()
                );
                comparisons++;
            }
        }

        return comparisons > 0 ? sumDistances / comparisons : 0.0;
    }

    private double calculateHammingDistance(List<List<Integer>> sol1, List<List<Integer>> sol2) {
        List<Integer> flat1 = sol1.stream().flatMap(List::stream).collect(Collectors.toList());
        List<Integer> flat2 = sol2.stream().flatMap(List::stream).collect(Collectors.toList());

        int differences = 0;
        int minSize = Math.min(flat1.size(), flat2.size());

        for (int i = 0; i < minSize; i++) {
            if (!flat1.get(i).equals(flat2.get(i))) {
                differences++;
            }
        }

        return minSize > 0 ? (double) differences / minSize : 0.0;
    }






}
