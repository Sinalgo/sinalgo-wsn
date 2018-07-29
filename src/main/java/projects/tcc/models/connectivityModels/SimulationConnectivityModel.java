package projects.tcc.models.connectivityModels;

import projects.tcc.nodes.nodeImplementations.SensorNode;
import sinalgo.models.ConnectivityModelHelper;
import sinalgo.nodes.Node;

public class SimulationConnectivityModel extends ConnectivityModelHelper {

    @Override
    protected boolean isConnected(Node from, Node to) {
        if (!(from instanceof SensorNode) || (!(to instanceof SensorNode))) {
            return false;
        }
        SensorNode s1 = (SensorNode) from;
        SensorNode s2 = (SensorNode) to;
        return s1.getChildren().contains(s2) || s2.getChildren().contains(s1);
    }
}
