package put.plane.boarding.simulator.simulator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import put.plane.boarding.simulator.passenger.Passenger;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.structure.queue.Queue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static put.plane.boarding.simulator.plane.PlaneConstants.EXIT_FROM_PLANE;

@Service
@RequiredArgsConstructor
public final class Simulator {

    public SimulatorResponse simulate(SimulatorRequest request) {

        var problem = request.getProblem();
        var plane = problem.getPlane();
        var queue = plane.getQueue();
        List<Passenger> passengers = new ArrayList<>(problem.getPassengers());
        var resultTime = 0;

        while (!passengers.isEmpty()) {
            var someCustomerHasMadeAction = new AtomicBoolean(true);
            List<Passenger> passengersNotMoved = new ArrayList<>(passengers);
            while (someCustomerHasMadeAction.get()) {
                someCustomerHasMadeAction.set(false);
                var passengersWhoMoved = new ArrayList<Passenger>();
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
                passengersNotMoved = passengersNotMoved.stream()
                        .filter(passenger -> !passengersWhoMoved.contains(passenger))
                        .toList();
            }


            passengers = passengers.stream()
                    .filter(Passenger::isOnPlane)
                    .toList();
            resultTime++;
        }

        return SimulatorResponse.builder()
                .time(resultTime)
                .build();
    }

    private void doPassengerAction(Passenger passenger, Queue queue) {
        var action = passenger.toAction();
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

    private void possiblyCreatePassengerAction(Plane plane, Queue queue, Passenger passenger) {
        var nextAction = passenger.chooseAction(plane);
        nextAction.ifPresent(action -> {
            passenger.setAction(action);
            if (action.getLocation() != queue.findPassenger(passenger)) {
                queue.lockSpot(passenger, action.getLocation());
            }
        });
    }
}
