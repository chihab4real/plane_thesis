package put.plane.boarding.service.plane;

import lombok.Builder;
import lombok.Data;
import put.plane.boarding.service.plane.structure.File;
import put.plane.boarding.service.plane.structure.Queue;

import java.util.List;

@Data
@Builder
public class PlaneSpecification {
    private Queue queue;
    private List<File> files;
}
