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
    private final Simulator simulator;

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
        OptimizerResult zigzagResult = optimizer.zigzagOptimization(true, order, plane);
        log.info("Best time from zigzag: {}\n\n", zigzagResult);

        log.info("FrontToBack Optimization\n\n");
        OptimizerResult frontToBack = optimizer.frontToBackOptimization(true, order, plane);
        log.info("Best time from frontToBack: {}\n\n", frontToBack);


        log.info("BackToFront Optimization\n\n");
        OptimizerResult backToFront = optimizer.backToFrontOptimization(true, order, plane);
        log.info("Best time from BackToFront: {}\n\n", backToFront);


        log.info("AislemMiddleWindow Optimization\n\n");
        OptimizerResult aisleMiddleWindow = optimizer.aisleMiddleWindow(true, order, plane);
        log.info("Best time from BackToFront: {}\n\n", aisleMiddleWindow);

    }

}
