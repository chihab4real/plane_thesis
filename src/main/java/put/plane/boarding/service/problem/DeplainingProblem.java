package put.plane.boarding.service.problem;

import lombok.Data;
import put.plane.boarding.service.agent.Agent;
import put.plane.boarding.service.agent.File;

import java.util.List;

@Data
public class DeplainingProblem {

    private List<Agent> agents;
    private List<File> files;
    private int rows;
}
