package put.plane.boarding.simulator.problem.factory.passenger;

import org.springframework.stereotype.Service;
import put.plane.boarding.simulator.passenger.PassengerBuilder;
import put.plane.boarding.simulator.passenger.PassengerDecorator;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.structure.Seat;
import put.plane.boarding.simulator.passenger.impl.DefaultPassenger;

@Service
public class PassengerFactory {

    // TODO: Replace with passenger definition - Patrick
    public PassengerDecorator create(Plane plane, int row, int column) {

        var seat = new Seat(row, plane.getFiles().get(column));

        var defaultPassenger = new DefaultPassenger(seat, 1);
        return new PassengerBuilder(defaultPassenger)
                .build();
    }
}