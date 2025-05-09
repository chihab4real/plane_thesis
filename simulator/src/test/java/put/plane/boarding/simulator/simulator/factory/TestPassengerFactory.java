package put.plane.boarding.simulator.simulator.factory;

import org.springframework.stereotype.Service;
import put.plane.boarding.simulator.passenger.Passenger;
import put.plane.boarding.simulator.passenger.PassengerBuilder;
import put.plane.boarding.simulator.passenger.impl.DefaultPassenger;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.structure.Seat;

@Service
public class TestPassengerFactory {

    public Passenger create(Plane plane, int row, int column, int passengerSpeed) {

        var seat = new Seat(row, plane.getFiles().get(column));

        var defaultPassenger = new DefaultPassenger(seat, passengerSpeed);
        return new PassengerBuilder(defaultPassenger)
                .build();
    }
}
