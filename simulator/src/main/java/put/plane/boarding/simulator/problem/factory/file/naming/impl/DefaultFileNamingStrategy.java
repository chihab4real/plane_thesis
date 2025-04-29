package put.plane.boarding.simulator.problem.factory.file.naming.impl;

import org.springframework.stereotype.Service;
import put.plane.boarding.simulator.problem.factory.file.naming.FileNamingStrategy;

@Service
public class DefaultFileNamingStrategy implements FileNamingStrategy {

    @Override
    public String name(int index) {
        return String.valueOf((char) (65 + index));
    }
}
