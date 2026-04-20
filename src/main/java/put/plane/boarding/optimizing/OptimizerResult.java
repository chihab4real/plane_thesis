package put.plane.boarding.optimizing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OptimizerResult{
    String methodName;
    int bestTime;
    List<Integer> bestSolution;
    List<List<Integer>> passengerGroups;
    Map<Integer, Long> waitCountPerPassenger;
    int totalCallsToSimulator;
    long optimizationDurationMillis;

    // NEW: Track fitness evolution
    List<Integer> fitnessHistory;      // All evaluated fitness values
    int iterationOfBestSolution;       // When best was found
    List<Double> diversityHistory;

    public OptimizerResult(String methodName, int bestTime, List<Integer> bestSolution, List<List<Integer>> passengerGroups, Map<Integer, Long> waitCountPerPassenger, int totalCallsToSimulator, long optimizationDurationMillis) {
        this.methodName = methodName;
        this.bestTime = bestTime;
        this.bestSolution = bestSolution;
        this.passengerGroups = passengerGroups;
        this.waitCountPerPassenger = waitCountPerPassenger;
        this.totalCallsToSimulator = totalCallsToSimulator;
        this.optimizationDurationMillis = optimizationDurationMillis;
        this.fitnessHistory = new ArrayList<>();
        this.diversityHistory = new ArrayList<>();
    }

    public OptimizerResult(String methodName, int bestTime, List<Integer> bestSolution, List<List<Integer>> passengerGroups, Map<Integer, Long> waitCountPerPassenger, int totalCallsToSimulator) {
        this.methodName = methodName;
        this.bestTime = bestTime;
        this.bestSolution = bestSolution;
        this.passengerGroups = passengerGroups;
        this.waitCountPerPassenger = waitCountPerPassenger;
        this.totalCallsToSimulator = totalCallsToSimulator;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public int getBestTime() {
        return bestTime;
    }

    public void setBestTime(int bestTime) {
        this.bestTime = bestTime;
    }

    public List<Integer> getBestSolution() {
        return bestSolution;
    }

    public void setBestSolution(List<Integer> bestSolution) {
        this.bestSolution = bestSolution;
    }

    public List<List<Integer>> getPassengerGroups() {
        return passengerGroups;
    }

    public void setPassengerGroups(List<List<Integer>> passengerGroups) {
        this.passengerGroups = passengerGroups;
    }

    public Map<Integer, Long> getWaitCountPerPassenger() {
        return waitCountPerPassenger;
    }

    public void setWaitCountPerPassenger(Map<Integer, Long> waitCountPerPassenger) {
        this.waitCountPerPassenger = waitCountPerPassenger;
    }

    public int getTotalCallsToSimulator() {
        return totalCallsToSimulator;
    }

    public void setTotalCallsToSimulator(int totalCallsToSimulator) {
        this.totalCallsToSimulator = totalCallsToSimulator;
    }

    public long getOptimizationDurationMillis() {
        return optimizationDurationMillis;
    }

    public void setOptimizationDurationMillis(long optimizationDurationMillis) {
        this.optimizationDurationMillis = optimizationDurationMillis;
    }
    public List<Integer> getFitnessHistory() { return fitnessHistory; }
    public void setFitnessHistory(List<Integer> fitnessHistory) { this.fitnessHistory = fitnessHistory; }

    public int getIterationOfBestSolution() { return iterationOfBestSolution; }
    public void setIterationOfBestSolution(int iteration) { this.iterationOfBestSolution = iteration; }

    public List<Double> getDiversityHistory() { return diversityHistory; }
    public void setDiversityHistory(List<Double> diversityHistory) { this.diversityHistory = diversityHistory; }


}
