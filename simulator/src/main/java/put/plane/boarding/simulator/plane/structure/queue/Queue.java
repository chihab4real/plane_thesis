package put.plane.boarding.simulator.plane.structure.queue;

import lombok.Builder;
import lombok.Data;
import put.plane.boarding.simulator.passenger.PassengerDecorator;

import java.util.*;

@Data
@Builder
public class Queue {

    private final int size;
    private final List<PassengerDecorator> queueArray;
    private final List<PassengerDecorator> queueLocks;
    private final SeatQueue seatQueue;

    public boolean isAvailable(int position) {
        return position < 0 ||
                queueArray.get(position) == null && queueLocks.get(position) == null;
    }

    public boolean isPassengerInFrontOfQueue(PassengerDecorator passenger) {
        return seatQueue.isPassengerInFrontOfQueue(passenger);
    }

    public void onPassengerOffSeat(PassengerDecorator passenger) {
        seatQueue.onPassengerOffSeat(passenger);
    }

    public void take(PassengerDecorator passenger, int position) {
        queueArray.set(position, passenger);
        queueLocks.set(position, null);
    }

    public void lock(PassengerDecorator passenger, int position) {
        if (position >= 0) {
            queueLocks.set(position, passenger);
        }
    }

    public void release(PassengerDecorator passenger) {
        var position = findPassenger(passenger);
        if (position >= 0) {
            queueArray.set(position, null);
        }
    }

    public int findPassenger(PassengerDecorator passenger) {
        return queueArray.indexOf(passenger);
    }

    public int stepTo(PassengerDecorator passenger, int targetLocation) {
        var passengerLocation = findPassenger(passenger);
        return passengerLocation == targetLocation ?
            passengerLocation : passengerLocation > targetLocation ?
                passengerLocation - 1 :
                passengerLocation + 1;

    }

    public void boardPassengers(List<PassengerDecorator> passengers) {
        seatQueue.boardPassengers(passengers);
    }
}
