package put.plane.boarding.simulator.passenger.action;

public record ActionResult(
        Action action,
        boolean movementBlocked,
        int actionDirection
) {
    public static final ActionResult NO_ACTION = new ActionResult(null, false, Integer.MIN_VALUE);

    public static ActionResult blockedAction(int actionDirection) {
        return new ActionResult(null, true, actionDirection);
    }

    public static ActionResult nonBlockedAction(Action action) {
        return new ActionResult(action, false, action.getDirection());
    }
}
