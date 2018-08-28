package projects.tcc.simulation.wsn.data;

import lombok.Getter;

public class DemandPoints {

    private final IndexedPosition[] demandPoints;
    private final int[] coverageMatrix;

    @Getter
    private int numCoveredPoints;

    public DemandPoints(int size) {
        this.demandPoints = new IndexedPosition[size];
        this.coverageMatrix = new int[size];
    }

    public int getSize() {
        return this.demandPoints.length;
    }

}
