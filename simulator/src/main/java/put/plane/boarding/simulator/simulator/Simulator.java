package put.plane.boarding.simulator.simulator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import put.plane.boarding.simulator.passenger.SimulatorPassenger;
import put.plane.boarding.simulator.passenger.action.Action;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.structure.queue.Queue;
import put.plane.boarding.simulator.problem.DeplainingProblem;
import put.plane.boarding.simulator.problem.PassengerGroup;
import put.plane.boarding.simulator.simulator.frame.dto.SingleFrame;
import put.plane.boarding.simulator.simulator.frame.dto.SinglePassenger;
import put.plane.boarding.simulator.simulator.frame.dto.VisualizationDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static put.plane.boarding.simulator.plane.PlaneConstants.EXIT_FROM_PLANE;

@Service
@RequiredArgsConstructor
public final class Simulator {

    public SimulatorResponse simulate(SimulatorRequest request) {

        DeplainingProblem problem = request.getProblem();
        Plane plane = problem.getPlane();
        Queue queue = plane.getQueue();
        List<PassengerGroup> passengerGroups = new ArrayList<>(problem.getPassengers());
        int resultTime = 0;
        List<SingleFrame> visualizationFrames = new ArrayList<>();

        List<SimulatorPassenger> remainingPassengers = passengerGroups
                .stream()
                .map(PassengerGroup::getPassengers)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        for (PassengerGroup passengerGroup : passengerGroups) {
            List<SimulatorPassenger> passengers = passengerGroup.getPassengers();
            remainingPassengers.removeAll(passengers);
            while (!passengers.isEmpty()) {
                passengers.forEach(passenger -> {
                    if (!passenger.isDuringAction()) {
                        chooseNextAction(passenger, plane, queue);
                    }
                    if (passenger.isDuringAction()) {
                        executePassengerAction(passenger, queue);
                    }
                });
                passengers = passengers.stream()
                        .filter(SimulatorPassenger::isOnPlane)
                        .toList();
                List<SimulatorPassenger> passengersForFrames = new ArrayList<>(passengers);
                passengersForFrames.addAll(remainingPassengers);
                savePassengerFrames(passengersForFrames, queue, visualizationFrames);
                resultTime++;
            }
        }

        return SimulatorResponse.builder()
                .time(resultTime)
                .visualizationDto(new VisualizationDto(plane, visualizationFrames))
                .build();
    }

    private static void savePassengerFrames(List<SimulatorPassenger> passengers, Queue queue, List<SingleFrame> visualizationFrames) {
        List<SinglePassenger> frame = new ArrayList<>();
        passengers.forEach(passenger -> {
            if (queue.findPassenger(passenger) != -1) {
                frame.add(new SinglePassenger(passenger, queue));
            } else {
                frame.add(new SinglePassenger(passenger));
            }
        });
        visualizationFrames.add(new SingleFrame(frame));
    }

    private void executePassengerAction(SimulatorPassenger passenger, Queue queue) {
        Action action = passenger.toAction();
        if (action.isOver()) {
            passenger.onActionComplete();
            queue.releaseSpot(passenger);
            if (action.getLocation() != EXIT_FROM_PLANE) {
                queue.takeSpot(passenger, action.getLocation());
            }
        } else {
            action.makeProgress();
        }
    }

    private void chooseNextAction(SimulatorPassenger passenger, Plane plane, Queue queue) {
        Optional<Action> nextAction = passenger.chooseAction(plane);
        nextAction.ifPresent(action -> {
            passenger.setAction(action);
            if (action.getLocation() != queue.findPassenger(passenger)) {
                queue.lockSpot(passenger, action.getLocation());
            }
        });
    }
}
