package put.plane.boarding.simulator.plane.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.structure.queue.factory.QueueFactory;
import put.plane.boarding.simulator.problem.factory.file.FileFactory;

@Service
@RequiredArgsConstructor
public class PlaneFactory {

    private final FileFactory fileFactory;

    public Plane create(int numberOfRows, int numberOfColumns) {

        return Plane.builder()
                .rows(numberOfRows)
                .queue(QueueFactory.create(numberOfRows))
                .files(fileFactory.create(numberOfColumns))
                .build();

    }
}
