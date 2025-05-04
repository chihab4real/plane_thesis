package put.plane.boarding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import put.plane.boarding.simulator.passenger.PassengerDecorator;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.factory.PlaneFactory;
import put.plane.boarding.simulator.problem.DeplainingProblem;
import put.plane.boarding.simulator.problem.factory.agent.PassengerFactory;
import put.plane.boarding.simulator.simulator.Simulator;
import put.plane.boarding.simulator.simulator.SimulatorRequest;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static put.plane.boarding.simulator.utils.PairUtils.generateUniquePairs;
import java.util.ArrayList;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class Application {

    private final Simulator simulator;
    private final PassengerFactory passengerFactory;
    private final PlaneFactory planeFactory;

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
//        var initial_passengers = createPassengers(plane);

        ArrayList<Integer> order = new ArrayList<Integer>(IntStream.range(0, rows * columns).boxed().toList());

        int best_time = -1;
        ArrayList<Integer> best_order = order;

        // Checking random combinations
        for (int i = 0; i < 100000; i++) {
            Collections.shuffle(order);

            int time = getTimeForOrder(order, plane);

            if (time < best_time || best_time < 0) {
                best_time = time;
                best_order = order;

                log.info("TIME({}): {}\nORDER: {}", i, time, order);
            }
        }

        log.info("\n\nSwapping pairs");
        // Swapping pairs, only if the time is improved
        ArrayList<Integer> last_order = new ArrayList<Integer>(IntStream.range(0, rows * columns).boxed().toList());
        int last_time = getTimeForOrder(last_order, plane);

        int no_change = 0;

        for (int i = 0; i < 100000; i++) {
            order = swapTwoRand(last_order);
            int time = getTimeForOrder(order, plane);

            if (time < last_time) {
                last_order = order;
                last_time = time;
                log.info("TIME({}): {}\nORDER: {}", i, last_time, last_order);
            }
            else {
                no_change++;

                if (no_change >= 10000) {
                    no_change = 0;

                    Collections.shuffle(last_order);
                    last_time = getTimeForOrder(last_order, plane);
                    log.info("Started from random point\nTIME({}): {}\nORDER: {}", i, last_time, last_order);
                }
            }
        }
    }

    public List<PassengerDecorator> createPassengers(Plane plane) {

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

    public static ArrayList<Integer> swapTwoRand(List<Integer> original) {
        int[] to_swap = ThreadLocalRandom.current().ints(0, original.size()).limit(2).toArray();

        ArrayList<Integer> result = new ArrayList<>(original);
        result.set(to_swap[0], original.get(to_swap[1]));
        result.set(to_swap[1], original.get(to_swap[0]));

        return result;
    }

    public int getTimeForOrder(List<Integer> order, Plane plane) {
        var curr_passengers = permute(createPassengers(plane), order);

        var problem = DeplainingProblem.builder()
                .plane(plane)
                .passengers(curr_passengers)
                .build();

        var request = new SimulatorRequest(problem);
        var response = simulator.simulate(request);

        return response.getTime();
    }
}
