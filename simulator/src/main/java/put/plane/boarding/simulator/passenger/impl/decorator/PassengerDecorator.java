package put.plane.boarding.simulator.passenger.impl.decorator;

import lombok.RequiredArgsConstructor;
import put.plane.boarding.simulator.passenger.SimulatorPassenger;
import put.plane.boarding.simulator.passenger.action.Action;
import put.plane.boarding.simulator.plane.structure.Seat;

@RequiredArgsConstructor
public abstract class PassengerDecorator implements SimulatorPassenger {

    protected final SimulatorPassenger passenger;

    @Override
    public Seat startSeat() {
        return passenger.startSeat();
    }

    @Override
    public Seat seat() {
        return passenger.seat();
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
    public int toGroup() {
        return passenger.toGroup();
    }

    @Override
    public SimulatorPassenger rootPassenger() {
        return passenger.rootPassenger();
    }

    @Override
    public int hashCode() {
        return passenger.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SimulatorPassenger simulatorPassenger) {
            return passenger.equals(simulatorPassenger);
        }
        return false;
    }
}
