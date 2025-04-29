package put.plane.boarding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import put.plane.boarding.service.AppConfig;
import put.plane.boarding.service.orchestrator.Orchestrator;
import put.plane.boarding.service.orchestrator.OrchestratorRequest;
import put.plane.boarding.service.problem.factory.DeplainingProblemFactory;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaneBoardingApplication {

    private final DeplainingProblemFactory deplainingProblemFactory;

    public static void main(String[] args) {

        var context = new AnnotationConfigApplicationContext(AppConfig.class);
        var application = context.getBean(PlaneBoardingApplication.class);
        application.run();
    }

    public void run() {

        var deplainingProblem = deplainingProblemFactory.create(4, 4, 4);

        deplainingProblem.getPassengers().forEach(passenger -> log.info("PASSENGER: {}", passenger));

        var orchestrator = new Orchestrator();
        var request = new OrchestratorRequest(deplainingProblem);
        var result = orchestrator.orchestrate(request);

        log.info("RESULT: {}", result);
    }
}
