package put.plane.boarding.simulator.utils;

import lombok.experimental.UtilityClass;
import put.plane.boarding.simulator.passenger.SimulatorPassenger;
import put.plane.boarding.simulator.passenger.action.ActionResult;
import put.plane.boarding.simulator.plane.structure.queue.Queue;

@UtilityClass
public class ActionUtils {

    public static ActionResult chooseBetterAction(
            ActionResult action1,
            ActionResult action2,
            Queue queue,
            SimulatorPassenger passenger
    ) {
        if (action1 == ActionResult.NO_ACTION)
            return action2;
        if (action2 == ActionResult.NO_ACTION)
            return action1;

        int step1 = action1.actionDirection();
        int step2 = action2.actionDirection();

        if (queue.isExitPosition(action1.actionDestination()))
            return action2;
        if (queue.isExitPosition(action2.actionDestination()))
            return action1;

        int passengerLocation = queue.findPassenger(passenger);
        if (action1.actionDestination() == passengerLocation)
            return action1;
        if (action2.actionDestination() == passengerLocation)
            return action2;

        if (!queue.isDoubleExit())
            return step1 >= step2 ? action1 : action2;
        int queueMiddle = queue.getSize() / 2;
        boolean leftHalf = passengerLocation <= queueMiddle;
        if (leftHalf)
            return step1 <= step2 ? action1 : action2;
        return step1 >= step2 ? action1 : action2;
    }
}
