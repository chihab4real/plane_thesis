package put.plane.boarding.simulator.passenger.impl;

import put.plane.boarding.simulator.passenger.PassengerDecorator;
import put.plane.boarding.simulator.passenger.action.Action;
import put.plane.boarding.simulator.plane.structure.queue.Queue;

import java.util.Optional;

import static put.plane.boarding.simulator.utils.ActionUtils.chooseBetterAction;

public class BaggagePassenger extends PassengerDecorator {

    private final int baggageLocation;
    private final int baggagePickDuration;
    private boolean hasBaggage;

    public BaggagePassenger(PassengerDecorator passenger, int baggageLocation, int baggagePickDuration) {
        super(passenger);
        this.baggageLocation = baggageLocation;
        this.baggagePickDuration = baggagePickDuration;
        this.hasBaggage = false;
    }

    @Override
    public Optional<Action> chooseAction(Queue queue) {
        var bestAction = passenger.chooseAction(queue);
        var newAction = baggageAction(queue);
        return chooseBetterAction(bestAction, newAction);
    }

    private Optional<Action> baggageAction(Queue queue) {
        if (hasBaggage) {
            return Optional.empty();
        }
        var positionInQueue = queue.findPassenger(this);
        if (positionInQueue >= 0) {
            if (positionInQueue == baggageLocation) {
                var result = new Action(positionInQueue, baggagePickDuration, this::onBaggagePicked);
                return Optional.of(result);
            }
            var nextPosition = queue.stepTo(this, baggageLocation);
            if (queue.isAvailable(nextPosition)) {
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
