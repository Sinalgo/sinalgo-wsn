package projects.tcc.simulation.rssf;

import projects.tcc.simulation.data.SensorHolder;

import java.util.Collection;
import java.util.Objects;

public class SensorNetwork {

    public static void init() {
        computeDistances();
        computeNeighbors();
    }

    private static void computeDistances() {
        Collection<Sensor> allSensorsAndSinks = SensorHolder.getAllSensorsAndSinks().values();
        allSensorsAndSinks.forEach(s1 -> allSensorsAndSinks.forEach(s2 -> computeDistance(s1, s2)));
    }

    private static void computeDistance(Sensor s1, Sensor s2) {
        if (!Objects.equals(s1, s2)) {
            s1.getDistances().computeIfAbsent(s2.getID(), v ->
                    s2.getDistances().computeIfAbsent(s1.getID(), v2 ->
                            s1.getPosition().distanceTo(s2.getPosition())));
        }
    }

    private static void computeNeighbors() {
        SensorHolder.getAvailableSensors().values().forEach(s -> s.getDistances().forEach((k, v) -> {
            if (Double.compare(v, s.getCommRadius()) < 0) {
                s.getNeighbors().put(k, v);
            }
        }));
    }

}
