package put.plane.boarding.service.problem.factory.agent;

import org.springframework.stereotype.Service;
import put.plane.boarding.service.passenger.PassengerBuilder;
import put.plane.boarding.service.passenger.PassengerDecorator;
import put.plane.boarding.service.plane.structure.File;
import put.plane.boarding.service.plane.structure.Seat;
import put.plane.boarding.service.passenger.impl.DefaultPassenger;

@Service
public class PassengerFactory {

    public PassengerDecorator create(int row, File file) {

        var seat = new Seat(row, file);

        var defaultPassenger = new DefaultPassenger(seat, 1);
        return new PassengerBuilder(defaultPassenger)
                .build();
    }
}