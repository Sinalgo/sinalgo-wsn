package projects.wsn.models.connectivityModels;

import projects.wsn.nodes.SimulationNode;
import sinalgo.models.ConnectivityModelHelper;
import sinalgo.nodes.Node;

import java.util.Objects;

public class WSNConnectivityModel extends ConnectivityModelHelper {

    @Override
    protected boolean isConnected(Node from, Node to) {
        if (!(from instanceof SimulationNode && to instanceof SimulationNode)) {
            return false;
        }
        SimulationNode s1 = (SimulationNode) from;
        SimulationNode s2 = (SimulationNode) to;
        return s1.isActive() && s2.isActive()
                && !s1.isSleep() && !s2.isSleep()
                && Objects.equals(s1.getParent(), s2);
    }

}
