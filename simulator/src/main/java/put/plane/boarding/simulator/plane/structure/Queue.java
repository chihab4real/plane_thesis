package put.plane.boarding.simulator.plane.structure;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import put.plane.boarding.simulator.passenger.PassengerDecorator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Data
public class Queue {

    private final int size;
    private List<PassengerDecorator> queueArray;
    private List<PassengerDecorator> queueLocks;

    public Queue(int size) {
        this.size = size;
        var list = Arrays
                .stream(new PassengerDecorator[size])
                .toList();
        this.queueArray = new ArrayList<>(list);
        this.queueLocks = new ArrayList<>(list);
    }

    public boolean isAvailable(int position) {
        return position < 0 ||
                queueArray.get(position) == null && queueLocks.get(position) == null;
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
}
