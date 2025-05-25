package put.plane.boarding.simulator.passenger.impl.decorator;

import put.plane.boarding.simulator.passenger.SimulatorPassenger;
import put.plane.boarding.simulator.passenger.action.Action;
import put.plane.boarding.simulator.plane.Plane;

import java.util.Optional;

import static put.plane.boarding.simulator.utils.ActionUtils.chooseBetterAction;

public class BaggagePassenger extends PassengerDecorator {

    private final int baggageLocation;
    private final int baggagePickDuration;
    private boolean hasBaggage;

    public BaggagePassenger(SimulatorPassenger passenger, int baggageLocation, int baggagePickDuration) {
        super(passenger);
        this.baggageLocation = baggageLocation;
        this.baggagePickDuration = baggagePickDuration;
        this.hasBaggage = false;
    }

    @Override
    public Optional<Action> chooseAction(Plane plane) {
        var bestAction = passenger.chooseAction(plane);
        var newAction = baggageAction(plane);
        return chooseBetterAction(bestAction, newAction);
    }

    private Optional<Action> baggageAction(Plane plane) {
        if (hasBaggage) {
            return Optional.empty();
        }
        var queue = plane.getQueue();
        var positionInQueue = queue.findPassenger(this);
        if (positionInQueue >= 0) {
            if (positionInQueue == baggageLocation) {
                var result = new Action(positionInQueue, baggagePickDuration, this::onBaggagePicked);
                return Optional.of(result);
            }
            var nextPosition = queue.stepInDirection(this, baggageLocation);
            if (queue.isSpotAvailable(nextPosition)) {
                var result = new Action(nextPosition, toMovingDuration());
                return Optional.of(result);
            }
            return Optional.empty();
        }

        return Optional.empty();
    }

    public void onBaggagePicked() {
        hasBaggage = true;
    }
}
