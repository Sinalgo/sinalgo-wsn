package projects.tcc.models.connectivityModels;

import projects.defaultProject.models.connectivityModels.UDG;
import sinalgo.exception.CorruptConfigurationEntryException;

public class SimulationConnectivityModel extends UDG {

    public SimulationConnectivityModel(double rMax) {
        super(rMax);
    }

    public SimulationConnectivityModel() throws CorruptConfigurationEntryException {
    }
}
