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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
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
        if (request.isSaveVisualization())
            savePassengerFrames(remainingPassengers, queue, visualizationFrames);
        for (PassengerGroup passengerGroup : passengerGroups) {
            List<SimulatorPassenger> passengers = new ArrayList<>(passengerGroup.getPassengers());
            remainingPassengers.removeAll(passengers);
            while (!passengers.isEmpty()) {
                AtomicBoolean someCustomerHasMadeAction = new AtomicBoolean(true);
                List<SimulatorPassenger> passengersNotMoved = new ArrayList<>(passengers);
                passengersNotMoved.sort(Comparator.comparingInt(p -> {
                    int position = queue.findPassenger(p);
                    return position < 0 ? Integer.MAX_VALUE : position;
                }));
                while (someCustomerHasMadeAction.get()) {
                    someCustomerHasMadeAction.set(false);
                    List<SimulatorPassenger> passengersWhoMoved = new ArrayList<>();
                    passengersNotMoved.forEach(passenger -> {
                        if (!passenger.isDuringAction()) {
                            possiblyCreatePassengerAction(plane, queue, passenger);
                        }
                        if (passenger.isDuringAction()) {
                            passengersWhoMoved.add(passenger);
                            someCustomerHasMadeAction.set(true);
                            doPassengerAction(passenger, queue);
                        }
                    });
                    passengersNotMoved.removeIf(passengersWhoMoved::contains);
                }
                passengers.removeIf(p -> !p.isOnPlane());
                if (request.isSaveVisualization()) {
                    List<SimulatorPassenger> passengersForFrames = new ArrayList<>(passengers);
                    passengersForFrames.addAll(remainingPassengers);
                    savePassengerFrames(passengersForFrames, queue, visualizationFrames);
                }
                resultTime++;
            }
        }

        return SimulatorResponse.builder()
                .time(resultTime)
                .visualizationDto(new VisualizationDto(plane, visualizationFrames))
                .build();
    }

    private void doPassengerAction(SimulatorPassenger passenger, Queue queue) {
        Action action = passenger.toAction();
        if (action.isOver()) {
            passenger.onActionComplete();
            if (action.getLocation() != EXIT_FROM_PLANE) {
                queue.takeSpot(passenger, action.getLocation());
            }
        } else {
            action.makeProgress();
        }
    }

    private void possiblyCreatePassengerAction(Plane plane, Queue queue, SimulatorPassenger passenger) {
        Optional<Action> nextAction = passenger.chooseAction(plane);
        nextAction.ifPresent(action -> {
            passenger.setAction(action);
            if (action.getLocation() != queue.findPassenger(passenger)) {
                queue.lockSpot(passenger, action.getLocation());
            }
        });
    }

    private void savePassengerFrames(List<SimulatorPassenger> passengers, Queue queue, List<SingleFrame> visualizationFrames) {
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
}