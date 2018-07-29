package projects.tcc.simulation.io;

import lombok.Builder;
import lombok.Data;
import sinalgo.nodes.Position;

import java.util.List;

@Data
@Builder
public class SimulationConfiguration {

    @Data
    @Builder
    public static class SensorConfiguration {
        private final double x;
        private final double y;

        public Position toPosition() {
            return new Position(this.getX(), this.getY(), 0);
        }
    }

    private final double sensorRadius;
    private final double commRadius;
    private final double batteryEnergy;
    private final double activationPower;
    private final double receivePower;
    private final double maintenancePower;
    private final double commRatio;
    private transient double coverageFactor;
    private transient int dimX;
    private transient int dimY;

    private final List<SensorConfiguration> sinkConfigurations;
    private final List<SensorConfiguration> sensorConfigurations;

}
