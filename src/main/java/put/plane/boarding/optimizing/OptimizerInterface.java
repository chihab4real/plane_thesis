package put.plane.boarding.optimizing;

import put.plane.boarding.passengers.generator.Passenger;
import put.plane.boarding.simulator.plane.Plane;

import java.util.List;

public interface OptimizerInterface {
    int getTimeForOrder(Plane plane, List<List<Passenger>> passengers, String path, String methodName, boolean saveVisualization);

    OptimizerResult runOptimization(Plane plane, boolean logs, String path, String methodName, String description,
                                    List<List<Integer>> allGroups,
                                    List<Passenger> generatedPassengers, boolean saveVisualization);


    OptimizerResult run(Plane plane,List<Passenger> generatedPassengers, String path, boolean logs);
}
