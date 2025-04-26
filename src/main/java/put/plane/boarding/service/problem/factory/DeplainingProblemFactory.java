package put.plane.boarding.service.problem.factory;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import put.plane.boarding.service.passenger.PassengerDecorator;
import put.plane.boarding.service.plane.PlaneSpecification;
import put.plane.boarding.service.plane.structure.Queue;
import put.plane.boarding.service.problem.DeplainingProblem;
import put.plane.boarding.service.problem.factory.agent.PassengerFactory;
import put.plane.boarding.service.problem.factory.file.FileFactory;

import java.util.ArrayList;

import static put.plane.boarding.service.utils.PairUtils.generateUniquePairs;

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
        var planeSpecifications = PlaneSpecification.builder()
                .queue(queue)
                .files(fileFactory.create(numberOfColumns))
                .build();

        var passengers = new ArrayList<PassengerDecorator>();

        generateUniquePairs(numberOfPassengers, numberOfRows, numberOfColumns).forEach(pair -> {
            passengers.add(passengerFactory.create(pair.getLeft(), planeSpecifications.getFiles().get(pair.getRight())));
        });

        return DeplainingProblem.builder()
                .passengers(passengers)
                .planeSpecification(planeSpecifications)
                .build();
    }
}
