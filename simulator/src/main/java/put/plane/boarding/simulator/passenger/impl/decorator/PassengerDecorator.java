package put.plane.boarding.simulator.passenger.impl.decorator;

import lombok.RequiredArgsConstructor;
import put.plane.boarding.simulator.passenger.Passenger;
import put.plane.boarding.simulator.passenger.action.Action;
import put.plane.boarding.simulator.plane.structure.Seat;

@RequiredArgsConstructor
public abstract class PassengerDecorator implements Passenger {

    protected final Passenger passenger;

    @Override
    public Seat toSeat() {
        return passenger.toSeat();
    }

    @Override
    public int toMovingDuration() {
        return passenger.toMovingDuration();
    }

    @Override
    public Action toAction() {
        return passenger.toAction();
    }

    @Override
    public void setAction(Action action) {
        passenger.setAction(action);
    }

    @Override
    public void onLeavePlane() {
        passenger.onLeavePlane();
    }

    @Override
    public boolean isOnPlane() {
        return passenger.isOnPlane();
    }

    @Override
    public int hashCode() {
        return passenger.hashCode();
    }
}
