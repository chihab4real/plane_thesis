package put.plane.boarding.simulator.passenger;

import put.plane.boarding.simulator.passenger.action.Action;
import put.plane.boarding.simulator.plane.structure.Seat;
import put.plane.boarding.simulator.plane.structure.queue.Queue;

import java.util.Optional;

public interface Passenger {

    Seat toSeat();
    int toMovingDuration();
    Action toAction();
    void setAction(Action action);
    void onLeavePlane();
    boolean isOnPlane();
    Optional<Action> chooseAction(Queue queue);

    default boolean isDuringAction() {
        var action = toAction();
        return action != null;
    }

    default void onActionComplete() {
        var action = toAction();
        action.getOnActionCompleted().run();
        setAction(null);
    }
}
