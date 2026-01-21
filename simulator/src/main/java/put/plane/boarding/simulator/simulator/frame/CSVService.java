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
import java.nio.file.StandardOpenOption;
import java.util.*;

@Slf4j
@Service
public class CSVService {

    public void appendOptimizationSummary(List<OptimizerResult> results, String path, String fileName, String flightId) {
        Path outputPath = Paths.get(path, fileName);

        try {
            Files.createDirectories(outputPath.getParent());


            boolean fileExists = Files.exists(outputPath);

            try (BufferedWriter writer = Files.newBufferedWriter(outputPath,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND)) {

                if (!fileExists) {
                    writer.write("flightID,methodName,totalDeplaningTime,numberOfGroups");
                    writer.newLine();
                }

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
            log.error("Failed to append to CSV file: {}", outputPath, e);
            throw new RuntimeException("Could not append to CSV file", e);
        }
    }

    public void appendOptimizationResultsToCSV(List<Passenger> passengers,
                                               List<OptimizerResult> optimizerResults,
                                               String path,
                                               String fileName,
                                               String flightId) {
        Path outputPath = Paths.get(path, fileName);

        // 1. Build the dynamic header
        StringBuilder methodsHeader = new StringBuilder();
        for (int i = 0; i < optimizerResults.size(); i++) {
            if (i > 0) methodsHeader.append(",");
            methodsHeader.append(optimizerResults.get(i).methodName()).append("Group");
        }

        // 2. Pre-process results: Create a Map for each method: <PassengerIndex, GroupNumber>
        List<Map<Integer, Integer>> groupLookups = new ArrayList<>();
        for (OptimizerResult result : optimizerResults) {
            Map<Integer, Integer> lookup = new HashMap<>();
            List<List<Integer>> groups = result.passengerGroups();
            for (int g = 0; g < groups.size(); g++) {
                for (Integer pIdx : groups.get(g)) {
                    lookup.put(pIdx, g + 1); // Group numbers usually start at 1
                }
            }
            groupLookups.add(lookup);
        }

        try {
            Files.createDirectories(outputPath.getParent());
            boolean fileExists = Files.exists(outputPath);

            try (BufferedWriter writer = Files.newBufferedWriter(outputPath,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND)) {

                // Write Header
                if (!fileExists) {
                    writer.write("flightID,passengerID,seatLocation,speedQueue,speedExiting,hasLuggage," +
                            "luggageLocation,luggagePickupTime," + methodsHeader);
                    writer.newLine();
                }

                // 3. Iterate through all passengers once
                for (int i = 0; i < passengers.size(); i++) {
                    Passenger passenger = passengers.get(i);

                    // Build the static part of the row
                    StringBuilder line = new StringBuilder(String.format("%s,%s,%s,%d,%d,%b,%s,%s",
                            flightId,
                            escapeCSV(passenger.getId()),
                            escapeCSV(passenger.getSeatLocation()),
                            passenger.getSpeedQueue(),
                            passenger.getSpeedExiting(),
                            passenger.isHasLuggage(),
                            passenger.getLuggageLocation() != null ? escapeCSV(passenger.getLuggageLocation()) : "",
                            passenger.getLuggagePickUpTime() != null ? passenger.getLuggagePickUpTime().toString() : ""
                    ));

                    // 4. Append the group number for each specific optimization method
                    for (Map<Integer, Integer> lookup : groupLookups) {
                        Integer gNum = lookup.getOrDefault(i, 0); // 0 if passenger wasn't in a group
                        line.append(",").append(gNum);
                    }

                    writer.write(line.toString());
                    writer.newLine();
                }
            }

            log.info("CSV results appended successfully at: {}", outputPath.toAbsolutePath());

        } catch (IOException e) {
            log.error("Failed to append to CSV file: {}", outputPath, e);
            throw new RuntimeException("Could not append to CSV file", e);
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
