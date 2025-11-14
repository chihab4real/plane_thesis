package put.plane.boarding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import put.plane.boarding.optimizing.Optimizer;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.factory.PlaneFactory;

@Slf4j
@Service
@RequiredArgsConstructor
public class Application {

    private final PlaneFactory planeFactory;

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        Application application = context.getBean(Application.class);
        application.run();
    }

    public void run() {
        // creating example problem
        Plane plane = planeFactory.create(16, 6);
        Optimizer optimizer = new Optimizer(plane);

        log.info("Random\n\n");
        // Checking random combinations
        int randResult = optimizer.randomOptimization(100_000, true);
        log.info("Best time from random search: {}\n\n", randResult);

        log.info("Swapping pairs\n\n");
        // Swapping pairs, only if the time is improved
        int swapResult = optimizer.pairSwapOptimization(100_000, 10_000, true);
        log.info("Best time from pair swap: {}\n\n", swapResult);

        log.info("Best time found: {}\nBest order found:\n{}", optimizer.getBestTime(), optimizer.getBestOrder());
    }
}
