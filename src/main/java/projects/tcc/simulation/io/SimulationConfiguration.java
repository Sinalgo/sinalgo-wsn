package projects.tcc.simulation.io;

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
