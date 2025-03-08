package put.plane.boarding.service;

import org.springframework.context.annotation.Bean;
import put.plane.boarding.PlaneBoardingApplication;
import put.plane.boarding.service.problem.factory.DeplainingProblemFactory;
import put.plane.boarding.service.problem.factory.agent.AgentFactory;
import put.plane.boarding.service.problem.factory.file.FileFactory;
import put.plane.boarding.service.problem.factory.file.naming.FileNamingStrategy;
import put.plane.boarding.service.problem.factory.file.naming.impl.DefaultFileNamingStrategy;
import put.plane.boarding.service.strategy.OrderStrategy;
import put.plane.boarding.service.strategy.impl.DefaultOrderStrategy;

import java.util.Random;

public class AppConfig {

    @Bean
    public FileNamingStrategy fileNamingStrategy() {
        return new DefaultFileNamingStrategy();
    }

    @Bean
    public FileFactory fileFactory(FileNamingStrategy fileNamingStrategy) {
        return new FileFactory(fileNamingStrategy);
    }

    @Bean
    public AgentFactory agentFactory() {
        return new AgentFactory();
    }

    @Bean
    public DeplainingProblemFactory deplainingProblemFactory(AgentFactory agentFactory, FileFactory fileFactory) {
        return new DeplainingProblemFactory(fileFactory, agentFactory);
    }

    @Bean
    public PlaneBoardingApplication planeBoardingApplication(DeplainingProblemFactory deplainingProblemFactory) {
        return new PlaneBoardingApplication(deplainingProblemFactory);
    }
}
