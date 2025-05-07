package put.plane.boarding.simulator.passenger.impl;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import put.plane.boarding.simulator.passenger.Passenger;
import put.plane.boarding.simulator.passenger.action.Action;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.structure.queue.Queue;
import put.plane.boarding.simulator.plane.structure.Seat;

import java.util.Objects;
import java.util.Optional;

import static put.plane.boarding.simulator.plane.PlaneConstants.EXIT_FROM_PLANE;

@ToString
@RequiredArgsConstructor
public class DefaultPassenger implements Passenger {

    private Action action;
    private final Seat seat;
    private final int movingDuration;
    private boolean hasLeftPlane = false;

    @Override
    public Optional<Action> chooseAction(Plane plane) {
        var queue = plane.getQueue();
        var passengersOnSeats = plane.getPassengersOnSeats();
        var positionInQueue = queue.findPassenger(this);
        var isInQueue = positionInQueue >= 0;

        if (!isInQueue) {
            if (queue.isSpotAvailable(seat.row()) && passengersOnSeats.isPassengerInFrontSeat(this)) {
                var result = new Action(seat.row(), movingDuration, () -> {
                    passengersOnSeats.onPassengerOffSeat(this);
                });
                return Optional.of(result);
            }
            return Optional.empty();
        }

        var nextStep = queue.stepInDirection(this, EXIT_FROM_PLANE);
        if (queue.isSpotAvailable(nextStep)) {
            var result = new Action(nextStep, movingDuration, () -> {
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