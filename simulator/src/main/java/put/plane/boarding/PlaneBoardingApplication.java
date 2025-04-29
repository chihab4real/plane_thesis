package put.plane.boarding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import put.plane.boarding.simulator.SimulatorConfig;
import put.plane.boarding.simulator.simulator.Simulator;
import put.plane.boarding.simulator.simulator.SimulatorRequest;
import put.plane.boarding.simulator.problem.factory.DeplainingProblemFactory;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaneBoardingApplication {

    private final DeplainingProblemFactory deplainingProblemFactory;

    public static void main(String[] args) {

        var context = new AnnotationConfigApplicationContext(SimulatorConfig.class);
        var application = context.getBean(PlaneBoardingApplication.class);
        application.run();
    }

    public void run() {

        var deplainingProblem = deplainingProblemFactory.create(4, 4, 4);

        deplainingProblem.getPassengers().forEach(passenger -> log.info("PASSENGER: {}", passenger));

        var simulator = new Simulator();
        var request = new SimulatorRequest(deplainingProblem);
        var result = simulator.simulate(request);

        log.info("RESULT: {}", result);
    }
}
