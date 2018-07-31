package projects.tcc.simulation.io;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import sinalgo.exception.SinalgoFatalException;
import sinalgo.nodes.Position;

import java.util.Iterator;
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
    private transient double crossoverRate;
    private transient int numberOfGenerations;
    private transient int populationSize;

    private final List<SensorConfiguration> sinkConfigurations;
    private final List<SensorConfiguration> sensorConfigurations;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private transient Iterator<SensorConfiguration> sensorConfigurationIterator;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private transient Iterator<SensorConfiguration> sinkConfigurationIterator;

    public Position getNextPosition() {
        if (this.sensorConfigurationIterator == null) {
            this.sensorConfigurationIterator = this.getSensorConfigurations().iterator();
        }
        if (this.sensorConfigurationIterator.hasNext()) {
            return this.sensorConfigurationIterator.next().toPosition();
        }
        if (this.sinkConfigurationIterator == null) {
            this.sinkConfigurationIterator = this.getSinkConfigurations().iterator();
        }
        if (this.sinkConfigurationIterator.hasNext()) {
            return this.sinkConfigurationIterator.next().toPosition();
        }
        throw new SinalgoFatalException("No more positions available!");
    }

}
