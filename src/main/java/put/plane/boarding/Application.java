package put.plane.boarding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import put.plane.boarding.optimizing.Optimizer;
import put.plane.boarding.optimizing.OptimizerResult;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.factory.PlaneFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class Application {

    private final PlaneFactory planeFactory;
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


        log.info("AislemMiddleWindow Optimization\n\n");
        OptimizerResult aisleMiddleWindow = optimizer.aisleMiddleWindow(true, plane);
        log.info("Best time from BackToFront: {}\n\n", aisleMiddleWindow);

    }

}
