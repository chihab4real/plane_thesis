package put.plane.boarding.simulator.simulator.factory;

import org.springframework.stereotype.Service;
import put.plane.boarding.simulator.passenger.PassengerBuilder;
import put.plane.boarding.simulator.passenger.SimulatorPassenger;
import put.plane.boarding.simulator.passenger.impl.DefaultPassenger;
import put.plane.boarding.simulator.passenger.impl.decorator.BaggagePassenger;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.structure.Seat;

@Service
public class TestPassengerFactory {

    public SimulatorPassenger createDefault(Plane plane, int row, int column, int passengerSpeed, int queueEnteringSpeed, int group) {

        Seat seat = new Seat(row, plane.getFiles().get(column));

        DefaultPassenger defaultPassenger = new DefaultPassenger(seat, passengerSpeed, queueEnteringSpeed, group);
        return new PassengerBuilder(defaultPassenger)
                .build();
    }

    public SimulatorPassenger createLuggage(Plane plane, int row, int column, int passengerSpeed, int queueEnteringSpeed, int group, int baggageLocation, int pickupDuration) {
        SimulatorPassenger result = createDefault(plane, row, column, passengerSpeed, queueEnteringSpeed, group);
        return new BaggagePassenger(result, baggageLocation, pickupDuration);
    }
}
