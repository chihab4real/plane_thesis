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
import put.plane.boarding.passengers.generator.Passenger;
import put.plane.boarding.passengers.generator.PassengerGenerator;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.factory.PlaneFactory;
import put.plane.boarding.simulator.simulator.Simulator;
import put.plane.boarding.simulator.simulator.frame.CSVService;
import put.plane.boarding.simulator.simulator.frame.JSONService;
import put.plane.boarding.simulator.simulator.frame.XMLService;

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
        // creating example problem
        Plane plane = planeFactory.create(10, 6);

        String path = createDirectoryIfNotExists();
        String flightId = "FLIGHT_" + DateFormatUtils.format(new Date(), "yyyyMMdd_HHmmss");

        List<Passenger> generatedPassengers = generatePassengers(plane);
        xmlService.saveGeneratedPassengers(generatedPassengers, path);

        Map<String, OptimizerResult> allResults = new LinkedHashMap<>();

        log.info("Running Basic Optimizers\n");

        log.info("Running Aisle-Middle-Window Optimizer");
        OptimizerResult aileMiddleWindowResult = aisleMiddleWindowOptimizer.run(plane, generatedPassengers, path, true);
        log.info("Result: {}\n\n", aileMiddleWindowResult);
        csvService.saveOptimizationResultsToCSV(generatedPassengers, aileMiddleWindowResult.passengerGroups(),
                path, "AisleMiddleWindow", flightId);
        allResults.put("AisleMiddleWindow", aileMiddleWindowResult);

        log.info("Running Row Based Optimizer");
        OptimizerResult rowBasedResult = rowBasedOptimizer.run(plane, generatedPassengers, path, true);
        log.info("Result: {}\n\n", rowBasedResult);
        csvService.saveOptimizationResultsToCSV(generatedPassengers, rowBasedResult.passengerGroups(),
                path, "BackToFront", flightId);
        allResults.put("BackToFront", rowBasedResult);

        log.info("Running Zig Zag Optimizer");
        OptimizerResult zigZagResult = zigZagOptimizer.run(plane, generatedPassengers, path, true);
        log.info("Result: {}\n\n", zigZagResult);
        csvService.saveOptimizationResultsToCSV(generatedPassengers, zigZagResult.passengerGroups(),
                path, "Zigzag", flightId);
        allResults.put("Zigzag", zigZagResult);


        log.info("Running Advanced Optimizers\n");

        log.info("Genetic Algorithm Optimization\n");
        OptimizerResult gaResult = geneticAlgorithmOptimizer.run(true, plane, generatedPassengers, path, 50, 20,
                0.2, 0.8);
        log.info("Best time from GA: {}\n\n", gaResult);
        csvService.saveOptimizationResultsToCSV(generatedPassengers, gaResult.passengerGroups(),
                path, "GeneticAlgorithm", flightId);
        allResults.put("GeneticAlgorithm", gaResult);

        log.info("Simulated Annealing Optimization\n");
        OptimizerResult saResult = simulatedAnnealingOptimizer.run(true, plane, generatedPassengers, path, 100.0, 0.995, 1000);
        log.info("Best time from SA: {}\n\n", saResult);
        csvService.saveOptimizationResultsToCSV(generatedPassengers, saResult.passengerGroups(),
                path, "SimulatedAnnealing", flightId);
        allResults.put("SimulatedAnnealing", saResult);

        log.info("Tabu Search Optimization\n");
        OptimizerResult tsResult = tabuSearchOptimizer.run(true, plane, generatedPassengers, path, 500, 15, 20);
        log.info("Best time from TS: {}\n\n", tsResult);
        csvService.saveOptimizationResultsToCSV(generatedPassengers, tsResult.passengerGroups(),
                path, "TabuSearch", flightId);
        allResults.put("TabuSearch", tsResult);

        csvService.saveOptimizationSummary(List.of(
                aileMiddleWindowResult,
                rowBasedResult,
                zigZagResult,
                gaResult,
                saResult,
                tsResult
        ), path, "optimization_summary", flightId);

        log.info("\nSaving comprehensive JSON file with all optimization results...");
        jsonService.saveOptimizationResultsToJSON(allResults, generatedPassengers, path, "optimization_results", flightId);
        log.info("All results saved successfully!");
    }

    private String createDirectoryIfNotExists() {
        String currentDateString = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH.mm.ss");
        Path path = Paths.get("simulation_results", currentDateString);

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


    private List<Passenger> generatePassengers(Plane plane) {
        return PassengerGenerator.generatePassengers(
                plane.getRows(),
                plane.getColumns(),
                plane.getRows() * plane.getColumns(),
                50,
                0
        );
    }

}
