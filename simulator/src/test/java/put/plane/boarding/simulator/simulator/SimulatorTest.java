package put.plane.boarding.simulator.simulator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import put.plane.boarding.simulator.SimulatorConfig;
import put.plane.boarding.simulator.plane.factory.PlaneFactory;
import put.plane.boarding.simulator.problem.DeplainingProblem;
import put.plane.boarding.simulator.problem.factory.passenger.PassengerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SimulatorConfig.class)
public class SimulatorTest {

    @Autowired
    private Simulator simulator;

    @Autowired
    private PlaneFactory planeFactory;

    @Autowired
    private PassengerFactory passengerFactory;

    @Test
    public void smallNonCollisionSetup() {
        int rows = 5;
        int cols = 4;
        var plane = planeFactory.create(rows, cols);

        var passenger1 = passengerFactory.create(plane, 0, 0);
        var passenger2 = passengerFactory.create(plane, 1, 0);

        var passengers = List.of(passenger1, passenger2);
        plane.boardPassengers(passengers);

        var deplainingProblem = DeplainingProblem.builder()
                .plane(plane)
                .passengers(passengers)
                .build();
        var simulatorRequest = new SimulatorRequest(deplainingProblem);
        var simulatorResponse = simulator.simulate(simulatorRequest);
        assertEquals(3, simulatorResponse.time());
    }

    @Test
    public void smallCollisionSetup() {
        int rows = 5;
        int cols = 4;
        var plane = planeFactory.create(rows, cols);

        var passenger1 = passengerFactory.create(plane, 0, 0);
        var passenger2 = passengerFactory.create(plane, 0, 1);

        var passengers = List.of(passenger1, passenger2);
        plane.boardPassengers(passengers);

        var deplainingProblem = DeplainingProblem.builder()
                .plane(plane)
                .passengers(passengers)
                .build();
        var simulatorRequest = new SimulatorRequest(deplainingProblem);
        var simulatorResponse = simulator.simulate(simulatorRequest);
        assertEquals(4, simulatorResponse.time());
    }
}