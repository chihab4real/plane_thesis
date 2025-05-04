package put.plane.boarding.simulator.problem.factory;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import put.plane.boarding.simulator.passenger.PassengerDecorator;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.structure.Queue;
import put.plane.boarding.simulator.problem.DeplainingProblem;
import put.plane.boarding.simulator.problem.factory.agent.PassengerFactory;
import put.plane.boarding.simulator.problem.factory.file.FileFactory;

import java.util.ArrayList;

import static put.plane.boarding.simulator.utils.PairUtils.generateUniquePairs;

@Service
@RequiredArgsConstructor
public class DeplainingProblemFactory {

    private final FileFactory fileFactory;
    private final PassengerFactory passengerFactory;

    public DeplainingProblem create(int numberOfPassengers, int numberOfRows, int numberOfColumns) {

        var maximumNumberOfSeats = numberOfRows * numberOfColumns;

        Preconditions.checkState(numberOfPassengers <= maximumNumberOfSeats, "Too many passengers (" + numberOfPassengers + ") to fit to plane with " + maximumNumberOfSeats + " seats");
        Preconditions.checkState(numberOfColumns % 2 == 0, "Number of columns should be even");

        var queue = new Queue(numberOfRows);
        var plane = Plane.builder()
                .queue(queue)
                .files(fileFactory.create(numberOfColumns))
                .build();

        var passengers = new ArrayList<PassengerDecorator>();

        generateUniquePairs(numberOfPassengers, numberOfRows, numberOfColumns).forEach(pair -> {
            passengers.add(passengerFactory.create(plane, pair.getLeft(), pair.getRight()));
        });

        return DeplainingProblem.builder()
                .passengers(passengers)
                .plane(plane)
                .build();
    }
}
