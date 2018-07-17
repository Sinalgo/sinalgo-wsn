package projects.tcc.simulation.data;

import lombok.Getter;
import projects.tcc.simulation.rssf.sensor.Sensor;
import projects.tcc.simulation.rssf.sensor.Sink;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SensorHolder {

    private static final Consumer<Sensor> SENSOR_EVALUATOR = s -> {
        if (s.isFailed()) {
            addToFailedSensors(s);
        } else if (s.isActive()) {
            addToActiveSensors(s);
        } else {
            addToInactiveSensors(s);
        }
    };

    private static void removeFromActiveSensors(Sensor s) {
        getActiveSensors().remove(s.getID());
        getPreviousRoundActivatedSensors().remove(s.getID());
    }

    private static void removeFromInactiveSensors(Sensor s) {
        getInactiveSensors().remove(s.getID());
        getPreviousRoundDeactivatedSensors().remove(s.getID());
    }

    private static void addToActiveSensors(Sensor s) {
        removeFromInactiveSensors(s);
        getActiveSensors().put(s.getID(), s);
        getPreviousRoundActivatedSensors().put(s.getID(), s);
    }

    private static void addToInactiveSensors(Sensor s) {
        removeFromActiveSensors(s);
        getInactiveSensors().put(s.getID(), s);
        getPreviousRoundDeactivatedSensors().put(s.getID(), s);
    }

    private static void addToFailedSensors(Sensor s) {
        removeFromActiveSensors(s);
        removeFromInactiveSensors(s);
        getAvailableSensors().remove(s.getID());
        getFailedSensors().put(s.getID(), s);
        getPreviousRoundFailedSensors().put(s.getID(), s);
    }

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


    public static void addSensor(Sensor sensor) {
        getAllSensorsAndSinks().put(sensor.getID(), sensor);
        if (sensor instanceof Sink) {
            getSinks().put(sensor.getID(), (Sink) sensor);
        } else {
            getAvailableSensors().put(sensor.getID(), sensor);
        }
    }

    public static void update() {
        getAvailableSensors().values().forEach(Sensor::updateState);
        clearRoundSpecificMaps();
        updateCollections();
        removeFailedSensorsFromNeighborhoods();
    }

    public static void updateCollections() {
        getAvailableSensors().values().forEach(SENSOR_EVALUATOR);
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
