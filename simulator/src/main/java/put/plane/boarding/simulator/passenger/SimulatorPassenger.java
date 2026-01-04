package put.plane.boarding.simulator.passenger;

import put.plane.boarding.simulator.passenger.action.Action;
import put.plane.boarding.simulator.passenger.action.ActionResult;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.structure.Seat;

public interface SimulatorPassenger {

    Seat startSeat();

    Seat seat();

    int toMovingDuration();

    Action toAction();

    void setAction(Action action);

    void onLeavePlane();

    boolean isOnPlane();

    ActionResult chooseAction(Plane plane);

    default boolean isDuringAction() {
        Action action = toAction();
        return action != null;
    }

    default void onActionComplete() {
        Action action = toAction();
        action.getOnActionCompleted().run();
        setAction(null);
    }

    SimulatorPassenger rootPassenger();
}
