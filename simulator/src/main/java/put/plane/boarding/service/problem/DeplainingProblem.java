package put.plane.boarding.service.problem;

import lombok.Builder;
import lombok.Data;
import put.plane.boarding.service.passenger.PassengerDecorator;
import put.plane.boarding.service.plane.PlaneSpecification;
import put.plane.boarding.service.plane.structure.File;

import java.util.List;

@Data
@Builder
public class DeplainingProblem {

    private List<PassengerDecorator> passengers;
    private PlaneSpecification planeSpecification;
}
