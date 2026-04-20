package put.plane.boarding.simulator.simulator.frame;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import put.plane.boarding.optimizing.OptimizerResult;
import put.plane.boarding.passengers.generator.Passenger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Service
public class JSONService {

    private final ObjectMapper objectMapper;

    public JSONService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void saveOptimizationResultsToJSON(
            Map<String, OptimizerResult> results,
            List<Passenger> passengers,
            String path,
            String fileName,
            String flightId) {
        Random random = new Random();

        Path outputPath = Paths.get(path, fileName + ".json");

        try {
            Files.createDirectories(outputPath.getParent());

            Map<String, Object> jsonData = new LinkedHashMap<>();
            jsonData.put("flightID", flightId);

            List<Map<String, Object>> methodsList = new ArrayList<>();
            for (Map.Entry<String, OptimizerResult> entry : results.entrySet()) {
                String methodName = entry.getKey();
                OptimizerResult result = entry.getValue();

                Map<String, Object> methodInfo = new LinkedHashMap<>();
                methodInfo.put("methodName", methodName);
                methodInfo.put("totalDeplaningTime", result.getBestTime());
                methodInfo.put("numberOfGroups", result.getPassengerGroups().size());

                methodsList.add(methodInfo);
            }
            jsonData.put("methods", methodsList);

            List<Map<String, Object>> passengersList = new ArrayList<>();

            for (Passenger passenger : passengers) {
                Map<String, Object> passengerData = new LinkedHashMap<>();
                passengerData.put("passengerID", passenger.getId());
                passengerData.put("seatLocation", passenger.getSeatLocation());
                passengerData.put("speedQueue", passenger.getSpeedQueue());
                passengerData.put("speedExiting", passenger.getSpeedExiting());
                passengerData.put("hasLuggage", passenger.isHasLuggage());
                passengerData.put("luggageLocation",
                    passenger.getLuggageLocation() != null ? passenger.getLuggageLocation() : null);
                passengerData.put("luggagePickupTime",
                    passenger.getLuggagePickUpTime() != null ? passenger.getLuggagePickUpTime() : null);
                passengerData.put("totalDeplaningTime", random.nextInt(10, 200));
                passengerData.put("timeSpentInQueue", random.nextInt(1, 100));

                Map<String, Integer> deplaningGroups = new LinkedHashMap<>();
                for (Map.Entry<String, OptimizerResult> entry : results.entrySet()) {
                    String methodName = entry.getKey();
                    OptimizerResult result = entry.getValue();

                    int groupNumber = findPassengerGroup(passenger, passengers, result.getPassengerGroups());
                    deplaningGroups.put(methodName, groupNumber);
                }
                passengerData.put("deplaningGroups", deplaningGroups);

                passengersList.add(passengerData);
            }
            jsonData.put("passengers", passengersList);

            objectMapper.writeValue(outputPath.toFile(), jsonData);

            log.info("JSON file saved successfully at: {}", outputPath.toAbsolutePath());

        } catch (IOException e) {
            log.error("Failed to save JSON file: {}", outputPath, e);
            throw new RuntimeException("Could not save JSON file", e);
        }
    }

    private int findPassengerGroup(Passenger passenger, List<Passenger> allPassengers,
                                   List<List<Integer>> passengerGroups) {

        int passengerIndex = allPassengers.indexOf(passenger);

        if (passengerIndex == -1) {
            return 0;
        }

        int groupNumber = 1;
        for (List<Integer> group : passengerGroups) {
            if (group.contains(passengerIndex)) {
                return groupNumber;
            }
            groupNumber++;
        }

        return 0;
    }
}

