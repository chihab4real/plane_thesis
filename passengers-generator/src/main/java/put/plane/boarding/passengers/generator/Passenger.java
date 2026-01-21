package put.plane.boarding.passengers.generator;

import lombok.*;
import java.util.UUID;


@Data
public class Passenger {

    private final String id;
    private final String seatLocation;
    private final Integer speedQueue;
    private final Integer speedExiting;
    private final boolean hasLuggage;
    private final String luggageLocation;
    private final Integer luggagePickUpTime;

    public Passenger(String id,String seatLocation, Integer speedQueue, Integer speedExiting, boolean hasLuggage, String luggageLocation, Integer luggagePickUpTime) {
        this.id = id;
        this.seatLocation = seatLocation;
        this.speedQueue = speedQueue;
        this.speedExiting = speedExiting;
        this.hasLuggage = hasLuggage;
        this.luggageLocation = luggageLocation;
        this.luggagePickUpTime = luggagePickUpTime;
    }

    public Passenger(String seatLocation, Integer speedQueue, Integer speedExiting, boolean hasLuggage, String luggageLocation, Integer luggagePickUpTime) {
        this.id = UUID.randomUUID().toString();
        this.seatLocation = seatLocation;
        this.speedQueue = speedQueue;
        this.speedExiting = speedExiting;
        this.hasLuggage = hasLuggage;
        this.luggageLocation = luggageLocation;
        this.luggagePickUpTime = luggagePickUpTime;
    }
}
