package put.plane.boarding.simulator.passenger.action;

public record ActionResult(
        Action action,
        boolean movementBlocked,
        int actionDirection,
        int actionDestination
) {
    public static final ActionResult NO_ACTION = new ActionResult(null, false, Integer.MIN_VALUE, Integer.MIN_VALUE);

    public static ActionResult blockedAction(int actionDirection, int actionDestination) {
        return new ActionResult(null, true, actionDirection, actionDestination);
    }

    public static ActionResult nonBlockedAction(Action action, int actionDestination) {
        return new ActionResult(action, false, action.getDirection(), actionDestination);
    }
}
