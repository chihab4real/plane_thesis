package put.plane.boarding.passengers.generator;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.security.SecureRandom;
import java.util.UUID;


@Getter @Setter @NoArgsConstructor
public class Passenger {

    private String ID;
    private String seatLocation;
    private Integer speedQueue;
    private Integer speedExiting;
    private boolean hasLuggage;
    private String luggageLocation;


    public Passenger(String seatLocation, Integer speedQueue, Integer speedExiting, boolean hasLuggage, String luggageLocation) {
        this.seatLocation = seatLocation;
        this.ID = UUID.randomUUID().toString();
        this.speedQueue = speedQueue;
        this.speedExiting = speedExiting;
        this.hasLuggage = hasLuggage;
        this.luggageLocation = luggageLocation;
    }



}
