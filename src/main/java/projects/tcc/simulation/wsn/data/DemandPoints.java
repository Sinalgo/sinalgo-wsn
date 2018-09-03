package projects.tcc.simulation.wsn.data;

import lombok.Getter;
import sinalgo.configuration.Configuration;

import java.util.ArrayList;
import java.util.List;

public final class DemandPoints {

    private int nextIndex = 0;

    @Getter
    private final List<DemandPoint> points;

    @Getter
    private int coveredNumPoints;

    @Getter
    private double coveragePercent;

    private static DemandPoints currentInstance;

    public static DemandPoints currentInstance() {
        if (currentInstance == null) {
            return newInstance();
        }
        return currentInstance;
    }

    public static DemandPoints newInstance() {
        currentInstance = new DemandPoints(Configuration.getDimX(), Configuration.getDimY());
        return currentInstance;
    }

    private DemandPoints(int dimX, int dimY) {
        int area = dimX * dimY;
        this.points = new ArrayList<>(area);
        this.computeDemandPoints(dimX, dimY);
    }

    public int getTotalNumPoints() {
        return this.points.size();
    }

    private void computeDemandPoints(int dimX, int dimY) {
        for (int i = 0; i < dimX; i++) {
            for (int j = 0; j < dimY; j++) {
                this.getPoints().add(new DemandPoint(nextIndex++, i + 0.5, j + 0.5, 0));
            }
        }
    }

    public void computeSensorsCoveredPoints(List<Sensor> sensors) {
        for (Sensor sens : sensors) {
            for (DemandPoint p : this.getPoints()) {
                double distance = sens.getPosition().distanceTo(p);
                if (Double.compare(distance, sens.getSensRadius()) <= 0) {
                    sens.getCoveredPoints().add(p);
                }
            }
        }
    }

    void removeCoverage(Sensor s) {
        for (DemandPoint p : s.getCoveredPoints()) {
            if (p.getCoverage() == 1) {
                this.coveredNumPoints--;
            }
            p.removeCoverage();
        }
        computeCoveragePercent();
    }

    void addCoverage(Sensor s) {
        for (DemandPoint p : s.getCoveredPoints()) {
            if (p.getCoverage() == 0) {
                this.coveredNumPoints++;
            }
            p.addCoverage();
        }
        this.computeCoveragePercent();
    }

    private void computeCoveragePercent() {
        this.coveragePercent = (double) this.getCoveredNumPoints() / (double) this.getTotalNumPoints();
    }

}
