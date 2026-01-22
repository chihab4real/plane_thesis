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
            String name = optimizerResults.get(i).methodName();
            methodsHeader.append(name).append("Group")
                    .append(",")
                    .append(name).append("OrderInGroup")
                    .append(",")
                    .append(name).append("WaitCount");
        }

        // 2. Pre-process results: Create a Map for each method: <PassengerIndex, GroupNumber>
        List<Map<Integer, Integer>> groupLookups = new ArrayList<>();
        List<Map<Integer, Integer>> orderInGroupLookups = new ArrayList<>();
        for (OptimizerResult result : optimizerResults) {
            Map<Integer, Integer> lookup = new HashMap<>();
            Map<Integer, Integer> orderLookup = new HashMap<>();
            List<List<Integer>> groups = result.passengerGroups();
            for (int g = 0; g < groups.size(); g++) {
                List<Integer> group = groups.get(g);
                for (int order = 0; order < group.size(); order++) {
                    Integer pIdx = group.get(order);
                    lookup.put(pIdx, g + 1); // Group numbers usually start at 1
                    orderLookup.put(pIdx, order + 1); // Order in group starts at 1
                }
            }
            groupLookups.add(lookup);
            orderInGroupLookups.add(orderLookup);
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

                    // 4. Append the group number, order in group, and wait count for each optimization method
                    for (int j = 0; j < optimizerResults.size(); j++) {
                        Map<Integer, Integer> groupLookup = groupLookups.get(j);
                        Map<Integer, Integer> orderLookup = orderInGroupLookups.get(j);
                        OptimizerResult result = optimizerResults.get(j);

                        Integer gNum = groupLookup.getOrDefault(i, 0); // 0 if passenger wasn't in a group
                        Integer orderInGroup = orderLookup.getOrDefault(i, 0);
                        Long waitCount = result.waitCountPerPassenger().getOrDefault(i, 0L);

                        line.append(",").append(gNum)
                            .append(",").append(orderInGroup)
                            .append(",").append(waitCount);
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
