package put.plane.boarding.simulator.passenger.action;

import lombok.Getter;
import put.plane.boarding.simulator.utils.Constants;

import java.util.function.BooleanSupplier;

@Getter
public class Action {

    private int actionProgress;
    private final int direction;
    private final int actionDuration;
    private final Runnable onActionCompleted;
    private final BooleanSupplier finishCondition;

    public Action(int actionDirection, int actionDuration, Runnable onActionCompleted, BooleanSupplier finishCondition) {
        this.actionProgress = 1;
        this.actionDuration = actionDuration;
        this.direction = actionDirection;
        this.onActionCompleted = onActionCompleted;
        this.finishCondition = finishCondition;
    }

    public Action(int actionDirection, int actionDuration, Runnable onActionCompleted) {
        this(actionDirection, actionDuration, onActionCompleted, Constants.ALWAYS_TRUE);
    }

    public boolean isOver() {
        return isOverWaiting() && finishCondition.getAsBoolean();
    }

    public boolean isOverWaiting() {
        return actionProgress >= actionDuration;
    }

    public void makeProgress() {
        actionProgress = Math.min(actionProgress + 1, actionDuration);
    }
}
