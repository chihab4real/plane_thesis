package put.plane.boarding.simulator.passenger;

import lombok.RequiredArgsConstructor;
import put.plane.boarding.simulator.passenger.action.Action;
import put.plane.boarding.simulator.plane.structure.queue.Queue;
import put.plane.boarding.simulator.plane.structure.Seat;

import java.util.Optional;

@RequiredArgsConstructor
public abstract class PassengerDecorator {

    protected final PassengerDecorator passenger;

    public Seat toSeat() {
        return passenger.toSeat();
    }

    public int toMovingDuration() {
        return passenger.toMovingDuration();
    }

    public Action toAction() {
        return passenger.toAction();
    }

    public void setAction(Action action) {
        passenger.setAction(action);
    }

    public void onLeavePlane() {
        passenger.onLeavePlane();
    }

    public boolean isOnPlane() {
        return passenger.isOnPlane();
    }

    public abstract Optional<Action> chooseAction(Queue queue);

    public boolean isDuringAction() {
        var action = toAction();
        return action != null;
    }

    public void onActionComplete() {
        var action = toAction();
        action.getOnActionCompleted().run();
        setAction(null);
    }

    public int hashCode() {
        return passenger.hashCode();
    }
}
