package put.plane.boarding.optimizing.optimizers.basic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import put.plane.boarding.optimizing.Optimizer;
import put.plane.boarding.optimizing.OptimizerResult;
import put.plane.boarding.passengers.generator.Passenger;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.factory.PlaneFactory;
import put.plane.boarding.simulator.simulator.Simulator;
import put.plane.boarding.simulator.simulator.frame.XMLService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class ZigZagOptimizer extends Optimizer {


    @Override
    public OptimizerResult run(Plane plane,List<Passenger> generatedPassengers, String path, boolean logs) {
        int numColumns = plane.getColumns();
        int columnsPerSide = numColumns / 2;

        List<List<Integer>> allGroups = new ArrayList<>();

        for (int iteration = 0; iteration < columnsPerSide; iteration++) {
            int leftColumn = iteration + 1;
            int rightColumn = numColumns - iteration;

            List<Integer> group1 = new ArrayList<>();
            List<Integer> group2 = new ArrayList<>();

            for (int i = 0; i < generatedPassengers.size(); i++) {
                Passenger passenger = generatedPassengers.get(i);
                String seatLocation = passenger.getSeatLocation();
                String[] parts = seatLocation.split("_");
                int row = Integer.parseInt(parts[0]);
                int column = Integer.parseInt(parts[1]);

                boolean isLeftIterationColumn = (column == leftColumn);
                boolean isRightIterationColumn = (column == rightColumn);

                if (!isLeftIterationColumn && !isRightIterationColumn) {
                    continue;
                }

                if (row % 2 == 1) {
                    if (isRightIterationColumn) group1.add(i);
                    else group2.add(i);
                } else {
                    if (isLeftIterationColumn) group1.add(i);
                    else group2.add(i);
                }
            }
            allGroups.add(group1);
            allGroups.add(group2);
        }

        Collections.reverse(allGroups);

        return runOptimization(plane, logs, path, "Zigzag",
                "Zigzag Optimization (Iterative alternating sides)", allGroups,
                generatedPassengers, false);
    }


}
