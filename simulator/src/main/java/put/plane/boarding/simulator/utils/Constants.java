package put.plane.boarding.simulator.utils;

import lombok.experimental.UtilityClass;

import java.util.function.BooleanSupplier;

@UtilityClass
public class Constants {

    public static final BooleanSupplier ALWAYS_TRUE = () -> true;
    public static final Runnable DO_NOTHING = () -> {
    };
}
