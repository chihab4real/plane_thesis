package put.plane.boarding.simulator.simulator.frame.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import put.plane.boarding.simulator.passenger.SimulatorPassenger;
import put.plane.boarding.simulator.plane.structure.queue.Queue;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SinglePassenger {

    private static final String Q = "Q";

    private String id;
    private int row;
    private String column;

    /**
     * Passenger not in queue
     */
    public SinglePassenger(SimulatorPassenger passenger) {
        id = passenger.startSeat().shortDescription();
        row = passenger.seat().row();
        column = passenger.seat().file().column();
    }

    /**
     * Passenger in queue
     */
    public SinglePassenger(SimulatorPassenger passenger, Queue queue) {
        id = passenger.startSeat().shortDescription();
        row = queue.findPassenger(passenger);
        column = Q;
    }
}