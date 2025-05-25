package put.plane.boarding.passengers.generator;

import lombok.*;
import java.util.UUID;


@Data
@RequiredArgsConstructor
public class Passenger {

    private final String ID = UUID.randomUUID().toString();
    private final String seatLocation;
    private final Integer speedQueue;
    private final Integer speedExiting;
    private final boolean hasLuggage;
    private final String luggageLocation;
}
