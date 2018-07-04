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
    @Setter(AccessLevel.PRIVATE)
    private static double availableEnergy;

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

    @Getter
    private static final Map<Long, Sensor> allSensorsAndSinks = new HashMap<>();

    @Getter
    private static final Map<Long, Sink> sinks = new HashMap<>();

    @Getter
    private static final Map<Long, Sensor> availableSensors = new HashMap<>();

    @Getter
    private static final Map<Long, Sensor> activeSensors = new HashMap<>();

    @Getter
    private static final Map<Long, Sensor> inactiveSensors = new HashMap<>();

    @Getter
    private static final Map<Long, Sensor> failedSensors = new HashMap<>();

    public static void addSensors(Sensor sensor) {
        getAllSensorsAndSinks().put(sensor.getID(), sensor);
        if (sensor instanceof Sink) {
            getSinks().put(sensor.getID(), (Sink) sensor);
        } else {
            getAvailableSensors().put(sensor.getID(), sensor);
        }
    }

    public static void updateSensors() {
        getActiveSensors().values().forEach(Sensor::updateState);
        getAvailableSensors().values().removeIf(FAILED_PREDICATE);
        getActiveSensors().values().removeIf(FAILED_PREDICATE);
        getInactiveSensors().values().removeIf(FAILED_PREDICATE);
        getInactiveSensors().values().removeIf(ACTIVATED_PREDICATE);
        getActiveSensors().values().removeIf(INACTIVATED_PREDICATE);
        updateAggregateEnergy();
    }

    private static void updateAggregateEnergy() {
        setAvailableEnergy(0);
        getAvailableSensors().values().forEach(s -> setAvailableEnergy(getAvailableEnergy() + s.getBatteryEnergy()));
    }

    public static void clear() {
        setAvailableEnergy(0);
        getAvailableSensors().clear();
        getActiveSensors().clear();
        getInactiveSensors().clear();
        getFailedSensors().clear();
    }

}
