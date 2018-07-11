package projects.tcc.simulation.rssf;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import projects.tcc.nodes.nodeImplementations.Sensor;
import projects.tcc.simulation.data.SensorHolder;
import sinalgo.nodes.Position;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Getter
public class Environment {

    private final double area;
    private final long height, width;
    private final double coverageFactor;

    private final List<Position> points;
    private final Set<Position> coveredPoints;
    private final Set<Position> disconnectedCoveredPoints;

    @Setter(AccessLevel.PRIVATE)
    private double currentCoverage;

    @Setter(AccessLevel.PRIVATE)
    private double disconnectedCoverage;

    Environment(long height, long width, double coverageFactor) {
        this.height = height;
        this.width = width;
        this.coverageFactor = coverageFactor;
        this.area = height * width;
        this.points = new ArrayList<>();
        this.coveredPoints = new HashSet<>();
        this.disconnectedCoveredPoints = new HashSet<>();
        this.setCurrentCoverage(0);
        this.setDisconnectedCoverage(0);
        this.generatePoints();
    }

    private void generatePoints() {
        for (long i = 0; i < this.getHeight(); i++) {
            for (long j = 0; j < this.getWidth(); j++) {
                this.getPoints().add(new Position(i + 0.5, j + 0.5, 0));
            }
        }
    }

    public void init() {
        computeCoveredPoints();
        updateExclusivelyCoveredPoints();
    }

    private void computeCoveredPoints() {
        SensorHolder.getAvailableSensors().values()
                .forEach(s -> this.getPoints().forEach(p -> this.computeCoveredPoint(s, p)));
    }

    private void computeCoveredPoint(Sensor s, Position p) {
        if (Double.compare(s.getPosition().distanceTo(p), s.getSensorRadius()) <= 0) {
            s.getCoveredPoints().add(p);
        }
    }

    public void update() {
        updateExclusivelyCoveredPoints();
        updateCoverage();
        updateDisconnectedCoverage();
    }

    public void updateCoverage() {
        this.getCoveredPoints().clear();
        SensorHolder.getActiveSensors().values().stream()
                .filter(Sensor::isConnected)
                .map(Sensor::getCoveredPoints)
                .forEach(this.getCoveredPoints()::addAll);
        this.setCurrentCoverage(
                ((double) this.getCoveredPoints().size()) / ((double) this.getPoints().size()));
    }

    public void updateDisconnectedCoverage() {
        this.getDisconnectedCoveredPoints().clear();
        SensorHolder.getActiveSensors().values()
                .forEach(s -> this.getDisconnectedCoveredPoints().addAll(s.getCoveredPoints()));
        this.setDisconnectedCoverage(
                ((double) this.getDisconnectedCoveredPoints().size()) / ((double) this.getPoints().size()));
    }

    public void updateExclusivelyCoveredPoints() {
        Collection<Sensor> availableSensors = SensorHolder.getAvailableSensors().values();
        availableSensors.forEach(s -> {
            s.getExclusivelyCoveredPoints().clear();
            s.getExclusivelyCoveredPoints().addAll(s.getCoveredPoints());
        });
        availableSensors.forEach(s1 -> availableSensors.forEach(s2 -> this.filterOutCoveredPoints(s1, s2)));
    }

    private void filterOutCoveredPoints(Sensor s1, Sensor s2) {
        if (!Objects.equals(s1, s2)) {
            s1.getExclusivelyCoveredPoints().removeAll(s2.getCoveredPoints());
        }
    }

}
