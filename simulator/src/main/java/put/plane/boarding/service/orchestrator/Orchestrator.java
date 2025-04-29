package put.plane.boarding.service.orchestrator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import put.plane.boarding.service.passenger.PassengerDecorator;
import put.plane.boarding.service.problem.DeplainingProblem;

import java.util.*;
import java.util.stream.IntStream;

import static put.plane.boarding.service.plane.PlaneConstants.EXIT_FROM_PLANE;

@Slf4j
@RequiredArgsConstructor
public final class Orchestrator {

    public OrchestratorResponse orchestrate(OrchestratorRequest request) {

        var problem = request.getProblem();
        var queue = problem.getPlaneSpecification().getQueue();
        var passengers = problem.getPassengers();
        var resultTime = 0;

        while (!passengers.isEmpty()) {
            passengers.forEach(passenger -> {
                var nextAction = passenger.chooseAction(queue);
                nextAction.ifPresent(action -> {
                    passenger.setAction(action);
                    if (action.getLocation() != queue.findPassenger(passenger)) {
                        queue.lock(passenger, action.getLocation());
                    }
                });
                if (passenger.isDuringAction()) {
                    var action = passenger.toAction();
                    if (action.isOver()) {
                        passenger.onActionComplete();
                        queue.release(passenger);
                        if (action.getLocation() != EXIT_FROM_PLANE) {
                            queue.take(passenger, action.getLocation());
                        }
                    } else {
                        action.makeProgress();
                    }
                }
            });
            passengers = passengers.stream()
                    .filter(PassengerDecorator::isOnPlane)
                    .toList();
            resultTime++;
        }

        return new OrchestratorResponse(resultTime);
    }
}
