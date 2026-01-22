package put.plane.boarding.optimizing;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import put.plane.boarding.passengers.generator.Passenger;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.factory.PlaneFactory;
import put.plane.boarding.simulator.plane.structure.Seat;
import put.plane.boarding.simulator.problem.DeplainingProblem;
import put.plane.boarding.simulator.problem.PassengerGroup;
import put.plane.boarding.simulator.problem.factory.passenger.PassengerFactory;
import put.plane.boarding.simulator.simulator.Simulator;
import put.plane.boarding.simulator.simulator.SimulatorRequest;
import put.plane.boarding.simulator.simulator.SimulatorResponse;
import put.plane.boarding.simulator.simulator.frame.XMLService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public abstract class Optimizer {

    @Setter(onMethod_ = @Autowired)
    protected Simulator simulator;

    @Setter(onMethod_ = @Autowired)
    protected XMLService xmlservice;

    @Setter(onMethod_ = @Autowired)
    protected PlaneFactory planeFactory;

    public OptimizerResult runOptimization(Plane plane, boolean logs, String path, String methodName, String description,
                                           List<List<Integer>> allGroups, List<Passenger> generatedPassengers,
                                           boolean saveVisualization) {
        List<List<Passenger>> mappedPassengers = allGroups
                .stream()
                .map(passengers -> passengers
                        .stream()
                        .map(generatedPassengers::get)
                        .toList())
                .collect(Collectors.toCollection(ArrayList::new));

        SimulatorResponse simulatorResponse = getTimeForOrder(plane, mappedPassengers, path, methodName, false);

        // Map wait count from Seat to passenger index
        Map<Integer, Long> waitCountPerPassenger = new HashMap<>();
        for (int i = 0; i < generatedPassengers.size(); i++) {
            Passenger p = generatedPassengers.get(i);
            String seatLocation = p.getSeatLocation();
            String[] parts = seatLocation.split("_");
            int row = Integer.parseInt(parts[0]) - 1;
            int fileIndex = Integer.parseInt(parts[1]) - 1;
            Seat seat = new Seat(row, plane.getFiles().get(fileIndex));
            Long waitTime = simulatorResponse.waitCount().get(seat);
            if (waitTime != null) {
                waitCountPerPassenger.put(i, waitTime);
            }
        }

        if (logs) {
            log.info(description);
            log.info("Number of iterations: {} (columns per side)", allGroups.size() / 2);
            log.info("Total groups: {}", allGroups.size());
            for (int i = 0; i < allGroups.size(); i++) {
                log.info("Group {} size: {}", i + 1, allGroups.get(i).size());
            }
        }

        return new OptimizerResult(methodName, simulatorResponse.time(), new ArrayList<>(), allGroups, waitCountPerPassenger);
    }

    public SimulatorResponse getTimeForOrder(Plane plane, List<List<Passenger>> passengers, String path, String methodName, boolean saveVisualization) {
        Plane freshPlane = planeFactory.create(plane.getRows(), plane.getColumns(), plane.getQueue().isDoubleExit());

        List<PassengerGroup> currPassengers = PassengerFactory.createSimulatorPassengers(freshPlane, passengers);
        freshPlane.boardPassengers(currPassengers);

        DeplainingProblem problem = DeplainingProblem.builder()
                .plane(freshPlane)
                .passengers(currPassengers)
                .build();

        SimulatorRequest request = new SimulatorRequest(problem);
        request.setSaveVisualization(saveVisualization);
        SimulatorResponse response = simulator.simulate(request);

        if (saveVisualization) {
            xmlservice.saveVisualization(response.visualizationDto(), path, methodName);
        }

        return response;
    }

    public abstract OptimizerResult run(Plane plane,List<Passenger> generatedPassengers, String path, boolean logs);

}
