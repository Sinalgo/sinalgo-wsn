package projects.tcc.simulation.rssf;

import lombok.Getter;
import projects.tcc.simulation.rssf.sensor.Sensor;
import projects.tcc.simulation.rssf.sensor.Sink;

import java.util.HashMap;
import java.util.Map;

public class SensorCollection {

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
    private static final Map<Long, Sensor> currentRoundActivatedSensors = new HashMap<>();

    @Getter
    private static final Map<Long, Sensor> currentRoundDeactivatedSensors = new HashMap<>();

    @Getter
    private static final Map<Long, Sensor> currentRoundFailedSensors = new HashMap<>();

    private static void updateCollections(Sensor s) {
        if (s.isFailed()) {
            addToFailedSensors(s);
        } else if (s.isActive()) {
            addToActiveSensors(s);
        } else {
            addToInactiveSensors(s);
        }
    }

    private static <K, V> boolean put(Map<K, V> map, K key, V value) {
        if (map.containsKey(key)) {
            return false;
        }
        map.put(key, value);
        return true;
    }

    private static void removeFromActiveSensors(Sensor s) {
        getActiveSensors().remove(s.getID());
        getCurrentRoundActivatedSensors().remove(s.getID());
    }

    private static void removeFromInactiveSensors(Sensor s) {
        getInactiveSensors().remove(s.getID());
        getCurrentRoundDeactivatedSensors().remove(s.getID());
    }

    private static void addToActiveSensors(Sensor s) {
        removeFromInactiveSensors(s);
        if (put(getActiveSensors(), s.getID(), s)) {
            getCurrentRoundActivatedSensors().put(s.getID(), s);
        }
    }

    private static void addToInactiveSensors(Sensor s) {
        removeFromActiveSensors(s);
        if (put(getInactiveSensors(), s.getID(), s)) {
            getCurrentRoundDeactivatedSensors().put(s.getID(), s);
        }
    }

    private static void addToFailedSensors(Sensor s) {
        removeFromActiveSensors(s);
        removeFromInactiveSensors(s);
        getAvailableSensors().remove(s.getID());
        if (put(getFailedSensors(), s.getID(), s)) {
            getCurrentRoundFailedSensors().put(s.getID(), s);
        }
        s.getCoveredPoints().forEach(p -> {
            p.getCoveringSensors().remove(s);
            p.getConnectedCoveringSensors().remove(s);
            if (p.getCoveringSensors().isEmpty()) {
                Environment.getCoveredPoints().remove(p);
            }
            if (p.getConnectedCoveringSensors().isEmpty()) {
                Environment.getConnectedCoveredPoints().remove(p);
            }
        });
        s.getNeighbors().values().forEach(s2 -> s2.getNeighbors().remove(s.getID()));
    }

    public static void addSensor(Sensor sensor) {
        getAllSensorsAndSinks().put(sensor.getID(), sensor);
        if (sensor instanceof Sink) {
            getSinks().put(sensor.getID(), (Sink) sensor);
        } else {
            getAvailableSensors().put(sensor.getID(), sensor);
        }
    }

    public static void update(Sensor s) {
        updateCollections(s);
    }

    public static void clearRoundSpecificMaps() {
        getCurrentRoundActivatedSensors().clear();
        getCurrentRoundDeactivatedSensors().clear();
        getCurrentRoundFailedSensors().clear();
    }

    public static void clear() {
        clearRoundSpecificMaps();
        getAvailableSensors().clear();
        getActiveSensors().clear();
        getInactiveSensors().clear();
        getFailedSensors().clear();
    }

}
