package put.plane.boarding.simulator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

@Configuration
@ComponentScan("put.plane.boarding")
public class SimulatorConfig {

    @Bean
    public Random random() {
        return new Random(42);
    }
}
