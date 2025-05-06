package put.plane.boarding.simulator.plane.structure.queue.factory;

import put.plane.boarding.simulator.passenger.PassengerDecorator;
import put.plane.boarding.simulator.plane.structure.queue.Queue;
import put.plane.boarding.simulator.plane.structure.queue.SeatQueue;

import java.util.ArrayList;
import java.util.Arrays;

public class QueueFactory {

    public static Queue create(int size) {
        var queueList = Arrays
                .stream(new PassengerDecorator[size])
                .toList();
        return Queue.builder()
                .size(size)
                .queueArray(new ArrayList<>(queueList))
                .queueLocks(new ArrayList<>(queueList))
                .seatQueue(new SeatQueue())
                .build();
    }
}
