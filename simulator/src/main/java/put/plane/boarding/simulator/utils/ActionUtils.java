package put.plane.boarding.simulator.utils;

import lombok.experimental.UtilityClass;
import put.plane.boarding.simulator.passenger.action.ActionResult;

@UtilityClass
public class ActionUtils {

    public static ActionResult chooseBetterAction(ActionResult action1, ActionResult action2) {
        if (action1 == ActionResult.NO_ACTION) {
            return action2;
        }

        if (action2 == ActionResult.NO_ACTION) {
            return action1;
        }

        int location1 = action1.actionDirection();
        int location2 = action2.actionDirection();
        return location1 >= location2 ? action1 : action2;
    }
}
