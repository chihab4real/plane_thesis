package put.plane.boarding.service.problem.factory.file.naming.impl;

import put.plane.boarding.service.problem.factory.file.naming.FileNamingStrategy;

public class DefaultFileNamingStrategy implements FileNamingStrategy {

    @Override
    public String name(int index) {
        return String.valueOf((char) (65 + index));
    }
}
