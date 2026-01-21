package put.plane.boarding;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import put.plane.boarding.optimizing.Optimizer;
import put.plane.boarding.optimizing.OptimizerResult;
import put.plane.boarding.optimizing.optimizers.advanced.GeneticAlgorithmOptimizer;
import put.plane.boarding.optimizing.optimizers.advanced.SimulatedAnnealingOptimizer;
import put.plane.boarding.optimizing.optimizers.advanced.TabuSearchOptimizer;
import put.plane.boarding.optimizing.optimizers.basic.*;
import put.plane.boarding.passengers.generator.Flight;
import put.plane.boarding.passengers.generator.Passenger;
import put.plane.boarding.passengers.generator.PassengerGenerator;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.factory.PlaneFactory;
import put.plane.boarding.simulator.simulator.Simulator;
import put.plane.boarding.simulator.simulator.frame.CSVService;
import put.plane.boarding.simulator.simulator.frame.JSONService;
import put.plane.boarding.simulator.simulator.frame.XMLService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class Application {

    @Setter(onMethod_ = @Autowired)
    private PlaneFactory planeFactory;

    @Setter(onMethod_ = @Autowired)
    private XMLService xmlService;

    @Setter(onMethod_ = @Autowired)
    private CSVService csvService;

    @Setter(onMethod_ = @Autowired)
    private JSONService jsonService;

    @Setter(onMethod_ = @Autowired)
    private GeneticAlgorithmOptimizer geneticAlgorithmOptimizer;

    @Setter(onMethod_ = @Autowired)
    private SimulatedAnnealingOptimizer simulatedAnnealingOptimizer;

    @Setter(onMethod_ = @Autowired)
    private TabuSearchOptimizer tabuSearchOptimizer;

    @Setter(onMethod_ = @Autowired)
    private AisleMiddleWindowOptimizer aisleMiddleWindowOptimizer;

    @Setter(onMethod_ = @Autowired)
    private RowBasedOptimizer rowBasedOptimizer;

    @Setter(onMethod_ = @Autowired)
    private ZigZagOptimizer zigZagOptimizer;



    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        Application application = context.getBean(Application.class);
        application.run();
    }

    public void run() {

        int startFlightIndex = 1;
        int endFlightIndex = 200;
        List<Flight> allFlights = readFlightsDataCSV();

        List<Flight> flightsToSimulate = splitFlights(allFlights, startFlightIndex, endFlightIndex);

        String path = createDirectoryIfNotExists("flights_" + startFlightIndex + "_to_" + endFlightIndex);

        int counter = 1;
        for (Flight flight : flightsToSimulate) {
            log.info("FLIGHT: {"+counter+"} - " + flight.getFlightNumber());
            counter++;
            Plane plane = planeFactory.create(flight.getPlaneRows(), flight.getPlaneColumns(), flight.isDoubleExit());
            List<Passenger> generatedPassengers = flight.getPassengers();

            log.info("\tRunning AMW");
            OptimizerResult aileMiddleWindowResult = aisleMiddleWindowOptimizer.run(plane, generatedPassengers, path, false);

            log.info("\tRunning FB");
            rowBasedOptimizer.setFrontToBack(true);
            OptimizerResult frontToBackResult = rowBasedOptimizer.run(plane, generatedPassengers, path, false);

            log.info("\tRunning BF");
            rowBasedOptimizer.setFrontToBack(false);
            OptimizerResult backToFront = rowBasedOptimizer.run(plane, generatedPassengers, path, false);

            log.info("\tRunning ZZ");
            OptimizerResult zigZagResult = zigZagOptimizer.run(plane, generatedPassengers, path, false);

            log.info("\tRunning GA");
            OptimizerResult gaResult = geneticAlgorithmOptimizer.run(false, plane, generatedPassengers, path,
                    50, 30,
                    0.2, 0.8);

            log.info("\tRunning SA");
            OptimizerResult saResult = simulatedAnnealingOptimizer.run(false, plane, generatedPassengers, path,
                    100.0, 0.995, 500);

            log.info("\tRunning TS");
            OptimizerResult tsResult = tabuSearchOptimizer.run(false, plane, generatedPassengers, path,
                    500, 10, 20);

            List<OptimizerResult> results = List.of(
                    aileMiddleWindowResult,
                    frontToBackResult,
                    backToFront,
                    zigZagResult,
                    gaResult,
                    saResult,
                    tsResult
            );

            csvService.appendOptimizationResultsToCSV(generatedPassengers,results, path, "optimization_summary.csv", flight.getFlightNumber());
            csvService.appendOptimizationSummary(results, path, "overall_summary.csv", flight.getFlightNumber());
        }

    }

    private String createDirectoryIfNotExists(String name) {
        Path path = Paths.get("simulation_results", name);

        try {
            if (Files.notExists(path)) {
                Files.createDirectories(path);
                log.info("Directory created at: " + path.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Failed to create directory: " + path, e);
            throw new RuntimeException("Could not initialize simulation directory", e);
        }

        return path.toAbsolutePath().toString();
    }


    private List<Passenger> generatePassengers(Plane plane, int passengersLuggagePercentage) {
        return PassengerGenerator.generatePassengers(
                plane.getRows(),
                plane.getColumns(),
                plane.getRows() * plane.getColumns(),
                passengersLuggagePercentage
        );
    }

    public static List<Flight> readFlightsDataCSV() {
        Path inputPath = Paths.get("generated_flights/", "generated_passengers.csv");
        // LinkedHashMap keeps the flights in the order they appear in the file
        Map<String, Flight> flightMap = new LinkedHashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(inputPath)) {
            String header = reader.readLine();
            if (header == null) return new ArrayList<>();

            String line;
            while ((line = reader.readLine()) != null) {
                // The "-1" limit is CRITICAL: it prevents Java from skipping empty trailing columns
                String[] columns = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)") ;

                // 1. Parse Flight Data
                String flightNumber = columns[0];
                int rows = Integer.parseInt(columns[1]);
                int cols = Integer.parseInt(columns[2]);
                int totalPassengers = Integer.parseInt(columns[3]);
                int luggagePerc = Integer.parseInt(columns[4]);
                boolean doubleExit = Boolean.parseBoolean(columns[5]);

                // 2. Parse Passenger Data
                String pId = columns[6];
                String seat = unescapeCSV(columns[7]);
                Integer speedQ = Integer.parseInt(columns[8]);
                Integer speedE = Integer.parseInt(columns[9]);
                boolean hasLuggage = Boolean.parseBoolean(columns[10]);

                // Handle empty Luggage Location
                String luggageLoc = (columns.length > 11 && !columns[11].isEmpty()) ? unescapeCSV(columns[11]) : null;

                // Handle empty Luggage Pickup Time
                Integer luggageTime = null;
                if (columns.length > 12 && !columns[12].trim().isEmpty()) {
                    luggageTime = Integer.parseInt(columns[12].trim());
                }

                Passenger passenger = new Passenger(pId, seat, speedQ, speedE, hasLuggage, luggageLoc, luggageTime);

                // 3. Grouping Logic
                flightMap.computeIfAbsent(flightNumber, k -> new Flight(
                        flightNumber, rows, cols, totalPassengers, luggagePerc, doubleExit, new ArrayList<>()
                )).getPassengers().add(passenger);
            }
        } catch (Exception e) {
            System.err.println("Critical error reading CSV: " + e.getMessage());
        }

        return new ArrayList<>(flightMap.values());
    }

    private static String unescapeCSV(String value) {
        if (value == null || value.isEmpty()) return "";
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
            return value.replace("\"\"", "\"");
        }
        return value;
    }

    public void runOptimizationForFlights(List<Flight> flights) {
        for (Flight flight : flights) {

        }
    }

    public List<Flight> splitFlights(List<Flight> flights, int startIndex, int endIndex) {
        List<Flight> splitFlights = new ArrayList<>();
        for (int i = startIndex-1; i <= endIndex-1 && i < flights.size(); i++) {
            splitFlights.add(flights.get(i));
        }
        return splitFlights;
    }

}
