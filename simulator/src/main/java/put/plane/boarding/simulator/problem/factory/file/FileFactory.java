package put.plane.boarding.simulator.problem.factory.file;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import put.plane.boarding.simulator.plane.structure.File;
import put.plane.boarding.simulator.problem.factory.file.naming.FileNamingStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class FileFactory {

    private final FileNamingStrategy fileNamingStrategy;

    public List<File> create(int numberOfFiles) {
        var result = new ArrayList<File>();

        IntStream.range(0, numberOfFiles).forEach(i -> {
            var file = new File(fileNamingStrategy.name(i), distance(i, numberOfFiles));
            result.add(file);
        });

        return result;
    }

    private int distance(int index, int numberOfFiles) {
        var center = (numberOfFiles + 1) / 2;

        return index >= center ?
                index - center + 1 :
                center - index;
    }
}
