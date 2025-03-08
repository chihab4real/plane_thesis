package put.plane.boarding.service.utils;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class PairUtils {

    public static List<Pair<Integer, Integer>> generateUniquePairs(int numberOfPairs, int maximumValue1, int maximumValue2) {

        var result = new ArrayList<Pair<Integer, Integer>>();

        IntStream.range(0, maximumValue1).forEach(i -> {
            IntStream.range(0, maximumValue2).forEach(j -> {
                result.add(Pair.of(i, j));
            });
        });

        Collections.shuffle(result);

        return result
                .stream()
                .limit(numberOfPairs)
                .toList();
    }
}
