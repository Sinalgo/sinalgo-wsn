package projects.tcc.simulation.data;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import projects.tcc.simulation.rssf.Sensor;
import projects.tcc.simulation.rssf.Sink;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class SensorHolder {

    @Getter
    @Setter
    private static Sink currentSink;

    private static final Predicate<Sensor> FAILED_PREDICATE = s -> {
        if (s.isFailed()) {
            getFailedSensors().put(s.getID(), s);
            return true;
        }
        return false;
    };

    private static final Predicate<Sensor> ACTIVATED_PREDICATE = s -> {
        if (s.isActive()) {
            getActiveSensors().put(s.getID(), s);
            return true;
        }
        return false;
    };

    private static final Predicate<Sensor> INACTIVATED_PREDICATE = s -> {
        if (!s.isActive()) {
            getInactiveSensors().put(s.getID(), s);
            return true;
        }
        return false;
    };

    @Getter(AccessLevel.PRIVATE)
    private static final Map<Long, Sensor> availableSensors = new HashMap<>();

    @Getter(AccessLevel.PRIVATE)
    private static final Map<Long, Sensor> activeSensors = new HashMap<>();

    @Getter(AccessLevel.PRIVATE)
    private static final Map<Long, Sensor> inactiveSensors = new HashMap<>();

    @Getter(AccessLevel.PRIVATE)
    private static final Map<Long, Sensor> failedSensors = new HashMap<>();

    public static void addSensors(Sensor sensor) {
        getAvailableSensors().put(sensor.getID(), sensor);
    }

    public static void updateSensorsState() {
        getAvailableSensors().values().removeIf(FAILED_PREDICATE);
        getActiveSensors().values().removeIf(FAILED_PREDICATE);
        getInactiveSensors().values().removeIf(FAILED_PREDICATE);
        getInactiveSensors().values().removeIf(ACTIVATED_PREDICATE);
        getActiveSensors().values().removeIf(INACTIVATED_PREDICATE);
    }

}
