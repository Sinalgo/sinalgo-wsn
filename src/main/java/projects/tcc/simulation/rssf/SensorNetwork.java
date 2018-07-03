package projects.tcc.simulation.rssf;

import projects.tcc.simulation.data.SensorHolder;

import java.util.Objects;

public class SensorNetwork {

    public static void init() {
        computeDistances();
        computeNeighbors();
    }

    private static void computeDistances() {
        SensorHolder.getAllSensorsAndSinks().values().forEach(s -> SensorHolder.getAllSensorsAndSinks().values()
                .forEach(s2 -> {
                    if (!Objects.equals(s, s2)) {
                        s.getDistances().computeIfAbsent(s2.getID(), v ->
                                s2.getDistances().computeIfAbsent(s.getID(), v2 ->
                                        s.getPosition().distanceTo(s2.getPosition())));
                    }
                }));
    }

    private static void computeNeighbors() {
        SensorHolder.getAvailableSensors().values().forEach(s -> s.getDistances().forEach((k, v) -> {
            if (Double.compare(v, s.getCommRadius()) < 0) {
                s.getNeighbors().put(k, v);
            }
        }));
    }

}
