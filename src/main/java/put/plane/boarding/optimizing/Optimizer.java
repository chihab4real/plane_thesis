package put.plane.boarding.optimizing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import put.plane.boarding.passengers.generator.Passenger;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.problem.DeplainingProblem;
import put.plane.boarding.simulator.problem.PassengerGroup;
import put.plane.boarding.simulator.problem.factory.passenger.PassengerFactory;
import put.plane.boarding.simulator.simulator.Simulator;
import put.plane.boarding.simulator.simulator.SimulatorRequest;
import put.plane.boarding.simulator.simulator.SimulatorResponse;
import put.plane.boarding.simulator.utils.GroupUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class Optimizer {

    private final Simulator simulator;

    public OptimizerResult randomOptimization(int numRepetitions, boolean logs, Plane plane, List<Passenger> passengers) {
        return randomOptimizationBatch(numRepetitions, logs, plane, List.of(passengers));
    }
    public OptimizerResult randomOptimizationBatch(int numRepetitions, boolean logs, Plane plane, List<List<Passenger>> passengers) {
        List<Integer> order = IntStream.range(0, plane.getColumns() * plane.getRows()).boxed().collect(Collectors.toList());

        float bestTime = -1;
        List<Integer> bestOrder = new ArrayList<>(order);

        for (int i = 0; i < numRepetitions; i++) {
            Collections.shuffle(order);

            float time = getTimeForOrders(order, plane, passengers);

            if (time < bestTime || bestTime < 0) {
                bestTime = time;
                Collections.copy(bestOrder, order);

                if (logs) {
                    log.info("TIME({}): {}\nORDER: {}", i, bestTime, bestOrder);
                }
            }
        }

        return new OptimizerResult(bestTime, bestOrder);
    }

    public OptimizerResult pairSwapOptimization(int numRepetitions, int noChangeLimit, boolean logs, Plane plane, List<Passenger> passengers) {
        return pairSwapOptimizationBatch(numRepetitions, noChangeLimit, logs, plane, List.of(passengers));
    }

    public OptimizerResult pairSwapOptimizationBatch(int numRepetitions, int noChangeLimit, boolean logs, Plane plane, List<List<Passenger>> passengers) {
        List<Integer> order = IntStream.range(0, plane.getColumns() * plane.getRows()).boxed().collect(Collectors.toList());

        float bestTime = -1;

        Collections.shuffle(order);
        List<Integer> lastOrder = new ArrayList<>(order);
        List<Integer> bestOrder = new ArrayList<>(lastOrder);
        Collections.copy(lastOrder, order);
        float lastTime = getTimeForOrders(lastOrder, plane, passengers);

        int noChange = 0;
        for (int i = 0; i < numRepetitions; i++) {
            order = swapTwoRand(lastOrder);
            float time = getTimeForOrders(order, plane, passengers);

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
                    lastTime = getTimeForOrders(lastOrder, plane, passengers);
                    if (logs) {
                        log.info("Started from random point\nTIME({}): {}\nORDER: {}", i, lastTime, lastOrder);
                    }
                }
            }
        }

        return new OptimizerResult(bestTime, bestOrder);
    }

    private float getTimeForOrder(List<Integer> order, Plane plane, List<Passenger> passengers) {
        return getTimeForOrders(order, plane, List.of(passengers));
    }

    private float getTimeForOrders(List<Integer> order, Plane plane, List<List<Passenger>> passengersLists) {
        float timeSum = 0;

        for(List<Passenger> generatedPassengers: passengersLists) {
            List<List<Passenger>> passengers = GroupUtils.singleGroup(permute(generatedPassengers, order));
            List<PassengerGroup> currPassengers = PassengerFactory.createSimulatorPassengers(plane, passengers);
            plane.boardPassengers(currPassengers);

            DeplainingProblem problem = DeplainingProblem.builder()
                    .plane(plane)
                    .passengers(currPassengers)
                    .build();

            SimulatorRequest request = new SimulatorRequest(problem);
            SimulatorResponse response = simulator.simulate(request);

            timeSum += response.time();
        }

        return timeSum / passengersLists.size();
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
}
