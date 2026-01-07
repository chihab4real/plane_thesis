package put.plane.boarding.simulator.passenger.impl;

import lombok.Setter;
import put.plane.boarding.simulator.passenger.SimulatorPassenger;
import put.plane.boarding.simulator.passenger.action.Action;
import put.plane.boarding.simulator.passenger.action.ActionResult;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.structure.Seat;
import put.plane.boarding.simulator.plane.structure.queue.PassengersOnSeats;
import put.plane.boarding.simulator.plane.structure.queue.Queue;

import java.util.Objects;

import static put.plane.boarding.simulator.plane.PlaneConstants.EXIT_FROM_PLANE;

public class DefaultPassenger implements SimulatorPassenger {

    @Setter
    private Action action;
    private final Seat startSeat;
    private final Seat seat;
    private final int movingDuration;
    private final int enteringDuration;
    private final int group;
    private boolean hasLeftPlane = false;

    public DefaultPassenger(Seat seat, int movingDuration, int enteringDuration, int group) {
        this.seat = seat;
        this.startSeat = seat;
        this.movingDuration = movingDuration;
        this.enteringDuration = enteringDuration;
        this.group = group;
    }

    @Override
    public ActionResult chooseAction(Plane plane) {
        Queue queue = plane.getQueue();
        PassengersOnSeats passengersOnSeats = plane.getPassengersOnSeats();
        int positionInQueue = queue.findPassenger(this);
        boolean isInQueue = positionInQueue >= 0;

        if (!isInQueue) {
            if (queue.isSpotAvailable(seat.row())) {
                if (passengersOnSeats.isPassengerInFrontSeat(this)) {
                    Action result = new Action(
                            seat.row(),
                            enteringDuration,
                            () -> passengersOnSeats.onPassengerOffSeat(this),
                            () -> queue.isSpotFree(seat.row())
                    );
                    return ActionResult.nonBlockedAction(result);
                }
                if (passengersOnSeats.isPassengerStuckInSeat(this)) {
                    Action result = new Action(
                            seat.row(),
                            enteringDuration + 10,
                            () -> passengersOnSeats.onPassengerOffSeat(this),
                            () -> queue.isSpotFree(seat.row())
                    );
                    return ActionResult.nonBlockedAction(result);
                }
            }
            return ActionResult.blockedAction(seat.row());
        }

        int nextStep = queue.stepInDirection(this, EXIT_FROM_PLANE);
        if (queue.isSpotAvailable(nextStep)) {
            Action result = new Action(
                    nextStep,
                    movingDuration,
                    () -> {
                        queue.releaseSpot(this);
                        if (nextStep == EXIT_FROM_PLANE) {
                            onLeavePlane();
                        }
                    },
                    () -> queue.isSpotFree(nextStep)
            );
            return ActionResult.nonBlockedAction(result);
        }
        return ActionResult.blockedAction(nextStep);
    }

    @Override
    public int toGroup() {
        return group;
    }

    @Override
    public SimulatorPassenger rootPassenger() {
        return this;
    }

    @Override
    public Seat startSeat() {
        return startSeat;
    }

    @Override
    public Seat seat() {
        return seat;
    }

    @Override
    public int toMovingDuration() {
        return movingDuration;
    }

    @Override
    public Action toAction() {
        return action;
    }

    @Override
    public void onLeavePlane() {
        hasLeftPlane = true;
    }

    @Override
    public boolean isOnPlane() {
        return !hasLeftPlane;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(seat);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SimulatorPassenger other) {
            return seat.equals(other.seat());
        }
        return false;
    }
}