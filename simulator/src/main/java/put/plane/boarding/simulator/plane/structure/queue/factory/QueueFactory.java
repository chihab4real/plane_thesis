package put.plane.boarding.simulator.plane.structure.queue.factory;

import lombok.experimental.UtilityClass;
import put.plane.boarding.simulator.passenger.impl.decorator.PassengerDecorator;
import put.plane.boarding.simulator.plane.structure.queue.Queue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@UtilityClass
public class QueueFactory {

    public static Queue create(int size, boolean doubleExit) {
        List<PassengerDecorator> queueList = Arrays
                .stream(new PassengerDecorator[size])
                .toList();
        return Queue.builder()
                .size(size)
                .doubleExit(doubleExit)
                .queueArray(new ArrayList<>(queueList))
                .queueLocks(new ArrayList<>(queueList))
                .build();
    }
}
