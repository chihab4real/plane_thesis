package put.plane.boarding.simulator.plane;

import lombok.Builder;
import lombok.Data;
import put.plane.boarding.simulator.plane.structure.File;
import put.plane.boarding.simulator.plane.structure.Queue;

import java.util.List;

@Data
@Builder
public class Plane {
    private int rows;
    private Queue queue;
    private List<File> files;

    public int getColumns() {
        return files.size();
    }
}
