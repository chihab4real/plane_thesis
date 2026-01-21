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

    public OptimizerResult run(boolean logs, Plane plane, List<Passenger> generatedPassengers,String path, int populationSize, int generations, double mutationRate, double crossoverRate) {
        int totalPassengers = generatedPassengers.size();

        if (logs) {
            log.info("Starting GA with {} passengers", totalPassengers);
        }

        List<List<List<Integer>>> population = initializePopulation(populationSize, totalPassengers);

        for (List<List<Integer>> individual : population) {
            if (!isValidIndividual(individual, totalPassengers)) {
//                log.error("Invalid individual in initial population!");
                return new OptimizerResult("", Integer.MAX_VALUE, new ArrayList<>(), new ArrayList<>());
            }
        }

        int bestTime = Integer.MAX_VALUE;
        List<List<Integer>> bestSolution = null;

        long startTime = System.currentTimeMillis();
        long MAX_TIME_MS = 150_000;

        for (int gen = 0; gen < generations  && !shouldStop(startTime, MAX_TIME_MS, gen, generations); gen++) {
            long genStart = System.currentTimeMillis();
            //log.info("Starting generation {}/{}", gen, generations);

            List<IndividualFitness> evaluated = new ArrayList<>();
            for (List<List<Integer>> individual : population) {
                int fitness = evaluateFitness(plane, individual, generatedPassengers);
                evaluated.add(new IndividualFitness(individual, fitness));

                if (fitness < bestTime) {
                    bestTime = fitness;
                    bestSolution = deepCopyIndices(individual);
                    if (logs) {
//                        log.info("Gen {}: New best = {}", gen, bestTime);
                    }
                }
            }

            if (evaluated.isEmpty()) {
//                log.error("No valid individuals in generation {}", gen);
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
        }

        if (bestSolution == null) {
//            log.error("No solution found!");
            return new OptimizerResult("", Integer.MAX_VALUE, new ArrayList<>(), new ArrayList<>());
        }

        if (logs) {
            log.info("GA completed. Best time: {}", bestTime);
            log.info("Running final simulation WITH visualization...");
        }

        // SAVE VISUALIZATION for the best solution only
        return runOptimization(plane, logs, path,"GeneticAlgorithm",
                "Genetic Algorithm Optimization", bestSolution, generatedPassengers, false);
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





}
