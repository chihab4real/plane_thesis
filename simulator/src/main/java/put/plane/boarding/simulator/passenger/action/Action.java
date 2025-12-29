package put.plane.boarding.simulator.passenger.action;

import lombok.Getter;
import put.plane.boarding.simulator.utils.Constants;

import java.util.function.BooleanSupplier;

@Getter
public class Action {

    private int actionProgress;
    private final int actionDuration;
    private final Direction direction;
    private final Runnable onActionCompleted;
    private final BooleanSupplier finishCondition;

    public Action(int actionDirection, int actionDuration, Runnable onActionCompleted, BooleanSupplier finishCondition) {
        this.actionProgress = 1;
        this.actionDuration = actionDuration;
        this.direction = new Direction(actionDirection);
        this.onActionCompleted = onActionCompleted;
        this.finishCondition = finishCondition;
    }

    public Action(int actionDirection, int actionDuration, Runnable onActionCompleted) {
        this(actionDirection, actionDuration, onActionCompleted, Constants.ALWAYS_TRUE);
    }

    public Action(int actionDirection, int actionDuration) {
        this(actionDirection, actionDuration, Constants.DO_NOTHING);
    }

    public boolean isOver() {
        return actionProgress >= actionDuration && finishCondition.getAsBoolean();
    }

    public void makeProgress() {
        actionProgress = Math.min(actionProgress + 1, actionDuration);
    }

    public int getLocation() {
        return direction.location();
    }

    public record Direction(int location) {
    }
}
