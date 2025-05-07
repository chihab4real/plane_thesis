package put.plane.boarding.simulator.plane.structure.queue;

import lombok.Builder;
import lombok.Data;
import put.plane.boarding.simulator.passenger.Passenger;

import java.util.*;

@Data
@Builder
public class Queue {

    private final int size;
    private final List<Passenger> queueArray;
    private final List<Passenger> queueLocks;

    public boolean isSpotAvailable(int position) {
        return position < 0 ||
                queueArray.get(position) == null && queueLocks.get(position) == null;
    }

    public void takeSpot(Passenger passenger, int position) {
        queueArray.set(position, passenger);
        queueLocks.set(position, null);
    }

    public void lockSpot(Passenger passenger, int position) {
        if (position >= 0) {
            queueLocks.set(position, passenger);
        }
    }

    public void releaseSpot(Passenger passenger) {
        var position = findPassenger(passenger);
        if (position >= 0) {
            queueArray.set(position, null);
        }
    }

    public int findPassenger(Passenger passenger) {
        return queueArray.indexOf(passenger);
    }

    public int stepInDirection(Passenger passenger, int targetLocation) {
        var passengerLocation = findPassenger(passenger);
        return passengerLocation == targetLocation ?
            passengerLocation : passengerLocation > targetLocation ?
                passengerLocation - 1 :
                passengerLocation + 1;

    }
}
