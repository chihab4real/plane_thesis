package put.plane.boarding.simulator.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@UtilityClass
public class PairUtils {

    public static List<Pair<Integer, Integer>> generateUniquePairs(int numberOfPairs, int maximumValue1, int maximumValue2) {

        ArrayList<Pair<Integer, Integer>> result = new ArrayList<>();

        IntStream.range(0, maximumValue1).forEach(i -> IntStream.range(0, maximumValue2).forEach(j -> result.add(Pair.of(i, j))));

        Collections.shuffle(result, new Random(42));

        return result
                .stream()
                .limit(numberOfPairs)
                .toList();
    }
}
