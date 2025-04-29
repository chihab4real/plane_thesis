package put.plane.boarding.simulator.plane.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.structure.Queue;
import put.plane.boarding.simulator.problem.factory.file.FileFactory;

@Service
@RequiredArgsConstructor
public class PlaneFactory {

    private final FileFactory fileFactory;

    public Plane create(int numberOfRows, int numberOfColumns) {

        var queue = new Queue(numberOfRows);
        var files = fileFactory.create(numberOfColumns);
        return Plane.builder()
                .rows(numberOfRows)
                .queue(queue)
                .files(files)
                .build();

    }
}
