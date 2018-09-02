package projects.tcc.simulation.wsn.data;

import lombok.Getter;

import java.util.List;

public final class DemandPoints {

    @Getter
    private final IndexedPosition[] points;
    private final int[] coverageMatrix;
    private int nextID = 0;

    @Getter
    private int numCoveredPoints;

    public DemandPoints(int dimX, int dimY) {
        int area = dimX * dimY;
        this.points = new IndexedPosition[area];
        this.coverageMatrix = new int[area];
        this.computeDemandPoints(dimX, dimY);
    }

    public int getNumPoints() {
        return this.points.length;
    }

    private void computeDemandPoints(int dimX, int dimY) {
        for (int i = 0; i < dimX; i++) {
            for (int j = 0; j < dimY; j++) {
                int ID = this.nextID++;
                this.points[ID] = new IndexedPosition(ID, i + 0.5, j + 0.5, 0);
            }
        }
    }

    public void computeSensorsCoveredPoints(List<Sensor> sensors) {
        for (Sensor sens : sensors) {
            for (IndexedPosition pontoDemanda : this.points) {
                double distance = sens.getPosition().distanceTo(pontoDemanda);
                if (Double.compare(distance, sens.getSensRadius()) <= 0) {
                    sens.getCoveredPoints().add(pontoDemanda);
                }
            }
        }
    }

    public void resetCoverage() {
        for (int i = 0; i < this.coverageMatrix.length; i++) {
            this.coverageMatrix[i] = 0;
        }
        this.numCoveredPoints = 0;
    }

    public void removeCoverage(Sensor s) {
        for (IndexedPosition pontoCoberto : s.getCoveredPoints()) {
            if (--this.coverageMatrix[pontoCoberto.getIndex()] == 0) {
                this.numCoveredPoints--;
            }
        }
    }

    public void addCoverage(Sensor s) {
        for (IndexedPosition point : s.getCoveredPoints()) {
            if (this.coverageMatrix[point.getIndex()]++ == 0) {
                this.numCoveredPoints++;
            }
        }
    }

    public int getCoverage(IndexedPosition position) {
        return this.coverageMatrix[position.getIndex()];
    }

}
