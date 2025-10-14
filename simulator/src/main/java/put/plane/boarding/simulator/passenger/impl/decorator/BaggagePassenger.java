package put.plane.boarding.simulator.passenger.impl.decorator;

import put.plane.boarding.simulator.passenger.SimulatorPassenger;
import put.plane.boarding.simulator.passenger.action.Action;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.structure.queue.Queue;

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
        Optional<Action> bestAction = passenger.chooseAction(plane);
        Optional<Action> newAction = baggageAction(plane);
        return chooseBetterAction(bestAction, newAction);
    }

    private Optional<Action> baggageAction(Plane plane) {
        if (hasBaggage) {
            return Optional.empty();
        }
        Queue queue = plane.getQueue();
        int positionInQueue = queue.findPassenger(this);
        if (positionInQueue >= 0) {
            if (positionInQueue == baggageLocation) {
                Action result = new Action(positionInQueue, baggagePickDuration, this::onBaggagePicked);
                return Optional.of(result);
            }
            int nextPosition = queue.stepInDirection(this, baggageLocation);
            if (queue.isSpotAvailable(nextPosition)) {
                Action result = new Action(nextPosition, toMovingDuration());
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
