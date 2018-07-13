package projects.tcc.models.distributionModels;

import lombok.AccessLevel;
import lombok.Getter;
import projects.tcc.simulation.io.ConfigurationLoader;
import projects.tcc.simulation.io.SimulationConfiguration;
import sinalgo.models.DistributionModel;
import sinalgo.nodes.Position;

import java.util.Iterator;

public class SimulationFilesPositionModel extends DistributionModel {

    @Getter(AccessLevel.PRIVATE)
    private final Iterator<SimulationConfiguration.SensorConfiguration> configurationIterator;

    public SimulationFilesPositionModel() {
        this.configurationIterator = ConfigurationLoader.getConfiguration().getSensorConfigurations().iterator();
        this.setNumberOfNodes(ConfigurationLoader.getConfiguration().getSensorConfigurations().size());
    }

    @Override
    public Position getNextPosition() {
        if (this.configurationIterator.hasNext()) {
            return this.configurationIterator.next().toPosition();
        }
        return new Position(ConfigurationLoader.getConfiguration().getSinkPosX(),
                ConfigurationLoader.getConfiguration().getSinkPosY(), 0);
    }
}
