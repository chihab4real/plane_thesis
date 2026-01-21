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
        Plane plane = planeFactory.create(30, 6, true);

//        for(int i=100;i>=20;i-=5) {
            log.info("===== SIMULATION FOR {}% OF PASSENGERS WITH LUGGAGE =====", 100);
            String path = createDirectoryIfNotExists();
            String flightId = "FLIGHT_" + DateFormatUtils.format(new Date(), "yyyyMMdd_HHmmss");

            List<Passenger> generatedPassengers = generatePassengers(plane,100);
            xmlService.saveGeneratedPassengers(generatedPassengers, path);

            Map<String, OptimizerResult> allResults = new LinkedHashMap<>();

            log.info("Running Basic Optimizers\n");

            log.info("Running Aisle-Middle-Window Optimizer");
            OptimizerResult aileMiddleWindowResult = aisleMiddleWindowOptimizer.run(plane, generatedPassengers, path, false);
            log.info("Result: {}\n\n", aileMiddleWindowResult);
            csvService.saveOptimizationResultsToCSV(generatedPassengers, aileMiddleWindowResult.passengerGroups(),
                    path, "AisleMiddleWindow", flightId);
            allResults.put("AisleMiddleWindow", aileMiddleWindowResult);
            log.info("==Aisle-Middle-Window optimization completed.==\n");

            log.info("Running FrontToBack Optimizer");
            rowBasedOptimizer.setFrontToBack(true);
            OptimizerResult frontToBackResult = rowBasedOptimizer.run(plane, generatedPassengers, path, false);
            log.info("Result: {}\n\n", frontToBackResult);
            csvService.saveOptimizationResultsToCSV(generatedPassengers, frontToBackResult.passengerGroups(),
                    path, "FrontToBack", flightId);
            allResults.put("FrontToBack", frontToBackResult);
            log.info("==FrontToBack optimization completed.==\n");

            log.info("Running BackToFront Optimizer");
            rowBasedOptimizer.setFrontToBack(false);
            OptimizerResult rowBasedResult = rowBasedOptimizer.run(plane, generatedPassengers, path, false);
            log.info("Result: {}\n\n", rowBasedResult);
            csvService.saveOptimizationResultsToCSV(generatedPassengers, rowBasedResult.passengerGroups(),
                    path, "BackToFront", flightId);
            allResults.put("BackToFront", rowBasedResult);
            log.info("==BackToFront optimization completed.==\n");

            log.info("Running Zig Zag Optimizer");
            OptimizerResult zigZagResult = zigZagOptimizer.run(plane, generatedPassengers, path, false);
            log.info("Result: {}\n\n", zigZagResult);
            csvService.saveOptimizationResultsToCSV(generatedPassengers, zigZagResult.passengerGroups(),
                    path, "Zigzag", flightId);
            allResults.put("Zigzag", zigZagResult);
            log.info("==Zig Zag optimization completed.==\n");


            log.info("Running Advanced Optimizers\n");
//
            log.info("Genetic Algorithm Optimization\n");
            OptimizerResult gaResult = geneticAlgorithmOptimizer.run(false, plane, generatedPassengers, path, 50, 30,
                    0.2, 0.8);
            log.info("Best time from GA: {}\n\n", gaResult);
            csvService.saveOptimizationResultsToCSV(generatedPassengers, gaResult.passengerGroups(),
                    path, "GeneticAlgorithm", flightId);
            allResults.put("GeneticAlgorithm", gaResult);
            log.info("==Genetic Algorithm optimization completed.==\n");

            log.info("Simulated Annealing Optimization\n");
            OptimizerResult saResult = simulatedAnnealingOptimizer.run(false, plane, generatedPassengers, path, 100.0, 0.995, 500);
            log.info("Best time from SA: {}\n\n", saResult);
            csvService.saveOptimizationResultsToCSV(generatedPassengers, saResult.passengerGroups(),
                    path, "SimulatedAnnealing", flightId);
            allResults.put("SimulatedAnnealing", saResult);
            log.info("==Simulated Annealing optimization completed.==\n");

            log.info("Tabu Search Optimization\n");
            OptimizerResult tsResult = tabuSearchOptimizer.run(false, plane, generatedPassengers, path, 500, 10, 10);
            log.info("Best time from TS: {}\n\n", tsResult);
            csvService.saveOptimizationResultsToCSV(generatedPassengers, tsResult.passengerGroups(),
                    path, "TabuSearch", flightId);
            allResults.put("TabuSearch", tsResult);
            log.info("==Tabu Search optimization completed.==\n");

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
//        }
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


    private List<Passenger> generatePassengers(Plane plane, int passengersLuggagePercentage) {
        return PassengerGenerator.generatePassengers(
                plane.getRows(),
                plane.getColumns(),
                plane.getRows() * plane.getColumns(),
                passengersLuggagePercentage
        );
    }

}
