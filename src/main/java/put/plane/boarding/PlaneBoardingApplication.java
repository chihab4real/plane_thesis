package put.plane.boarding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import put.plane.boarding.service.agent.Agent;
import put.plane.boarding.service.agent.Seat;
import put.plane.boarding.service.agent.impl.DefaultAgent;
import put.plane.boarding.service.AppConfig;
import put.plane.boarding.service.orchestrator.Orchestrator;
import put.plane.boarding.service.orchestrator.OrchestratorRequest;
import put.plane.boarding.service.problem.factory.DeplainingProblemFactory;
import put.plane.boarding.service.strategy.impl.DefaultOrderStrategy;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class PlaneBoardingApplication {

    private final DeplainingProblemFactory deplainingProblemFactory;

    public static void main(String[] args) {

        var context = new AnnotationConfigApplicationContext(AppConfig.class);
        var application = context.getBean(PlaneBoardingApplication.class);
        application.run();
    }

    public void run() {

        var deplainingProblem = deplainingProblemFactory.create(16, 4, 4);

        deplainingProblem.getAgents().forEach(agent -> {
            log.info("AGENT: {}", agent);
        });

        var orchestrator = new Orchestrator();

        var request = OrchestratorRequest.builder()
                .problem(deplainingProblem)
                .strategy(new DefaultOrderStrategy())
                .build();

        var result = orchestrator.orchestrate(request);

        log.info("RESULT: {}", result);
    }
}
