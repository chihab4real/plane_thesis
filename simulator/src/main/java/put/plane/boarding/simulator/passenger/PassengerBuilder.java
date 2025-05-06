package put.plane.boarding.simulator.passenger;

import put.plane.boarding.simulator.passenger.impl.decorator.BaggagePassenger;
import put.plane.boarding.simulator.passenger.impl.DefaultPassenger;

public class PassengerBuilder {

    private Passenger passenger;

    public PassengerBuilder(DefaultPassenger defaultPassenger) {
        this.passenger = defaultPassenger;
    }

    public PassengerBuilder withBaggage(int baggageLocation, int baggagePickupDuration) {
        passenger = new BaggagePassenger(passenger, baggageLocation, baggagePickupDuration);
        return this;
    }

    public Passenger build() {
        return passenger;
    }
}
