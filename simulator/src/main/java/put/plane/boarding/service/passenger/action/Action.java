package put.plane.boarding.service.passenger.action;

import lombok.Getter;

@Getter
public class Action {

    private int actionProgress;
    private final int actionDuration;
    private final Direction direction;
    private final Runnable onActionCompleted;

    public Action(int actionDirection, int actionDuration, Runnable onActionCompleted) {
        this.actionProgress = 1;
        this.actionDuration = actionDuration;
        this.direction = new Direction(actionDirection);
        this.onActionCompleted = onActionCompleted;
    }

    public Action(int actionDirection, int actionDuration) {
        this(actionDirection, actionDuration, () -> {});
    }

    public boolean isOver() {
        return actionProgress >= actionDuration;
    }

    public void makeProgress() {
        actionProgress++;
    }

    public int getLocation() {
        return direction.location();
    }

    public record Direction(int location) {}
}
