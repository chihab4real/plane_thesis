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

public class BackToFrontOprimizer extends Optimizer {
    public BackToFrontOprimizer(Simulator simulator, XMLService xmlservice, PlaneFactory planeFactory) {
        super(simulator, xmlservice, planeFactory);
    }

    @Override
    public OptimizerResult run(Plane plane, List<Passenger> generatedPassengers, String path, boolean logs) {
        int numRows = plane.getRows();

        List<List<Integer>> allGroups = new ArrayList<>();
        for (int i = numRows - 1; i >= 0; i--) {
            List<Integer> group = new ArrayList<>();
            for (int j = 0; j < generatedPassengers.size(); j++) {
                Passenger passenger = generatedPassengers.get(j);
                String seatLocation = passenger.getSeatLocation();
                String[] parts = seatLocation.split("_");
                int row = Integer.parseInt(parts[0]);
                if (row == i + 1) {
                    group.add(j);
                }
            }
            allGroups.add(group);
        }
        return runOptimization(plane, logs, path,"BackToFront",
                "BackToFront Optimization (Row by row from back to front)",
                allGroups, generatedPassengers, true);
    }
}
