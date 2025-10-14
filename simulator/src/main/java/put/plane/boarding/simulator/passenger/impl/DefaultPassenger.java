package put.plane.boarding.simulator.passenger.impl;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import put.plane.boarding.simulator.passenger.SimulatorPassenger;
import put.plane.boarding.simulator.passenger.action.Action;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.structure.Seat;
import put.plane.boarding.simulator.plane.structure.queue.PassengersOnSeats;
import put.plane.boarding.simulator.plane.structure.queue.Queue;

import java.util.Objects;
import java.util.Optional;

import static put.plane.boarding.simulator.plane.PlaneConstants.EXIT_FROM_PLANE;

@ToString
@RequiredArgsConstructor
public class DefaultPassenger implements SimulatorPassenger {

    private Action action;
    private final Seat seat;
    private final int movingDuration;
    private final int enteringDuration;
    private boolean hasLeftPlane = false;

    @Override
    public Optional<Action> chooseAction(Plane plane) {
        Queue queue = plane.getQueue();
        PassengersOnSeats passengersOnSeats = plane.getPassengersOnSeats();
        int positionInQueue = queue.findPassenger(this);
        boolean isInQueue = positionInQueue >= 0;

        if (!isInQueue) {
            if (queue.isSpotAvailable(seat.row()) && passengersOnSeats.isPassengerInFrontSeat(this)) {
                Action result = new Action(seat.row(), enteringDuration, () -> passengersOnSeats.onPassengerOffSeat(this));
                return Optional.of(result);
            }
            return Optional.empty();
        }

        int nextStep = queue.stepInDirection(this, EXIT_FROM_PLANE);
        if (queue.isSpotAvailable(nextStep)) {
            Action result = new Action(nextStep, movingDuration, () -> {
                if (nextStep == EXIT_FROM_PLANE) {
                    onLeavePlane();
                }
            });
            return Optional.of(result);
        }
        return Optional.empty();
    }

    @Override
    public Seat toSeat() {
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
    public void setAction(Action action) {
        this.action = action;
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
}