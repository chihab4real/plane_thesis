package put.plane.boarding.service.problem.factory;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import put.plane.boarding.service.agent.Agent;
import put.plane.boarding.service.problem.DeplainingProblem;
import put.plane.boarding.service.problem.factory.agent.AgentFactory;
import put.plane.boarding.service.problem.factory.file.FileFactory;

import java.util.ArrayList;

import static put.plane.boarding.service.utils.PairUtils.generateUniquePairs;

@RequiredArgsConstructor
public class DeplainingProblemFactory {

    private final FileFactory fileFactory;
    private final AgentFactory agentFactory;

    public DeplainingProblem create(int numberOfPassengers, int numberOfRows, int numberOfColumns) {

        var result = new DeplainingProblem();
        var maximumNumberOfSeats = numberOfRows * numberOfColumns;

        Preconditions.checkState(numberOfPassengers <= maximumNumberOfSeats, "Too many passengers (" + numberOfPassengers + ") to fit to plane with " + maximumNumberOfSeats + " seats");
        Preconditions.checkState(numberOfColumns % 2 == 0, "Number of columns should be even");

        result.setRows(numberOfRows);
        result.setFiles(fileFactory.create(numberOfColumns));

        var agents = new ArrayList<Agent>();

        generateUniquePairs(numberOfPassengers, numberOfRows, numberOfColumns).forEach(pair -> {
            agents.add(agentFactory.create(pair.getLeft(), result.getFiles().get(pair.getRight())));
        });

        result.setAgents(agents);

        return result;
    }
}
