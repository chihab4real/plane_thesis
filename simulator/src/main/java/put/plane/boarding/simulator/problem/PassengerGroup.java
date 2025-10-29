package put.plane.boarding.simulator.problem;

import lombok.AllArgsConstructor;
import lombok.Data;
import put.plane.boarding.simulator.passenger.SimulatorPassenger;

import java.util.List;

@Data
@AllArgsConstructor
public class PassengerGroup {

    private List<SimulatorPassenger> passengers;
}
