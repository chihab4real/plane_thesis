package put.plane.boarding.optimizing.optimizers.basic;

import org.springframework.stereotype.Service;
import put.plane.boarding.optimizing.Optimizer;
import put.plane.boarding.optimizing.OptimizerResult;
import put.plane.boarding.passengers.generator.Passenger;
import put.plane.boarding.simulator.plane.Plane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class RowBasedOptimizer extends Optimizer {
    private boolean frontToBack;

    @Override
    public OptimizerResult run(Plane plane, List<Passenger> generatedPassengers, String path, boolean logs) {
        int numRows = plane.getRows();
        Map<Integer, List<Integer>> passengersByRow = new HashMap<>();

        for (int i = 0; i < generatedPassengers.size(); i++) {
            String seatLocation = generatedPassengers.get(i).getSeatLocation();
            int row = Integer.parseInt(seatLocation.split("_")[0]);
            passengersByRow.computeIfAbsent(row, k -> new ArrayList<>()).add(i);
        }

        List<List<Integer>> allGroups = new ArrayList<>();

        if (frontToBack) {
            for (int r = 1; r <= numRows; r++) {
                allGroups.add(passengersByRow.getOrDefault(r, new ArrayList<>()));
            }
        } else {
            for (int r = numRows; r >= 1; r--) {
                allGroups.add(passengersByRow.getOrDefault(r, new ArrayList<>()));
            }
        }

        String type = frontToBack ? "FrontToBack" : "BackToFront";
        String description = frontToBack
                ? "FrontToBack (Row by row from front to back)"
                : "BackToFront (Row by row from back to front)";


        return runOptimization(plane, logs, path, type, description, allGroups, generatedPassengers, true);
    }

    public void setFrontToBack(boolean frontToBack) {
        this.frontToBack = frontToBack;
    }
}
