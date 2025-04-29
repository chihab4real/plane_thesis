package put.plane.boarding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import put.plane.boarding.simulator.passenger.PassengerDecorator;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.factory.PlaneFactory;
import put.plane.boarding.simulator.problem.DeplainingProblem;
import put.plane.boarding.simulator.problem.factory.agent.PassengerFactory;
import put.plane.boarding.simulator.simulator.Simulator;
import put.plane.boarding.simulator.simulator.SimulatorRequest;

import java.util.List;

import static put.plane.boarding.simulator.utils.PairUtils.generateUniquePairs;

@Slf4j
@Service
@RequiredArgsConstructor
public class Application {

    private final Simulator simulator;
    private final PassengerFactory passengerFactory;
    private final PlaneFactory planeFactory;

    public static void main(String[] args) {

        var context = new AnnotationConfigApplicationContext(AppConfig.class);
        var application = context.getBean(Application.class);
        application.run();
    }

    public void run() {
        // TODO: Create optimizer
        log.info("Hello");

        // creating example problem
        int rows = 4;
        int columns = 4;
        var plane = planeFactory.create(rows, columns);
        var passengers = createPassengers(plane);

        var problem = DeplainingProblem.builder()
                .plane(plane)
                .passengers(passengers)
                .build();

        // running problem
        var request = new SimulatorRequest(problem);
        var response = simulator.simulate(request);

        log.info("TIME: {}", response.getTime());
    }

    public List<PassengerDecorator> createPassengers(Plane plane) {

        // Random passengers - example
        var seats = generateUniquePairs(5, plane.getRows(), plane.getColumns());
        return seats.stream()
                .map(seat -> passengerFactory.create(plane, seat.getLeft(), seat.getRight()))
                .toList();
    }
}
