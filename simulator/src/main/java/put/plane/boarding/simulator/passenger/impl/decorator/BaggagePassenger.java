package put.plane.boarding.simulator.passenger.impl.decorator;

import put.plane.boarding.simulator.passenger.SimulatorPassenger;
import put.plane.boarding.simulator.passenger.action.Action;
import put.plane.boarding.simulator.passenger.action.ActionResult;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.structure.queue.Queue;
import put.plane.boarding.simulator.utils.ActionUtils;

import static put.plane.boarding.simulator.plane.PlaneConstants.EXIT_FROM_PLANE;

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
    public ActionResult chooseAction(Plane plane) {
        ActionResult bestAction = passenger.chooseAction(plane);
        ActionResult newAction = baggageAction(plane);
        return ActionUtils.chooseBetterAction(bestAction, newAction);
    }

    private ActionResult baggageAction(Plane plane) {
        if (hasBaggage) {
            return ActionResult.NO_ACTION;
        }
        Queue queue = plane.getQueue();
        int positionInQueue = queue.findPassenger(this);
        if (positionInQueue >= 0) {
            if (positionInQueue == baggageLocation) {
                Action result = new Action(positionInQueue, baggagePickDuration, this::onBaggagePicked);
                return ActionResult.nonBlockedAction(result);
            }
            int nextPosition = queue.stepInDirection(this, baggageLocation);
            if (queue.isSpotAvailable(nextPosition)) {
                Action result = new Action(nextPosition, toMovingDuration(), () -> {
                    queue.releaseSpot(this);
                    if (nextPosition == EXIT_FROM_PLANE) {
                        onLeavePlane();
                    }
                });
                return ActionResult.nonBlockedAction(result);
            }
            return ActionResult.blockedAction(nextPosition);
        }

        return ActionResult.NO_ACTION;
    }

    public void onBaggagePicked() {
        hasBaggage = true;
    }
}
