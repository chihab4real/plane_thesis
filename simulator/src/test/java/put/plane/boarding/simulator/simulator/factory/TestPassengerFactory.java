package put.plane.boarding.simulator.simulator.factory;

import org.springframework.stereotype.Service;
import put.plane.boarding.simulator.passenger.SimulatorPassenger;
import put.plane.boarding.simulator.passenger.PassengerBuilder;
import put.plane.boarding.simulator.passenger.impl.DefaultPassenger;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.structure.Seat;

@Service
public class TestPassengerFactory {

    public SimulatorPassenger create(Plane plane, int row, int column, int passengerSpeed, int queueEnteringSpeed) {

        Seat seat = new Seat(row, plane.getFiles().get(column));

        DefaultPassenger defaultPassenger = new DefaultPassenger(seat, passengerSpeed, queueEnteringSpeed);
        return new PassengerBuilder(defaultPassenger)
                .build();
    }
}
