package put.plane.boarding.simulator.simulator.frame.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class SingleFrame {

    private List<SinglePassenger> passengers;
}
