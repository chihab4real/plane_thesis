package put.plane.boarding.optimizing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Service
public class MetricsCsvWriter {

    public void writeMetricsToCsv(List<FlightMetrics> metrics, String outputPath) {
        Path path = Paths.get(outputPath);

        try {
            Files.createDirectories(path.getParent());

            boolean fileExists = Files.exists(path);

            try (FileWriter writer = new FileWriter(path.toFile(), true)) {
                // Write header
                if (!fileExists) {
                    writer.write("flightID,methodName,totalDeplaningTime,numberOfGroups," +
                            "totalCallsToSimulator,optimizationDurationMillis," +
                            "avgWaitTime,stdDevWaitTime,maxWaitTime,minWaitTime,medianWaitTime," +
                            "p25WaitTime,p75WaitTime,p90WaitTime,p95WaitTime," +
                            "avgGroupSize,stdDevGroupSize,maxGroupSize,minGroupSize," +
                            "coefficientOfVariation,skewness,totalWaitTime," +
                            "passengersWithZeroWait,percentageZeroWait," +
                            "bestFitness,avgFitness,worstFitness,fitnessImprovement,fitnessStdDev," +
                            "iterationsToBest,convergenceRate,plateauIterations,diversityScore,convergenceStability\n");
                }
                // Write data rows

                for (FlightMetrics m : metrics) {
                    writer.write(String.format("%s,%s,%d,%d,%d,%d," +
                            "%.2f,%.2f,%d,%d,%d," +
                            "%d,%d,%d,%d," +
                            "%.2f,%.2f,%d,%d," +
                            "%.4f,%.4f,%d," +
                            "%d,%.2f," +
                            "%d,%.2f,%d,%d,%.2f," +  // Add these optimization metrics
                            "%d,%.4f,%d,%.4f,%.2f\n",
                            m.getFlightID(), m.getMethodName(), m.getTotalDeplaningTime(), m.getNumberOfGroups(),
                            m.getTotalCallsToSimulator(), m.getOptimizationDurationMillis(),
                            m.getAvgWaitTime(), m.getStdDevWaitTime(), m.getMaxWaitTime(), m.getMinWaitTime(), m.getMedianWaitTime(),
                            m.getP25WaitTime(), m.getP75WaitTime(), m.getP90WaitTime(), m.getP95WaitTime(),
                            m.getAvgGroupSize(), m.getStdDevGroupSize(), m.getMaxGroupSize(), m.getMinGroupSize(),
                            m.getCoefficientOfVariation(), m.getSkewness(), m.getTotalWaitTime(),
                            m.getPassengersWithZeroWait(), m.getPercentageZeroWait(),
                            m.getBestFitness(), m.getAvgFitness(), m.getWorstFitness(),
                            m.getFitnessImprovement(), m.getFitnessStdDev(),
                            m.getIterationsToBest(), m.getConvergenceRate(), m.getPlateauIterations(),
                            m.getDiversityScore(), m.getConvergenceStability()));
                }
                log.info("Metrics CSV writing completed.");

                log.info("Metrics saved to: {}", outputPath);
            }} catch (IOException e) {
            log.error("Failed to write metrics CSV", e);
        }
    }
}