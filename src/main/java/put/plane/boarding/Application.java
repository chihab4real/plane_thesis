package put.plane.boarding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import put.plane.boarding.simulator.passenger.Passenger;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.factory.PlaneFactory;
import put.plane.boarding.simulator.problem.DeplainingProblem;
import put.plane.boarding.simulator.problem.factory.passenger.PassengerFactory;
import put.plane.boarding.simulator.simulator.Simulator;
import put.plane.boarding.simulator.simulator.SimulatorRequest;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static put.plane.boarding.simulator.utils.PairUtils.generateUniquePairs;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class Application {

    private final Simulator simulator;
    private final PassengerFactory passengerFactory;
    private final PlaneFactory planeFactory;
    private final Random random;

    public static void main(String[] args) {

        // TODO: create passenger generator - Patrick
        // TODO: create UML diagram of work - Marcin
        // TODO: simulator - create tests - Chichab

        var context = new AnnotationConfigApplicationContext(AppConfig.class);
        var application = context.getBean(Application.class);
        application.run();
    }

    public void run() {
        // creating example problem
        int rows = 16;
        int columns = 6;
        var plane = planeFactory.create(rows, columns);

        int numberOfPassengers = rows * columns;

        List<Integer> order = IntStream.range(0, numberOfPassengers).boxed().collect(Collectors.toList());

        int best_time = -1;
        List<Integer> best_order = new ArrayList<>(order);

        // Checking random combinations
        for (int i = 0; i < 100_000; i++) {
            Collections.shuffle(order);

            int time = getTimeForOrder(order, plane);

            if (time < best_time || best_time < 0) {
                best_time = time;
                Collections.copy(best_order, order);

                log.info("TIME({}): {}\nORDER: {}", i, best_time, best_order);
            }
        }

        log.info("\n\nSwapping pairs");
        // Swapping pairs, only if the time is improved
        List<Integer> last_order = IntStream.range(0, rows * columns).boxed().collect(Collectors.toList());
        int last_time = getTimeForOrder(last_order, plane);

        int no_change = 0;

        for (int i = 0; i < 100_000; i++) {
            order = swapTwoRand(last_order);
            int time = getTimeForOrder(order, plane);

            if (time < last_time) {
                Collections.copy(last_order, order);
                last_time = time;
                log.info("TIME({}): {}\nORDER: {}", i, last_time, last_order);
            }
            else {
                no_change++;

                if (no_change >= 10_000) {
                    no_change = 0;

                    Collections.shuffle(last_order);
                    last_time = getTimeForOrder(last_order, plane);
                    log.info("Started from random point\nTIME({}): {}\nORDER: {}", i, last_time, last_order);
                }
            }
        }
    }

    public List<Passenger> createPassengers(Plane plane) {

        // Random passengers - example
        var seats = generateUniquePairs(plane.getRows() * plane.getColumns(), plane.getRows(), plane.getColumns());
        return seats.stream()
                .map(seat -> passengerFactory.create(plane, seat.getLeft(), seat.getRight()))
                .toList();
    }

    public static <T> List<T> permute(List<T> original, List<Integer> scheme) {
        if (original.size() != scheme.size()) {
            throw new IllegalArgumentException("List and scheme must be the same size");
        }
        List<T> result = new ArrayList<>(original);
        for (int i = 0; i < scheme.size(); i++) {
            result.set(i, original.get(scheme.get(i)));
        }
        return result;
    }

    public List<Integer> swapTwoRand(List<Integer> original) {
        int[] to_swap = random
                .ints(0, original.size())
                .limit(2)
                .toArray();

        List<Integer> result = new ArrayList<>(original);
        result.set(to_swap[0], original.get(to_swap[1]));
        result.set(to_swap[1], original.get(to_swap[0]));

        return result;
    }

    public int getTimeForOrder(List<Integer> order, Plane plane) {
        var currPassengers = permute(createPassengers(plane), order);
        plane.boardPassengers(currPassengers);

        var problem = DeplainingProblem.builder()
                .plane(plane)
                .passengers(currPassengers)
                .build();

        var request = new SimulatorRequest(problem);
        var response = simulator.simulate(request);

        return response.time();
    }
}
