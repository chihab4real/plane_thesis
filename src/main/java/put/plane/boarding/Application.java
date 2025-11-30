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

        log.info("Random\n\n");
        // Checking random combinations
        OptimizerResult randResult = optimizer.randomOptimization(100_000, true, order, plane);
        int bestTime = randResult.bestTime();
        List<Integer> bestOrder = randResult.bestSolution();
        log.info("Best time from random search: {}\n\n", randResult);

        log.info("Swapping pairs\n\n");
        // Swapping pairs, only if the time is improved
        OptimizerResult swapResult = optimizer.pairSwapOptimization(100_000, 10_000, true, order, plane);
        if (swapResult.bestTime() < bestTime) {
            bestTime = swapResult.bestTime();
            bestOrder = swapResult.bestSolution();
        }

        log.info("Best time from pair swap: {}\n\n", swapResult);

        log.info("Best time found: {}\nBest order found:\n{}", bestTime, bestOrder);
    }
}
