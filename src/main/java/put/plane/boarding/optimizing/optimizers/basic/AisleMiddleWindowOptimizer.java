package put.plane.boarding.optimizing.optimizers.basic;

import put.plane.boarding.optimizing.Optimizer;
import put.plane.boarding.optimizing.OptimizerResult;
import put.plane.boarding.passengers.generator.Passenger;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.factory.PlaneFactory;
import put.plane.boarding.simulator.simulator.Simulator;
import put.plane.boarding.simulator.simulator.frame.XMLService;

import java.util.ArrayList;
import java.util.List;

public class AisleMiddleWindowOptimizer extends Optimizer {
    public AisleMiddleWindowOptimizer(Simulator simulator, XMLService xmlservice, PlaneFactory planeFactory) {
        super(simulator, xmlservice, planeFactory);
    }

    @Override
    public OptimizerResult run(Plane plane,List<Passenger> generatedPassengers, String path, boolean logs) {
        int numColumns = plane.getColumns();
        int centerColumn = numColumns / 2;

        List<List<Integer>> allGroups = new ArrayList<>();
        for (int iteration = 0; iteration < centerColumn; iteration++) {
            int leftColumn = centerColumn - iteration;
            int rightColumn = centerColumn + 1 + iteration;

            List<Integer> leftGroup = new ArrayList<>();
            List<Integer> rightGroup = new ArrayList<>();

            for (int i = 0; i < generatedPassengers.size(); i++) {
                Passenger passenger = generatedPassengers.get(i);
                String seatLocation = passenger.getSeatLocation();
                String[] parts = seatLocation.split("_");
                int row = Integer.parseInt(parts[0]);
                int column = Integer.parseInt(parts[1]);

                if (column == leftColumn) {
                    leftGroup.add(i);
                } else if (column == rightColumn) {
                    rightGroup.add(i);
                }
            }

            if (!leftGroup.isEmpty()) {
                allGroups.add(leftGroup);
            }
            if (!rightGroup.isEmpty()) {
                allGroups.add(rightGroup);
            }
        }

        return runOptimization(plane, logs,path, "AisleMiddleWindow",
                "aisleMiddleWindow Optimization", allGroups, generatedPassengers,
                true);
    }

}
