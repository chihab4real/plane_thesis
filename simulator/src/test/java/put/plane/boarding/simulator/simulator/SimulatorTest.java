package put.plane.boarding.simulator.simulator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import put.plane.boarding.simulator.SimulatorConfig;
import put.plane.boarding.simulator.passenger.SimulatorPassenger;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.factory.PlaneFactory;
import put.plane.boarding.simulator.problem.DeplainingProblem;
import put.plane.boarding.simulator.problem.PassengerGroup;
import put.plane.boarding.simulator.simulator.factory.TestPassengerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SimulatorConfig.class)
class SimulatorTest {

    @Autowired
    private Simulator simulator;

    @Autowired
    private PlaneFactory planeFactory;

    @Autowired
    private TestPassengerFactory passengerFactory;

    @Test
    void smallNonCollisionSetup() {
        int rows = 5;
        int cols = 4;
        Plane plane = planeFactory.create(rows, cols);

        List<SimulatorPassenger> passengers = List.of(
                passengerFactory.create(plane, 0, 0, 1, 2),
                passengerFactory.create(plane, 1, 0, 1, 2)
        );
        List<PassengerGroup> passengerGroup = List.of(new PassengerGroup(passengers));
        plane.boardPassengers(passengerGroup);

        DeplainingProblem deplainingProblem = DeplainingProblem.builder()
                .plane(plane)
                .passengers(passengerGroup)
                .build();
        SimulatorRequest simulatorRequest = new SimulatorRequest(deplainingProblem);
        SimulatorResponse simulatorResponse = simulator.simulate(simulatorRequest);
        assertEquals(4, simulatorResponse.time());
    }

    @Test
    void smallCollisionSetup() {
        int rows = 5;
        int cols = 4;
        Plane plane = planeFactory.create(rows, cols);

        List<SimulatorPassenger> passengers = List.of(
                passengerFactory.create(plane, 0, 0, 2, 3),
                passengerFactory.create(plane, 0, 1, 2, 3)
        );
        List<PassengerGroup> passengerGroup = List.of(new PassengerGroup(passengers));
        plane.boardPassengers(passengerGroup);

        DeplainingProblem deplainingProblem = DeplainingProblem.builder()
                .plane(plane)
                .passengers(passengerGroup)
                .build();
        SimulatorRequest simulatorRequest = new SimulatorRequest(deplainingProblem);
        SimulatorResponse simulatorResponse = simulator.simulate(simulatorRequest);
        assertEquals(10, simulatorResponse.time());
    }

    @Test
    void bigCollisionSetup() {
        int rows = 5;
        int cols = 4;
        Plane plane = planeFactory.create(rows, cols);

        List<SimulatorPassenger> passengers = List.of(
                passengerFactory.create(plane, 0, 1, 1, 1),
                passengerFactory.create(plane, 0, 2, 2, 2),
                passengerFactory.create(plane, 1, 1, 1, 1),
                passengerFactory.create(plane, 1, 2, 2, 2),
                passengerFactory.create(plane, 2, 1, 1, 1),
                passengerFactory.create(plane, 3, 1, 1, 1),
                passengerFactory.create(plane, 3, 2, 2, 2),
                passengerFactory.create(plane, 2, 2, 2, 2)
        );
        List<PassengerGroup> passengerGroup = List.of(new PassengerGroup(passengers));
        plane.boardPassengers(passengerGroup);

        DeplainingProblem deplainingProblem = DeplainingProblem.builder()
                .plane(plane)
                .passengers(passengerGroup)
                .build();
        SimulatorRequest simulatorRequest = new SimulatorRequest(deplainingProblem);
        SimulatorResponse simulatorResponse = simulator.simulate(simulatorRequest);
        assertEquals(20, simulatorResponse.time());
    }

    @Test
    void differentEnteringTimeSetup() {
        int rows = 5;
        int cols = 4;
        Plane plane = planeFactory.create(rows, cols);

        List<SimulatorPassenger> passengers = List.of(
                passengerFactory.create(plane, 0, 1, 1, 1),
                passengerFactory.create(plane, 0, 2, 1, 2)
        );
        List<PassengerGroup> passengerGroup = List.of(new PassengerGroup(passengers));
        plane.boardPassengers(passengerGroup);

        DeplainingProblem deplainingProblem = DeplainingProblem.builder()
                .plane(plane)
                .passengers(passengerGroup)
                .build();
        SimulatorRequest simulatorRequest = new SimulatorRequest(deplainingProblem);
        SimulatorResponse simulatorResponse = simulator.simulate(simulatorRequest);

        assertEquals(4, simulatorResponse.time());
    }

    @Test
    void moreThanOneDeplainingGroup() {
        int rows = 5;
        int cols = 4;
        Plane plane = planeFactory.create(rows, cols);

        List<SimulatorPassenger> passengers1 = List.of(
                passengerFactory.create(plane, 0, 1, 1, 1),
                passengerFactory.create(plane, 0, 2, 1, 2)
        );
        List<SimulatorPassenger> passengers2 = List.of(
                passengerFactory.create(plane, 0, 0, 2, 3),
                passengerFactory.create(plane, 0, 1, 2, 3)
        );
        List<PassengerGroup> passengerGroups = List.of(new PassengerGroup(passengers1), new PassengerGroup(passengers2));
        plane.boardPassengers(passengerGroups);

        DeplainingProblem deplainingProblem = DeplainingProblem.builder()
                .plane(plane)
                .passengers(passengerGroups)
                .build();
        SimulatorRequest simulatorRequest = new SimulatorRequest(deplainingProblem);
        SimulatorResponse simulatorResponse = simulator.simulate(simulatorRequest);

        assertEquals(14, simulatorResponse.time());
    }
}