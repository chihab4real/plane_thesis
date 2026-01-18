package put.plane.boarding.simulator.plane.structure.queue;

import lombok.Builder;
import lombok.Data;
import put.plane.boarding.simulator.passenger.SimulatorPassenger;
import put.plane.boarding.simulator.plane.PlaneConstants;

import java.util.List;

@Data
@Builder
public class Queue {

    private final int size;
    private final boolean doubleExit;
    private final List<SimulatorPassenger> queueArray;
    private final List<SimulatorPassenger> queueLocks;

    public boolean isSpotAvailable(int position) {
        return isExitPosition(position)
                || queueLocks.get(position) == null && isPassengerDuringMovingOrNull(queueArray.get(position));
    }

    public boolean isSpotFree(int position) {
        return isExitPosition(position) || queueArray.get(position) == null;
    }

    public void takeSpot(SimulatorPassenger passenger, int position) {
        queueArray.set(position, passenger.rootPassenger());
        queueLocks.set(position, null);
    }

    public void lockSpot(SimulatorPassenger passenger, int position) {
        if (!isExitPosition(position)) {
            queueLocks.set(position, passenger.rootPassenger());
        }
    }

    public void releaseSpot(SimulatorPassenger passenger) {
        int position = findPassenger(passenger);
        if (!isExitPosition(position)) {
            queueArray.set(position, null);
        }
    }

    public int findPassenger(SimulatorPassenger passenger) {
        return queueArray.indexOf(passenger.rootPassenger());
    }

    public int stepInDirection(SimulatorPassenger passenger, int targetLocation) {
        int passengerLocation = findPassenger(passenger);
        if (passengerLocation == targetLocation)
            return passengerLocation;
        return passengerLocation > targetLocation ?
                passengerLocation - 1 :
                passengerLocation + 1;
    }

    public boolean isPassengerDuringMovingOrNull(SimulatorPassenger passenger) {
        if (passenger == null)
            return true;
        if (passenger.toAction() == null)
            return false;
        int passengerLocation = findPassenger(passenger);
        return passenger.toAction().getDirection() != passengerLocation;
    }

    public SimulatorPassenger getPassengerAt(int position) {
        return queueArray.get(position);
    }

    public int closerExit(int position) {
        int leftDistance = position - PlaneConstants.EXIT_FROM_PLANE;
        int rightDistance = queueArray.size() - position;

        return doubleExit && rightDistance < leftDistance ? queueArray.size() : PlaneConstants.EXIT_FROM_PLANE;
    }

    public boolean isExitPosition(int position) {
        return position < 0 || doubleExit && position >= queueArray.size();
    }
}
