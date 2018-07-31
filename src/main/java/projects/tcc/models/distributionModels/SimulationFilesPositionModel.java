package projects.tcc.models.distributionModels;

import projects.tcc.simulation.io.SimulationConfigurationLoader;
import sinalgo.models.DistributionModel;
import sinalgo.nodes.Position;

public class SimulationFilesPositionModel extends DistributionModel {

    public SimulationFilesPositionModel() {
        this.setNumberOfNodes(SimulationConfigurationLoader.getConfiguration().getSensorConfigurations().size() +
                SimulationConfigurationLoader.getConfiguration().getSinkConfigurations().size());
    }

    @Override
    public Position getNextPosition() {
        return SimulationConfigurationLoader.getConfiguration().getNextPosition();
    }
}
