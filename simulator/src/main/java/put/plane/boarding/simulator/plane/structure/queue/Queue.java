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
    private final SeatQueue seatQueue;

    public boolean isAvailable(int position) {
        return position < 0 ||
                queueArray.get(position) == null && queueLocks.get(position) == null;
    }

    public boolean isPassengerInFrontOfQueue(Passenger passenger) {
        return seatQueue.isPassengerInFrontOfQueue(passenger);
    }

    public void onPassengerOffSeat(Passenger passenger) {
        seatQueue.onPassengerOffSeat(passenger);
    }

    public void take(Passenger passenger, int position) {
        queueArray.set(position, passenger);
        queueLocks.set(position, null);
    }

    public void lock(Passenger passenger, int position) {
        if (position >= 0) {
            queueLocks.set(position, passenger);
        }
    }

    public void release(Passenger passenger) {
        var position = findPassenger(passenger);
        if (position >= 0) {
            queueArray.set(position, null);
        }
    }

    public int findPassenger(Passenger passenger) {
        return queueArray.indexOf(passenger);
    }

    public int stepTo(Passenger passenger, int targetLocation) {
        var passengerLocation = findPassenger(passenger);
        return passengerLocation == targetLocation ?
            passengerLocation : passengerLocation > targetLocation ?
                passengerLocation - 1 :
                passengerLocation + 1;

    }

    public void boardPassengers(List<Passenger> passengers) {
        seatQueue.boardPassengers(passengers);
    }
}
