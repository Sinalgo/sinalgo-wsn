package projects.wsn.simulation.network.data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SensorIndex {


    private static SensorIndex currentInstance;

    public static SensorIndex currentInstance() {
        if (currentInstance == null) {
            return newInstance();
        }
        return currentInstance;
    }

    public static SensorIndex newInstance() {
        currentInstance = new SensorIndex();
        return currentInstance;
    }

    private final Map<Class<? extends Sensor>, AtomicInteger> SENSOR_TYPE_INDEXES = new HashMap<>();

    public int getNextIndex(Class<? extends Sensor> clazz) {
        return SENSOR_TYPE_INDEXES.computeIfAbsent(clazz, k -> new AtomicInteger()).getAndIncrement();
    }

    public int getIndexFor(Class<? extends Sensor> clazz) {
        return SENSOR_TYPE_INDEXES.computeIfAbsent(clazz, k -> new AtomicInteger()).get();
    }

}
