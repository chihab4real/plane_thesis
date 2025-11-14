package put.plane.boarding.optimizing;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import put.plane.boarding.passengers.generator.Passenger;
import put.plane.boarding.passengers.generator.PassengerGenerator;
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
public class Optimizer {
    private final Plane plane;

    private List<Integer> order;

    @Getter
    private int bestTime;
    @Getter
    private final List<Integer> bestOrder;

    public Optimizer(Plane plane) {
        this.plane = plane;
        order = IntStream.range(0, plane.getColumns() * plane.getRows()).boxed().collect(Collectors.toList());
        bestOrder = new ArrayList<>(order);
        bestTime = getTimeForOrder(bestOrder, plane);
    }

    public int randomOptimization(int numRepetitions, boolean logs) {
        int bestTime = -1;
        List<Integer> bestOrder = new ArrayList<>(order);

        for (int i = 0; i < numRepetitions; i++) {
            Collections.shuffle(order);

            int time = getTimeForOrder(order, plane);

            if (time < bestTime || bestTime < 0) {
                bestTime = time;
                Collections.copy(bestOrder, order);

                if (time < this.bestTime) {
                    this.bestTime = time;
                    Collections.copy(this.bestOrder, order);
                }

                if (logs) {
                    log.info("TIME({}): {}\nORDER: {}", i, bestTime, bestOrder);
                }
            }
        }

        return bestTime;
    }

    public int pairSwapOptimization(int numRepetitions, int noChangeLimit, boolean logs) {
        int bestTime = -1;

        Collections.shuffle(order);
        List<Integer> lastOrder = new ArrayList<>(order);
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
                }

                if (time < this.bestTime) {
                    this.bestTime = time;
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

        return bestTime;
    }

    private int getTimeForOrder(List<Integer> order, Plane plane) {
        List<Passenger> generatedPassengers = PassengerGenerator.generatePassengers(
                plane.getRows(),
                plane.getColumns(),
                plane.getRows() * plane.getColumns(),
                0
        );
        List<List<Passenger>> passengers = GroupUtils.singleGroup(permute(generatedPassengers, order));
        List<PassengerGroup> currPassengers = PassengerFactory.createSimulatorPassengers(plane, passengers);
        plane.boardPassengers(currPassengers);

        DeplainingProblem problem = DeplainingProblem.builder()
                .plane(plane)
                .passengers(currPassengers)
                .build();

        SimulatorRequest request = new SimulatorRequest(problem);
        SimulatorResponse response = Simulator.simulate(request);

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
}
