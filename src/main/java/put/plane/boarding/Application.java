package put.plane.boarding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import put.plane.boarding.optimizing.Optimizer;
import put.plane.boarding.optimizing.OptimizerResult;
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

import java.util.ArrayList;
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
        List<Integer> order = IntStream.range(0, plane.getColumns() * plane.getRows()).boxed().collect(Collectors.toList());

        log.info("Zigzag Optimization\n\n");
        OptimizerResult zigzagResult = optimizer.zigzagOptimization(true, plane);
        log.info("Best time from zigzag: {}\n\n", zigzagResult);

        log.info("FrontToBack Optimization\n\n");
        OptimizerResult frontToBack = optimizer.frontToBackOptimization(true, plane);
        log.info("Best time from frontToBack: {}\n\n", frontToBack);


        log.info("BackToFront Optimization\n\n");
        OptimizerResult backToFront = optimizer.backToFrontOptimization(true, plane);
        log.info("Best time from BackToFront: {}\n\n", backToFront);


        log.info("AisleMiddleWindow Optimization\n\n");
        OptimizerResult aisleMiddleWindow = optimizer.aisleMiddleWindow(true, plane);
        log.info("Best time from BackToFront: {}\n\n", aisleMiddleWindow);

//
//        log.info("Genetic Algorithm Optimization\n");
//        OptimizerResult gaResult = optimizer.geneticAlgorithm(
//            true,      // logs
//            plane,     // plane
//            50,        // population size
//            20,       // generations
//            0.2,       // mutation rate
//            0.8        // crossover rate
//        );
//        log.info("Best time from GA: {}\n\n", gaResult);

//        log.info("Simulated Annealing Optimization\n");
//        OptimizerResult saResult = optimizer.simulatedAnnealing(
//                true,      // logs
//                plane,     // plane
//                100.0,     // initial temperature
//                0.995,     // cooling rate (0.99-0.999)
//                1000       // max iterations
//        );
//        log.info("Best time from SA: {}\n\n", saResult);

        // Run Tabu Search
//        log.info("Tabu Search Optimization\n");
//        OptimizerResult tsResult = optimizer.tabuSearch(
//                true,      // logs
//                plane,     // plane
//                500,       // max iterations
//                15,        // tabu tenure (memory length)
//                20         // neighborhood size
//        );
//        log.info("Best time from TS: {}\n\n", tsResult);

    }


}
