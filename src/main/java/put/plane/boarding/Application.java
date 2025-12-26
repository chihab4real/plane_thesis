package put.plane.boarding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import put.plane.boarding.optimizing.Optimizer;
import put.plane.boarding.optimizing.OptimizerResult;
import put.plane.boarding.optimizing.optimizers.advanced.GeneticAlgorithmOptimizer;
import put.plane.boarding.optimizing.optimizers.advanced.SimulatedAnnealingOptimizer;
import put.plane.boarding.optimizing.optimizers.advanced.TabuSearchOptimizer;
import put.plane.boarding.optimizing.optimizers.basic.AisleMiddleWindowOptimizer;
import put.plane.boarding.optimizing.optimizers.basic.BackToFrontOprimizer;
import put.plane.boarding.optimizing.optimizers.basic.FrontToBackOptimizer;
import put.plane.boarding.optimizing.optimizers.basic.ZigZagOptimizer;
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
    private final List<Optimizer> basicOptimizers;

    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        Application application = context.getBean(Application.class);
        application.run();
    }

    public void run() {
        // creating example problem
        Plane plane = planeFactory.create(16, 6);
        List<Integer> order = IntStream.range(0, plane.getColumns() * plane.getRows()).boxed().collect(Collectors.toList());

        basicOptimizers.add(new ZigZagOptimizer(new Simulator(), xmlService, planeFactory));
        basicOptimizers.add(new BackToFrontOprimizer(new Simulator(), xmlService, planeFactory));
        basicOptimizers.add(new FrontToBackOptimizer(new Simulator(), xmlService, planeFactory));
        basicOptimizers.add(new AisleMiddleWindowOptimizer(new Simulator(), xmlService, planeFactory));

        log.info("Running Basic Optimizers\n");
        for(Optimizer optimizer : basicOptimizers){
            log.info("Running optimizer: {}\n", optimizer.getClass().getSimpleName());
            OptimizerResult result = optimizer.run(plane, true);
            log.info("Result: {}\n\n", result);
        }

        log.info("Running Advanced Optimizers\n");

        log.info("Genetic Algorithm Optimization\n");
        GeneticAlgorithmOptimizer optimizer = new GeneticAlgorithmOptimizer(new Simulator(), xmlService, planeFactory);
        OptimizerResult gaResult = optimizer.run(true, plane, 50, 20,
                0.2, 0.8);
        log.info("Best time from GA: {}\n\n", gaResult);

        log.info("Simulated Annealing Optimization\n");
        SimulatedAnnealingOptimizer saOptimizer = new SimulatedAnnealingOptimizer(new Simulator(), xmlService, planeFactory);
        OptimizerResult saResult = saOptimizer.run(true, plane,100.0, 0.995, 1000);
        log.info("Best time from SA: {}\n\n", saResult);

        log.info("Tabu Search Optimization\n");
        TabuSearchOptimizer tsOptimizer = new TabuSearchOptimizer(new Simulator(), xmlService, planeFactory);
        OptimizerResult tsResult = tsOptimizer.run(true, plane, 500, 15, 20);
        log.info("Best time from TS: {}\n\n", tsResult);
    }


}
