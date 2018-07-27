package projects.tcc.simulation.rssf;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import projects.tcc.simulation.rssf.sensor.Sensor;

import java.util.HashSet;
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
    private static Set<RSSFPosition> connectedCoveredPoints;

    @Getter
    @Setter(AccessLevel.PRIVATE)
    private static Set<RSSFPosition> coveredPoints;

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
        setConnectedCoveredPoints(new HashSet<>());
        setCurrentCoverage(0);
        setDisconnectedCoverage(0);
        for (long i = 0; i < getHeight(); i++) {
            for (long j = 0; j < getWidth(); j++) {
                getPoints().add(new RSSFPosition(i + 0.5, j + 0.5, 0));
            }
        }
    }

    public static void updateCoverage(Sensor s) {
        if (s.isActive()) {
            if (s.isConnected()) {
                s.getCoveredPoints().forEach(p -> p.getConnectedCoveringSensors().add(s));
                getConnectedCoveredPoints().addAll(s.getCoveredPoints());
            } else {
                s.getCoveredPoints().forEach(p -> {
                    p.getConnectedCoveringSensors().remove(s);
                    if (p.getConnectedCoveringSensors().isEmpty()) {
                        getConnectedCoveredPoints().remove(p);
                    }
                });
            }
            s.getCoveredPoints().forEach(p -> p.getCoveringSensors().add(s));
            getCoveredPoints().addAll(s.getCoveredPoints());
        } else {
            s.getCoveredPoints().forEach(p -> {
                p.getConnectedCoveringSensors().remove(s);
                p.getCoveringSensors().remove(s);
                if (p.getConnectedCoveringSensors().isEmpty()) {
                    getConnectedCoveredPoints().remove(p);
                }
                if (p.getCoveringSensors().isEmpty()) {
                    getCoveredPoints().remove(p);
                }
            });
        }
        setCurrentCoverage(
                ((double) getConnectedCoveredPoints().size()) / ((double) getPoints().size()));
        setDisconnectedCoverage(
                ((double) getCoveredPoints().size()) / ((double) getPoints().size()));
    }

    public static void updateExclusivelyCoveredPoints(Sensor s) {
        s.getExclusivelyCoveredPoints().clear();
        s.getCoveredPoints().forEach(p -> {
            if (p.getCoveringSensors().isEmpty()) {
                s.getExclusivelyCoveredPoints().add(p);
            }
        });
    }

}
