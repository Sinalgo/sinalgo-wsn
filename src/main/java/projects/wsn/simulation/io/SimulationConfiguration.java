package projects.wsn.simulation.io;

import lombok.Data;
import sinalgo.nodes.Position;

import java.util.List;

@Data
public class SimulationConfiguration {

    private transient double sensorRadius;
    private transient double commRadius;
    private transient double sinkCommRadius;
    private transient double batteryEnergy;
    private transient double activationPower;
    private transient double receivePower;
    private transient double maintenancePower;
    private transient double commRatio;
    private transient double coverageFactor;
    private transient double crossoverRate;
    private transient int numberOfGenerations;
    private transient int populationSize;
    private transient double minBatteryThreshold;
    private transient int transmitSpeedBps;
    private transient boolean minimizeActivationTree;
    private transient boolean useMessageSizeCalculation;
    private transient double failureDetectionModelSuccessRate;

    public boolean isUseFailureDetectionModel() {
        return this.failureDetectionModelSuccessRate > 0
                && this.failureDetectionModelSuccessRate <= 1;
    }

    public boolean isPerfectDetectionModel() {
        return this.failureDetectionModelSuccessRate == 1;
    }

    @Data
    public static class SensorPosition {
        private final double x;
        private final double y;

        public Position toPosition() {
            return new Position(this.getX(), this.getY(), 0);
        }
    }

    private final List<SensorPosition> sinkPositions;
    private final List<SensorPosition> sensorPositions;

}
