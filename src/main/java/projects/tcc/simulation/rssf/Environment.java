package projects.tcc.simulation.rssf;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
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

    @Setter(AccessLevel.NONE)
    private double currentCoverage;

    public Environment(long height, long width, double coverageFactor) {
        this.height = height;
        this.width = width;
        this.coverageFactor = coverageFactor;
        this.area = height * width;
        this.points = new ArrayList<>();
        this.coveredPoints = new HashSet<>();
        this.currentCoverage = 0;
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

    public void updateCoverage() {
        this.getCoveredPoints().clear();
        SensorHolder.getActiveSensors().values().forEach(s -> this.getCoveredPoints().addAll(s.getCoveredPoints()));
        this.currentCoverage = ((double) this.getCoveredPoints().size()) / ((double) this.getPoints().size());
    }

    public void updateExclusivelyCoveredPoints() {
        Collection<Sensor> availableSensors = SensorHolder.getAvailableSensors().values();
        availableSensors.forEach(s -> {
            s.getExclusivelyCoveredPoints().clear();
            s.getExclusivelyCoveredPoints().addAll(s.getCoveredPoints());
        });
        availableSensors.forEach(s1 -> availableSensors.forEach(s2 -> this.filterOutCoveredPoints(s1, s2)));
    }

    public void filterOutCoveredPoints(Sensor s1, Sensor s2) {
        if (!Objects.equals(s1, s2)) {
            s1.getExclusivelyCoveredPoints().removeAll(s2.getCoveredPoints());
        }
    }

}
