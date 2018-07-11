package projects.tcc.simulation.data;

import lombok.Getter;
import projects.tcc.nodes.nodeImplementations.Sensor;
import projects.tcc.nodes.nodeImplementations.Sink;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class SensorHolder {

    private static final Predicate<Sensor> FAILED_PREDICATE = s -> {
        if (s.isFailed()) {
            getFailedSensors().put(s.getID(), s);
            getPreviousRoundFailedSensors().put(s.getID(), s);
            getPreviousRoundActivatedSensors().remove(s.getID());
            getPreviousRoundDeactivatedSensors().remove(s.getID());
            return true;
        }
        return false;
    };

    private static final Predicate<Sensor> ACTIVATED_PREDICATE = s -> {
        if (s.isActive()) {
            getActiveSensors().put(s.getID(), s);
            getPreviousRoundActivatedSensors().put(s.getID(), s);
            getPreviousRoundDeactivatedSensors().remove(s.getID(), s);
            return true;
        }
        return false;
    };

    private static final Predicate<Sensor> INACTIVATED_PREDICATE = s -> {
        if (!s.isActive()) {
            getInactiveSensors().put(s.getID(), s);
            getPreviousRoundDeactivatedSensors().put(s.getID(), s);
            getPreviousRoundActivatedSensors().remove(s.getID());
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

    @Getter
    private static final Map<Long, Sensor> previousRoundActivatedSensors = new HashMap<>();

    @Getter
    private static final Map<Long, Sensor> previousRoundDeactivatedSensors = new HashMap<>();

    @Getter
    private static final Map<Long, Sensor> previousRoundFailedSensors = new HashMap<>();


    public static void addSensors(Sensor sensor) {
        getAllSensorsAndSinks().put(sensor.getID(), sensor);
        if (sensor instanceof Sink) {
            getSinks().put(sensor.getID(), (Sink) sensor);
        } else {
            getAvailableSensors().put(sensor.getID(), sensor);
        }
    }

    public static void update() {
        getActiveSensors().values().forEach(Sensor::updateState);
        clearRoundSpecificMaps();
        updateCollections();
        removeFailedSensorsFromNeighborhoods();
    }

    public static void updateCollections() {
        getAvailableSensors().values().removeIf(FAILED_PREDICATE);
        getActiveSensors().values().removeIf(FAILED_PREDICATE);
        getInactiveSensors().values().removeIf(FAILED_PREDICATE);
        getInactiveSensors().values().removeIf(ACTIVATED_PREDICATE);
        getActiveSensors().values().removeIf(INACTIVATED_PREDICATE);
    }

    public static void clearRoundSpecificMaps() {
        getPreviousRoundActivatedSensors().clear();
        getPreviousRoundDeactivatedSensors().clear();
        getPreviousRoundFailedSensors().clear();
    }

    private static void removeFailedSensorsFromNeighborhoods() {
        if (!getPreviousRoundFailedSensors().isEmpty()) {
            getAvailableSensors().values()
                    .forEach(s -> s.getNeighbors().keySet().removeAll(getPreviousRoundFailedSensors().keySet()));
        }
    }

    public static void clear() {
        clearRoundSpecificMaps();
        getAvailableSensors().clear();
        getActiveSensors().clear();
        getInactiveSensors().clear();
        getFailedSensors().clear();
    }

}
