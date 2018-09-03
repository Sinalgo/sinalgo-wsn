package projects.tcc.simulation.wsn.data;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public final class DemandPoints {

    @Getter
    private final List<DemandPoint> points;

    @Getter
    private int numCoveredPoints;
    private int nextIndex = 0;

    public DemandPoints(int dimX, int dimY) {
        int area = dimX * dimY;
        this.points = new ArrayList<>(area);
        this.computeDemandPoints(dimX, dimY);
    }

    public int getNumPoints() {
        return this.points.size();
    }

    private void computeDemandPoints(int dimX, int dimY) {
        for (int i = 0; i < dimX; i++) {
            for (int j = 0; j < dimY; j++) {
                this.points.add(new DemandPoint(nextIndex++, i + 0.5, j + 0.5, 0));
            }
        }
    }

    public void computeSensorsCoveredPoints(List<Sensor> sensors) {
        for (Sensor sens : sensors) {
            for (DemandPoint p : this.points) {
                double distance = sens.getPosition().distanceTo(p);
                if (Double.compare(distance, sens.getSensRadius()) <= 0) {
                    sens.getCoveredPoints().add(p);
                }
            }
        }
    }

    public void resetCoverage() {
        for (DemandPoint p : this.points) {
            p.setCoverage(0);
        }
        this.numCoveredPoints = 0;
    }

    public void removeCoverage(Sensor s) {
        for (DemandPoint p : s.getCoveredPoints()) {
            if (p.getCoverage() == 1) {
                this.numCoveredPoints--;
            }
            p.setCoverage(p.getCoverage() - 1);
        }
    }

    public void addCoverage(Sensor s) {
        for (DemandPoint p : s.getCoveredPoints()) {
            if (p.getCoverage() == 0) {
                this.numCoveredPoints++;
            }
            p.setCoverage(p.getCoverage() + 1);
        }
    }

}
