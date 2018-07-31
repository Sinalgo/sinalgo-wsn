package projects.tcc.models.connectivityModels;

import projects.tcc.nodes.nodeImplementations.SensorNode;
import projects.tcc.simulation.wsn.data.Sink;
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
        return (s1.equals(s2.getParent()) || s2.equals(s1.getParent())) &&
                ((s1.isConnected() && s2.isConnected())
                        || (s1.isConnected() && s2 instanceof Sink)
                        || (s2.isConnected() && s1 instanceof Sink));
    }
}
