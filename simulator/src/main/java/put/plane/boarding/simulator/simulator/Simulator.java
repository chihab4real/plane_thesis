package put.plane.boarding.simulator.simulator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import put.plane.boarding.simulator.passenger.Passenger;

import java.util.ArrayList;
import java.util.List;

import static put.plane.boarding.simulator.plane.PlaneConstants.EXIT_FROM_PLANE;

@Service
@RequiredArgsConstructor
public final class Simulator {

    public SimulatorResponse simulate(SimulatorRequest request) {

        // TODO: handle the seats further from queue - passenger will jump from window seat to queue even tho there is passenger blocking the way - Marcin
        var problem = request.getProblem();
        var queue = problem.getPlane().getQueue();
        List<Passenger> passengers = new ArrayList<>(problem.getPassengers());
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
                    .filter(Passenger::isOnPlane)
                    .toList();
            resultTime++;
        }

        return SimulatorResponse.builder()
                .time(resultTime)
                .build();
    }
}
