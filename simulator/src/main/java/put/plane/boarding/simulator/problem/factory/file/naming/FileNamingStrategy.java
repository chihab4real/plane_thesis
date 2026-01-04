package put.plane.boarding.simulator.problem.factory.file.naming;

import put.plane.boarding.simulator.plane.structure.FileSide;

public interface FileNamingStrategy {

    String name(int index);

    FileSide side(int index, int maxColumns);
}
