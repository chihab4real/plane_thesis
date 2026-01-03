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
import put.plane.boarding.simulator.simulator.frame.XMLService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SimulatorConfig.class)
class SimulatorTest {

    @Autowired
    private Simulator simulator;

    @Autowired
    private PlaneFactory planeFactory;

    @Autowired
    private TestPassengerFactory passengerFactory;

    @Autowired
    private XMLService xmlService;

    @Test
    void smallNonCollisionSetup() {
        int rows = 5;
        int cols = 4;
        Plane plane = planeFactory.create(rows, cols);

        List<SimulatorPassenger> passengers = List.of(
                passengerFactory.createDefault(plane, 0, 0, 1, 2),
                passengerFactory.createDefault(plane, 1, 0, 1, 2)
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
                passengerFactory.createDefault(plane, 0, 0, 2, 3),
                passengerFactory.createDefault(plane, 0, 1, 2, 3)
        );
        List<PassengerGroup> passengerGroup = List.of(new PassengerGroup(passengers));
        plane.boardPassengers(passengerGroup);

        DeplainingProblem deplainingProblem = DeplainingProblem.builder()
                .plane(plane)
                .passengers(passengerGroup)
                .build();
        SimulatorRequest simulatorRequest = new SimulatorRequest(deplainingProblem);
        SimulatorResponse simulatorResponse = simulator.simulate(simulatorRequest);
        assertEquals(8, simulatorResponse.time());
    }

    @Test
    void bigCollisionSetup() {
        int rows = 5;
        int cols = 4;
        Plane plane = planeFactory.create(rows, cols);

        List<SimulatorPassenger> passengers = List.of(
                passengerFactory.createDefault(plane, 0, 1, 1, 1),
                passengerFactory.createDefault(plane, 0, 2, 2, 2),
                passengerFactory.createDefault(plane, 1, 1, 1, 1),
                passengerFactory.createDefault(plane, 1, 2, 2, 2),
                passengerFactory.createDefault(plane, 2, 1, 1, 1),
                passengerFactory.createDefault(plane, 2, 2, 2, 2),
                passengerFactory.createDefault(plane, 3, 1, 1, 1),
                passengerFactory.createDefault(plane, 3, 2, 2, 2)
        );
        List<PassengerGroup> passengerGroup = List.of(new PassengerGroup(passengers));
        plane.boardPassengers(passengerGroup);

        DeplainingProblem deplainingProblem = DeplainingProblem.builder()
                .plane(plane)
                .passengers(passengerGroup)
                .build();
        SimulatorRequest simulatorRequest = new SimulatorRequest(deplainingProblem);
        SimulatorResponse simulatorResponse = simulator.simulate(simulatorRequest);
        assertEquals(14, simulatorResponse.time());
    }

    @Test
    void differentEnteringTimeSetup() {
        int rows = 5;
        int cols = 4;
        Plane plane = planeFactory.create(rows, cols);

        List<SimulatorPassenger> passengers = List.of(
                passengerFactory.createDefault(plane, 0, 1, 1, 1),
                passengerFactory.createDefault(plane, 0, 2, 1, 2)
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
    void moreThanOneDeplaningGroup() {
        int rows = 5;
        int cols = 4;
        Plane plane = planeFactory.create(rows, cols);

        List<SimulatorPassenger> passengers1 = List.of(
                passengerFactory.createDefault(plane, 0, 1, 1, 1),
                passengerFactory.createDefault(plane, 0, 2, 1, 2)
        );
        List<SimulatorPassenger> passengers2 = List.of(
                passengerFactory.createDefault(plane, 0, 0, 2, 3),
                passengerFactory.createDefault(plane, 0, 1, 2, 3)
        );
        List<PassengerGroup> passengerGroups = List.of(new PassengerGroup(passengers1), new PassengerGroup(passengers2));
        plane.boardPassengers(passengerGroups);

        DeplainingProblem deplainingProblem = DeplainingProblem.builder()
                .plane(plane)
                .passengers(passengerGroups)
                .build();
        SimulatorRequest simulatorRequest = new SimulatorRequest(deplainingProblem);
        SimulatorResponse simulatorResponse = simulator.simulate(simulatorRequest);

        assertEquals(12, simulatorResponse.time());
    }

    @Test
    void fullPlaneFastToSlow() {
        int rows = 5;
        int cols = 4;
        Plane plane = planeFactory.create(rows, cols);

        List<SimulatorPassenger> passengers = List.of(
                passengerFactory.createDefault(plane, 0, 0, 1, 1),
                passengerFactory.createDefault(plane, 0, 1, 1, 1),
                passengerFactory.createDefault(plane, 0, 2, 1, 1),
                passengerFactory.createDefault(plane, 0, 3, 1, 1),
                passengerFactory.createDefault(plane, 1, 0, 2, 2),
                passengerFactory.createDefault(plane, 1, 1, 2, 2),
                passengerFactory.createDefault(plane, 1, 2, 2, 2),
                passengerFactory.createDefault(plane, 1, 3, 2, 2),
                passengerFactory.createDefault(plane, 2, 0, 3, 3),
                passengerFactory.createDefault(plane, 2, 1, 3, 3),
                passengerFactory.createDefault(plane, 2, 2, 3, 3),
                passengerFactory.createDefault(plane, 2, 3, 3, 3),
                passengerFactory.createDefault(plane, 3, 0, 4, 4),
                passengerFactory.createDefault(plane, 3, 1, 4, 4),
                passengerFactory.createDefault(plane, 3, 2, 4, 4),
                passengerFactory.createDefault(plane, 3, 3, 4, 4),
                passengerFactory.createDefault(plane, 4, 0, 5, 5),
                passengerFactory.createDefault(plane, 4, 1, 5, 5),
                passengerFactory.createDefault(plane, 4, 2, 5, 5),
                passengerFactory.createDefault(plane, 4, 3, 5, 5)
        );

        List<PassengerGroup> passengerGroup = List.of(new PassengerGroup(passengers));
        plane.boardPassengers(passengerGroup);

        DeplainingProblem deplainingProblem = DeplainingProblem.builder()
                .plane(plane)
                .passengers(passengerGroup)
                .build();
        SimulatorRequest simulatorRequest = new SimulatorRequest(deplainingProblem);
        SimulatorResponse simulatorResponse = simulator.simulate(simulatorRequest);
        assertEquals(71, simulatorResponse.time());
    }

    @Test
    void smallLuggageSetup() {
        int rows = 5;
        int cols = 4;
        Plane plane = planeFactory.create(rows, cols);

        List<SimulatorPassenger> passengers = List.of(
                passengerFactory.createLuggage(plane, 0, 1, 1, 1, 0, 3),
                passengerFactory.createLuggage(plane, 1, 1, 1, 1, 1, 3)
        );

        List<PassengerGroup> passengerGroups = List.of(new PassengerGroup(passengers));
        plane.boardPassengers(passengerGroups);

        DeplainingProblem deplainingProblem = DeplainingProblem.builder()
                .plane(plane)
                .passengers(passengerGroups)
                .build();
        SimulatorRequest simulatorRequest = new SimulatorRequest(deplainingProblem);
        SimulatorResponse simulatorResponse = simulator.simulate(simulatorRequest);
        assertEquals(6, simulatorResponse.time());
    }

    @Test
    void smallLuggageBackOfPlaneSetup() {
        int rows = 5;
        int cols = 4;
        Plane plane = planeFactory.create(rows, cols);

        List<SimulatorPassenger> passengers = List.of(
                passengerFactory.createLuggage(plane, 0, 1, 1, 1, 3, 3),
                passengerFactory.createLuggage(plane, 1, 1, 1, 1, 4, 3)
        );

        List<PassengerGroup> passengerGroups = List.of(new PassengerGroup(passengers));
        plane.boardPassengers(passengerGroups);

        DeplainingProblem deplainingProblem = DeplainingProblem.builder()
                .plane(plane)
                .passengers(passengerGroups)
                .build();
        SimulatorRequest simulatorRequest = new SimulatorRequest(deplainingProblem);
        SimulatorResponse simulatorResponse = simulator.simulate(simulatorRequest);
        assertEquals(12, simulatorResponse.time());
    }

    @Test
    void smallLuggageSwapSetup() {
        int rows = 5;
        int cols = 4;
        Plane plane = planeFactory.create(rows, cols);

        List<SimulatorPassenger> passengers = List.of(
                passengerFactory.createLuggage(plane, 0, 1, 1, 4, 4, 3),
                passengerFactory.createLuggage(plane, 2, 1, 1, 1, 2, 20)
        );

        List<PassengerGroup> passengerGroups = List.of(new PassengerGroup(passengers));
        plane.boardPassengers(passengerGroups);

        DeplainingProblem deplainingProblem = DeplainingProblem.builder()
                .plane(plane)
                .passengers(passengerGroups)
                .build();
        SimulatorRequest simulatorRequest = new SimulatorRequest(deplainingProblem);
        SimulatorResponse simulatorResponse = simulator.simulate(simulatorRequest);
        xmlService.saveVisualization(simulatorResponse.visualizationDto(), "test");
        assertEquals(32, simulatorResponse.time());
    }
}