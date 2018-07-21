package projects.tcc.simulation.rssf;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import projects.tcc.simulation.rssf.sensor.Sensor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Environment {

    @Getter
    @Setter(AccessLevel.PRIVATE)
    private static double area;

    @Getter
    @Setter(AccessLevel.PRIVATE)
    private static long height, width;

    @Getter
    @Setter(AccessLevel.PRIVATE)
    private static double coverageFactor;

    @Getter
    @Setter(AccessLevel.PRIVATE)
    private static Set<RSSFPosition> points;

    @Getter
    @Setter(AccessLevel.PRIVATE)
    private static Set<RSSFPosition> coveredPoints;

    @Getter
    @Setter(AccessLevel.PRIVATE)
    private static Set<RSSFPosition> disconnectedCoveredPoints;

    @Getter
    @Setter(AccessLevel.PRIVATE)
    private static double currentCoverage;

    @Getter
    @Setter(AccessLevel.PRIVATE)
    private static double disconnectedCoverage;

    public static void init(long height, long width, double coverageFactor) {
        setHeight(height);
        setWidth(width);
        setCoverageFactor(coverageFactor);
        setArea(height * width);
        setPoints(new HashSet<>());
        setCoveredPoints(new HashSet<>());
        setDisconnectedCoveredPoints(new HashSet<>());
        setCurrentCoverage(0);
        setDisconnectedCoverage(0);
        for (long i = 0; i < getHeight(); i++) {
            for (long j = 0; j < getWidth(); j++) {
                getPoints().add(new RSSFPosition(i + 0.5, j + 0.5, 0));
            }
        }
    }

    public static void update() {
        updateExclusivelyCoveredPoints();
        updateCoverage();
        updateDisconnectedCoverage();
    }

    public static void updateCoverage() {
        getCoveredPoints().clear();
        SensorHolder.getActiveSensors().values().stream()
                .filter(Sensor::isConnected)
                .map(Sensor::getCoveredPoints)
                .forEach(getCoveredPoints()::addAll);
        setCurrentCoverage(
                ((double) getCoveredPoints().size()) / ((double) getPoints().size()));
    }

    public static void updateDisconnectedCoverage() {
        getDisconnectedCoveredPoints().clear();
        SensorHolder.getActiveSensors().values()
                .forEach(s -> getDisconnectedCoveredPoints().addAll(s.getCoveredPoints()));
        setDisconnectedCoverage(
                ((double) getDisconnectedCoveredPoints().size()) / ((double) getPoints().size()));
    }

    public static void updateExclusivelyCoveredPoints() {
        Collection<Sensor> availableSensors = SensorHolder.getAvailableSensors().values();
        Collection<Sensor> activeSensors = SensorHolder.getActiveSensors().values();
        availableSensors.forEach(s -> {
            s.getExclusivelyCoveredPoints().clear();
            s.getExclusivelyCoveredPoints().addAll(s.getCoveredPoints());
        });
        availableSensors.forEach(s1 -> activeSensors.forEach(s2 -> filterOutCoveredPoints(s1, s2)));
    }

    private static void filterOutCoveredPoints(Sensor s1, Sensor s2) {
        if (!Objects.equals(s1, s2)) {
            s1.getExclusivelyCoveredPoints().removeAll(s2.getCoveredPoints());
        }
    }

}
