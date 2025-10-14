package put.plane.boarding.simulator.utils;

import lombok.experimental.UtilityClass;
import put.plane.boarding.simulator.passenger.action.Action;

import java.util.Optional;

@UtilityClass
public class ActionUtils {

    public static Optional<Action> chooseBetterAction(Optional<Action> action1, Optional<Action> action2) {
        if (action1.isEmpty()) {
            return action2;
        }

        if (action2.isEmpty()) {
            return action1;
        }

        int location1 = action1.get().getLocation();
        int location2 = action2.get().getLocation();
        return location1 >= location2 ?
                action1 :
                action2;
    }
}
