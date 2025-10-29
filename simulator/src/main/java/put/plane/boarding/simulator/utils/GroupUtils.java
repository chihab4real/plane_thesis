package put.plane.boarding.simulator.utils;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Used to create groups of objects
 */
@UtilityClass
public class GroupUtils {

    public <T> List<List<T>> singleGroup(final List<T> list) {
        List<List<T>> result = new ArrayList<>();
        result.add(list);
        return result;
    }

    public <T, D> List<List<T>> customGroups(final List<T> list, Function<T, D> mapFunction) {

        return new ArrayList<>(list
                .stream()
                .collect(Collectors.groupingBy(mapFunction))
                .values());
    }
}
