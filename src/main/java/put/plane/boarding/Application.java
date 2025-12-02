package put.plane.boarding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import put.plane.boarding.optimizing.Optimizer;
import put.plane.boarding.optimizing.OptimizerResult;
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
import put.plane.boarding.simulator.utils.GroupUtils;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class Application {

    private final PlaneFactory planeFactory;
    private final Random random;
    private final XMLService xmlService;
    private final Optimizer optimizer;

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        Application application = context.getBean(Application.class);
        application.run();
    }

    public void run() {
        // creating example problem
        Plane plane = planeFactory.create(16, 6);
        List<Passenger> generatedPassengers = PassengerGenerator.generatePassengers(
                plane.getRows(),
                plane.getColumns(),
                plane.getRows() * plane.getColumns(),
                0
        );

        log.info("Random\n\n");
        // Checking random combinations
        OptimizerResult randResult = optimizer.randomOptimization(
                10_000, true, plane, generatedPassengers);
        float bestTime = randResult.bestTime();
        List<Integer> bestOrder = randResult.bestSolution();

        log.info("Swapping pairs\n\n");
        // Swapping pairs, only if the time is improved
        OptimizerResult swapResult = optimizer.pairSwapOptimization(
                10_000, 1000, true, plane, generatedPassengers);
        if (swapResult.bestTime() < bestTime) {
            bestTime = swapResult.bestTime();
            bestOrder = swapResult.bestSolution();
        }

        log.info("Best time from random search: {}\n\n", randResult);
        log.info("Best time from pair swap: {}\n\n", swapResult);
        log.info("Best time found: {}\nBest order found:\n{}", bestTime, bestOrder);
    }
}
