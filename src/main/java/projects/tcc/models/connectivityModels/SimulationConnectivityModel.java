package projects.tcc.models.connectivityModels;

import sinalgo.exception.WrongConfigurationException;
import sinalgo.models.ConnectivityModel;
import sinalgo.nodes.Node;

public class SimulationConnectivityModel extends ConnectivityModel {

    @Override
    public boolean updateConnections(Node n) throws WrongConfigurationException {
        return false;
    }

}
