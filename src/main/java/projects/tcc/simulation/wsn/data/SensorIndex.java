package projects.tcc.simulation.wsn.data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SensorIndex {

    private static final Map<Class<? extends Sensor>, AtomicInteger> SENSOR_TYPE_INDEXES = new HashMap<>();

    public static void reset() {
        SENSOR_TYPE_INDEXES.clear();
    }

    public static int getNextIndex(Class<? extends Sensor> clazz) {
        return SENSOR_TYPE_INDEXES.computeIfAbsent(clazz, k -> new AtomicInteger()).getAndIncrement();
    }

}
