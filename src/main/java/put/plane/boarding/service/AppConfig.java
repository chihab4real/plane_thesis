package put.plane.boarding.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import put.plane.boarding.PlaneBoardingApplication;
import put.plane.boarding.service.problem.factory.DeplainingProblemFactory;
import put.plane.boarding.service.problem.factory.agent.PassengerFactory;
import put.plane.boarding.service.problem.factory.file.FileFactory;
import put.plane.boarding.service.problem.factory.file.naming.FileNamingStrategy;
import put.plane.boarding.service.problem.factory.file.naming.impl.DefaultFileNamingStrategy;

import java.util.Random;

@Configuration
@ComponentScan("put.plane.boarding")
public class AppConfig {

    @Bean
    public Random random() {
        return new Random(42);
    }
}
