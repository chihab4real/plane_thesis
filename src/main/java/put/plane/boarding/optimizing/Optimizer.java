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
            Collections.reverse(allGroups);

        }

        return runOptimization(plane, logs, "Zigzag", "Zigzag Optimization (Iterative alternating sides)", allGroups, generatedPassengers);
    }

    public OptimizerResult frontToBackOptimization(boolean logs, Plane plane) {
        List<Passenger> generatedPassengers = generatePassengers(plane);
        int numRows = plane.getRows();

        List<List<Integer>> allGroups = new ArrayList<>();

        for (int i = 0; i < numRows; i++) {
            List<Integer> group = new ArrayList<>();
            for (int j = 0; j < generatedPassengers.size(); j++) {
                Passenger passenger = generatedPassengers.get(j);
                String seatLocation = passenger.getSeatLocation();
                String[] parts = seatLocation.split("_");
                int row = Integer.parseInt(parts[0]);
                if (row == i + 1) {
                    group.add(j);
                }
            }
            allGroups.add(group);
        }

        return runOptimization(plane, logs, "FrontToBack", "FrontToBack Optimization (Row by row from front to back)", allGroups, generatedPassengers);
    }

    public OptimizerResult backToFrontOptimization(boolean logs, Plane plane) {
        List<Passenger> generatedPassengers = generatePassengers(plane);
        int numRows = plane.getRows();

        List<List<Integer>> allGroups = new ArrayList<>();
        for (int i = numRows - 1; i >= 0; i--) {
            List<Integer> group = new ArrayList<>();
            for (int j = 0; j < generatedPassengers.size(); j++) {
                Passenger passenger = generatedPassengers.get(j);
                String seatLocation = passenger.getSeatLocation();
                String[] parts = seatLocation.split("_");
                int row = Integer.parseInt(parts[0]);
                if (row == i + 1) {
                    group.add(j);
                }
            }
            allGroups.add(group);
        }
        return runOptimization(plane, logs, "BackToFront", "BackToFront Optimization (Row by row from back to front)", allGroups, generatedPassengers);
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

        return runOptimization(plane, logs, "AisleMiddleWindow", "aisleMiddleWindow Optimization", allGroups, generatedPassengers);
    }

    private List<Passenger> generatePassengers(Plane plane) {
        return PassengerGenerator.generatePassengers(
                plane.getRows(),
                plane.getColumns(),
                plane.getRows() * plane.getColumns(),
                0
        );
    }

    private OptimizerResult runOptimization(Plane plane, boolean logs, String methodName, String description, List<List<Integer>> allGroups, List<Passenger> generatedPassengers) {
        List<List<Passenger>> mappedPassengers = allGroups
                .stream()
                .map(passengers -> passengers
                        .stream()
                        .map(generatedPassengers::get)
                        .toList())
                .collect(Collectors.toCollection(ArrayList::new));

        int time = getTimeForOrder(plane, mappedPassengers, methodName);

        if (logs) {
            log.info(description);
            log.info("Number of iterations: {} (columns per side)", allGroups.size() / 2);
            log.info("Total groups: {}", allGroups.size());
            for (int i = 0; i < allGroups.size(); i++) {
                log.info("Group {} size: {}", i + 1, allGroups.get(i).size());
            }
        }

        return new OptimizerResult(time, new ArrayList<>());
    }

    private int getTimeForOrder(Plane plane, List<List<Passenger>> passengers, String methodName) {

        Plane freshPlane = planeFactory.create(plane.getRows(), plane.getColumns());

        List<PassengerGroup> currPassengers = PassengerFactory.createSimulatorPassengers(freshPlane, passengers);
        freshPlane.boardPassengers(currPassengers);

        DeplainingProblem problem = DeplainingProblem.builder()
                .plane(freshPlane)
                .passengers(currPassengers)
                .build();

        SimulatorRequest request = new SimulatorRequest(problem);
        SimulatorResponse response = simulator.simulate(request);

        xmlservice.saveVisualization(response.visualizationDto(), methodName);

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
