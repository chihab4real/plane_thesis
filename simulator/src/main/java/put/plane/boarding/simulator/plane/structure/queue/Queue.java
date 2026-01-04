package put.plane.boarding.simulator.plane.structure.queue;

import lombok.Builder;
import lombok.Data;
import put.plane.boarding.simulator.passenger.SimulatorPassenger;

import java.util.List;

@Data
@Builder
public class Queue {

    private final int size;
    private final List<SimulatorPassenger> queueArray;
    private final List<SimulatorPassenger> queueLocks;

    public boolean isSpotAvailable(int position) {
        return position < 0
                || queueLocks.get(position) == null && isPassengerDuringMovingOrNull(queueArray.get(position));
    }

    public boolean isSpotFree(int position) {
        return position < 0 || queueArray.get(position) == null;
    }

    public void takeSpot(SimulatorPassenger passenger, int position) {
        queueArray.set(position, passenger.rootPassenger());
        queueLocks.set(position, null);
    }

    public void lockSpot(SimulatorPassenger passenger, int position) {
        if (position >= 0) {
            queueLocks.set(position, passenger.rootPassenger());
        }
    }

    public void releaseSpot(SimulatorPassenger passenger) {
        int position = findPassenger(passenger);
        if (position >= 0) {
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
}
