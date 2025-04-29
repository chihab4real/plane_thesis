package put.plane.boarding.service.passenger;

import put.plane.boarding.service.passenger.impl.BaggagePassenger;
import put.plane.boarding.service.passenger.impl.DefaultPassenger;

public class PassengerBuilder {

    private PassengerDecorator passenger;

    public PassengerBuilder(DefaultPassenger defaultPassenger) {
        this.passenger = defaultPassenger;
    }

    public PassengerBuilder withBaggage(int baggageLocation, int baggagePickupDuration) {
        passenger = new BaggagePassenger(passenger, baggageLocation, baggagePickupDuration);
        return this;
    }

    public PassengerDecorator build() {
        return passenger;
    }
}
