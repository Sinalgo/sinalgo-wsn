package projects.tcc.models.distributionModels;

import lombok.AccessLevel;
import lombok.Getter;
import projects.tcc.simulation.io.ConfigurationLoader;
import projects.tcc.simulation.io.SimulationConfiguration;
import sinalgo.exception.SinalgoFatalException;
import sinalgo.models.DistributionModel;
import sinalgo.nodes.Position;

import java.util.Iterator;

public class SimulationFilesPositionModel extends DistributionModel {

    @Getter(AccessLevel.PRIVATE)
    private final Iterator<SimulationConfiguration.SensorConfiguration> configurationIterator;

    @Getter(AccessLevel.PRIVATE)
    private final Iterator<SimulationConfiguration.SensorConfiguration> sinkConfigurationIterator;

    public SimulationFilesPositionModel() {
        this.configurationIterator = ConfigurationLoader.getConfiguration().getSensorConfigurations().iterator();
        this.sinkConfigurationIterator = ConfigurationLoader.getConfiguration().getSinkConfigurations().iterator();
        this.setNumberOfNodes(ConfigurationLoader.getConfiguration().getSensorConfigurations().size());
    }

    @Override
    public Position getNextPosition() {
        if (this.getConfigurationIterator().hasNext()) {
            return this.getConfigurationIterator().next().toPosition();
        }
        if (this.getSinkConfigurationIterator().hasNext()) {
            return this.getSinkConfigurationIterator().next().toPosition();
        }
        throw new SinalgoFatalException("No more node positions to load in this configuration!");
    }
}
