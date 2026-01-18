package put.plane.boarding.simulator.passenger.impl.decorator;

import put.plane.boarding.simulator.passenger.SimulatorPassenger;
import put.plane.boarding.simulator.passenger.action.Action;
import put.plane.boarding.simulator.passenger.action.ActionResult;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.structure.queue.Queue;
import put.plane.boarding.simulator.utils.ActionUtils;

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
        return ActionUtils.chooseBetterAction(bestAction, newAction, plane.getQueue(), this);
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
                return ActionResult.nonBlockedAction(result, baggageLocation);
            }
            int nextStep = queue.stepInDirection(this, baggageLocation);
            if (queue.isSpotAvailable(nextStep)) {
                Action result = new Action(
                        nextStep,
                        toMovingDuration(),
                        () -> queue.releaseSpot(this),
                        () -> queue.isSpotFree(nextStep)
                );
                return ActionResult.nonBlockedAction(result, baggageLocation);
            }
            return ActionResult.blockedAction(nextStep, baggageLocation);
        }

        return ActionResult.NO_ACTION;
    }

    public void onBaggagePicked() {
        hasBaggage = true;
    }
}
