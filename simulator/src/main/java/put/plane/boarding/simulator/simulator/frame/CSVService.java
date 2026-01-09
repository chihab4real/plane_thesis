package put.plane.boarding.simulator.simulator.frame;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import put.plane.boarding.optimizing.OptimizerResult;
import put.plane.boarding.passengers.generator.Passenger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Service
public class CSVService {

    public void saveOptimizationSummary(List<OptimizerResult> results, String path, String fileName, String flightId){
        Path outputPath = Paths.get(path, fileName + ".csv");

        try {
            Files.createDirectories(outputPath.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
                writer.write("flightID,methodName,totalDeplaningTime,numberOfGroups");
                writer.newLine();

                for (OptimizerResult result : results) {
                    String line = String.format("%s,%s,%d,%d",
                            flightId,
                            escapeCSV(result.methodName()),
                            result.bestTime(),
                            result.passengerGroups().size()
                    );

                    writer.write(line);
                    writer.newLine();
                }
            }

        } catch (IOException e) {
            log.error("Failed to save CSV file: {}", outputPath, e);
            throw new RuntimeException("Could not save CSV file", e);
        }
    }
    public void saveOptimizationResultsToCSV(List<Passenger> passengers,
                                             List<List<Integer>> passengerGroups,
                                             String path,
                                             String fileName,
                                             String flightId) {
        Path outputPath = Paths.get(path,"optimization_results", fileName + ".csv");

        try {
            Files.createDirectories(outputPath.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
                writer.write("flightID,passengerID,seatLocation,speedQueue,speedExiting,hasLuggage,luggageLocation,luggagePickupTime,deplaningGroupNumber");
                writer.newLine();

                int groupNumber = 1;
                for (List<Integer> group : passengerGroups) {
                    for (Integer passengerIndex : group) {
                        Passenger passenger = passengers.get(passengerIndex);

                        String line = String.format("%s,%s,%s,%d,%d,%s,%s,%s,%d",
                            flightId,
                            escapeCSV(passenger.getId()),
                            escapeCSV(passenger.getSeatLocation()),
                            passenger.getSpeedQueue(),
                            passenger.getSpeedExiting(),
                            passenger.isHasLuggage(),
                            passenger.getLuggageLocation() != null ? escapeCSV(passenger.getLuggageLocation()) : "",
                            passenger.getLuggagePickUpTime() != null ? passenger.getLuggagePickUpTime().toString() : "",
                            groupNumber
                        );

                        writer.write(line);
                        writer.newLine();
                    }
                    groupNumber++;
                }
            }

            log.info("CSV file saved successfully at: {}", outputPath.toAbsolutePath());

        } catch (IOException e) {
            log.error("Failed to save CSV file: {}", outputPath, e);
            throw new RuntimeException("Could not save CSV file", e);
        }
    }

    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }

        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }

}
