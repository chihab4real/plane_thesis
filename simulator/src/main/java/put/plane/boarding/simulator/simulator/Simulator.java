package put.plane.boarding.simulator.simulator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import put.plane.boarding.simulator.passenger.SimulatorPassenger;
import put.plane.boarding.simulator.passenger.action.Action;
import put.plane.boarding.simulator.passenger.action.ActionResult;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.structure.queue.Queue;
import put.plane.boarding.simulator.problem.DeplainingProblem;
import put.plane.boarding.simulator.problem.PassengerGroup;
import put.plane.boarding.simulator.simulator.frame.dto.SingleFrame;
import put.plane.boarding.simulator.simulator.frame.dto.SinglePassenger;
import put.plane.boarding.simulator.simulator.frame.dto.VisualizationDto;
import put.plane.boarding.simulator.utils.Constants;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

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
                    Set<SimulatorPassenger> passengersWhoMoved = new HashSet<>();
                    resolveCollidingPassengersWithAction(passengersNotMoved, plane);
                    passengersNotMoved.forEach(passenger -> {
                        if (!passenger.isDuringAction()) {
                            possiblyCreatePassengerAction(plane, queue, passenger);
                        }
                        if (passenger.isDuringAction()) {
                            passengersWhoMoved.add(passenger);
                            if (doPassengerAction(passenger, queue))
                                someCustomerHasMadeAction.set(true);
                        }
                    });
                    passengersNotMoved.removeIf(passengersWhoMoved::contains);
                    resolveCollidingPassengersWithoutAction(passengersNotMoved, plane, someCustomerHasMadeAction);
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

    private void resolveCollidingPassengersWithoutAction(List<SimulatorPassenger> passengersNotMoved, Plane plane, AtomicBoolean someCustomerHasMadeAction) {
        Queue queue = plane.getQueue();
        List<SimulatorPassenger> passengersWithoutAction = passengersNotMoved.stream()
                .filter(p -> p.toAction() == null && queue.findPassenger(p) != -1)
                .toList();
        Set<SimulatorPassenger> otherResolved = new HashSet<>();
        for (SimulatorPassenger passenger : passengersWithoutAction) {
            if (otherResolved.contains(passenger))
                continue;
            int passengerPosition = queue.findPassenger(passenger);
            ActionResult passengerAction = passenger.chooseAction(plane);

            int passengerActionDirection = passengerAction.actionDirection();
            SimulatorPassenger otherRoot = queue.getPassengerAt(passengerActionDirection);
            SimulatorPassenger other = passengersNotMoved.stream()
                    .filter(p -> p.rootPassenger().equals(otherRoot))
                    .findFirst().orElse(null);
            if (other == null || other.isDuringAction())
                continue;
            ActionResult otherAction = other.chooseAction(plane);

            if (passengerPosition == otherAction.actionDirection()) {
                Action action1 = new Action(
                        passengerActionDirection,
                        passenger.toMovingDuration(),
                        null,
                        Constants.ALWAYS_FALSE);
                Action action2 = new Action(
                        passengerPosition,
                        other.toMovingDuration(),
                        null,
                        Constants.ALWAYS_FALSE);
                passenger.setAction(action1);
                other.setAction(action2);
                queue.lockSpot(passenger, action1.getDirection());
                queue.lockSpot(other, action2.getDirection());
                otherResolved.add(other);
                someCustomerHasMadeAction.set(true);
            }
        }
    }

    private void resolveCollidingPassengersWithAction(List<SimulatorPassenger> passengersNotMoved, Plane plane) {
        Queue queue = plane.getQueue();
        List<SimulatorPassenger> passengersWithAction = passengersNotMoved.stream()
                .filter(p -> p.toAction() != null
                        && p.toAction().isOverWaiting()
                        && !queue.isExitPosition(p.toAction().getDirection())
                        && queue.findPassenger(p) != -1)
                .toList();
        Set<SimulatorPassenger> otherResolved = new HashSet<>();
        for (SimulatorPassenger passenger : passengersWithAction) {
            if (otherResolved.contains(passenger.rootPassenger()))
                continue;
            Action passengerAction = passenger.toAction();
            SimulatorPassenger other = queue.getPassengerAt(passengerAction.getDirection());
            if (other == null || other.toAction() == null || other == passenger.rootPassenger()) {
                continue;
            }
            int passengerPosition = queue.findPassenger(passenger);
            Action otherAction = other.toAction();
            if (otherAction.isOverWaiting() && passengerPosition == otherAction.getDirection()) {
                swapPassengers(queue, passenger, other);
                passengersNotMoved.remove(passenger);
                passengersNotMoved.removeIf(p -> p.rootPassenger().equals(other));
                passenger.setAction(null);
                other.setAction(null);
                otherResolved.add(other);
            }
        }
    }

    private void swapPassengers(Queue queue, SimulatorPassenger passenger1, SimulatorPassenger passenger2) {
        int position1 = queue.findPassenger(passenger1);
        int position2 = queue.findPassenger(passenger2);

        queue.takeSpot(passenger1, position2);
        queue.takeSpot(passenger2, position1);
    }

    private boolean doPassengerAction(SimulatorPassenger passenger, Queue queue) {
        Action action = passenger.toAction();
        if (action.isOver()) {
            passenger.onActionComplete();
            if (!queue.isExitPosition(action.getDirection())) {
                queue.takeSpot(passenger, action.getDirection());
            }
            return true;
        } else {
            return action.makeProgress();
        }
    }

    private void possiblyCreatePassengerAction(Plane plane, Queue queue, SimulatorPassenger passenger) {
        ActionResult nextAction = passenger.chooseAction(plane);
        if (nextAction.action() != null) {
            Action action = nextAction.action();
            passenger.setAction(action);
            if (action.getDirection() != queue.findPassenger(passenger)) {
                queue.lockSpot(passenger, action.getDirection());
            }
        }
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