package put.plane.boarding;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import put.plane.boarding.simulator.SimulatorConfig;

@Configuration
@ComponentScan("put.plane.boarding")
public class AppConfig extends SimulatorConfig {}