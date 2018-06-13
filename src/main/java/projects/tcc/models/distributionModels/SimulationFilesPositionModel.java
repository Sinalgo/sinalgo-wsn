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
        this.configurationIterator = ConfigurationLoader.getConfiguration().getSensors().iterator();
        this.setNumberOfNodes(ConfigurationLoader.getConfiguration().getSensors().size());
    }

    @Override
    public Position getNextPosition() {
        return this.configurationIterator.hasNext() ? this.configurationIterator.next().toPosition() : null;
    }
}
