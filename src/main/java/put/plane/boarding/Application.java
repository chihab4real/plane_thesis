package put.plane.boarding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import put.plane.boarding.passengers.generator.Passenger;
import put.plane.boarding.passengers.generator.PassengerGenerator;
import put.plane.boarding.simulator.passenger.SimulatorPassenger;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.factory.PlaneFactory;
import put.plane.boarding.simulator.problem.DeplainingProblem;
import put.plane.boarding.simulator.simulator.Simulator;
import put.plane.boarding.simulator.simulator.SimulatorRequest;
import put.plane.boarding.simulator.simulator.SimulatorResponse;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static put.plane.boarding.simulator.problem.factory.passenger.PassengerFactory.createSimulatorPassenger;

@Slf4j
@Service
@RequiredArgsConstructor
public class Application {

    private final Simulator simulator;
    private final PlaneFactory planeFactory;
    private final Random random;

    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        Application application = context.getBean(Application.class);
        application.run();
    }

    public void run() {
        // creating example problem
        int rows = 16;
        int columns = 6;
        Plane plane = planeFactory.create(rows, columns);

        int numberOfPassengers = rows * columns;

        List<Integer> order = IntStream.range(0, numberOfPassengers).boxed().collect(Collectors.toList());

        int bestTime = -1;
        List<Integer> bestOrder = new ArrayList<>(order);

        // Checking random combinations
        for (int i = 0; i < 100_000; i++) {
            Collections.shuffle(order);

            int time = getTimeForOrder(order, plane);

            if (time < bestTime || bestTime < 0) {
                bestTime = time;
                Collections.copy(bestOrder, order);

                log.info("TIME({}): {}\nORDER: {}", i, bestTime, bestOrder);
            }
        }

        log.info("\n\nSwapping pairs");
        // Swapping pairs, only if the time is improved
        List<Integer> lastOrder = IntStream.range(0, rows * columns).boxed().collect(Collectors.toList());
        int lastTime = getTimeForOrder(lastOrder, plane);

        int noChange = 0;

        for (int i = 0; i < 100_000; i++) {
            order = swapTwoRand(lastOrder);
            int time = getTimeForOrder(order, plane);

            if (time < lastTime) {
                Collections.copy(lastOrder, order);
                lastTime = time;
                log.info("TIME({}): {}\nORDER: {}", i, lastTime, lastOrder);
            }
            else {
                noChange++;

                if (noChange >= 10_000) {
                    noChange = 0;

                    Collections.shuffle(lastOrder);
                    lastTime = getTimeForOrder(lastOrder, plane);
                    log.info("Started from random point\nTIME({}): {}\nORDER: {}", i, lastTime, lastOrder);
                }
            }
        }
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
        int[] toSwap = random
                .ints(0, original.size())
                .limit(2)
                .toArray();

        List<Integer> result = new ArrayList<>(original);
        result.set(toSwap[0], original.get(toSwap[1]));
        result.set(toSwap[1], original.get(toSwap[0]));

        return result;
    }

    public int getTimeForOrder(List<Integer> order, Plane plane) {
        List<Passenger> generatedPassengers = PassengerGenerator.generatePassengers(plane.getRows(), plane.getColumns(), plane.getRows() * plane.getColumns(), 0);
        List<SimulatorPassenger> currPassengers = createSimulatorPassenger(plane, permute(generatedPassengers, order));
        plane.boardPassengers(currPassengers);

        DeplainingProblem problem = DeplainingProblem.builder()
                .plane(plane)
                .passengers(currPassengers)
                .build();

        SimulatorRequest request = new SimulatorRequest(problem);
        SimulatorResponse response = simulator.simulate(request);

        return response.time();
    }
}
