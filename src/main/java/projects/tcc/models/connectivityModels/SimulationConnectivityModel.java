package projects.tcc.models.connectivityModels;

import projects.tcc.nodes.nodeImplementations.SensorNode;
import projects.tcc.simulation.wsn.data.Sensor;
import projects.tcc.simulation.wsn.data.Sink;
import sinalgo.models.ConnectivityModelHelper;
import sinalgo.nodes.Node;

public class SimulationConnectivityModel extends ConnectivityModelHelper {

    @Override
    protected boolean isConnected(Node from, Node to) {
        if (!(from instanceof SensorNode && to instanceof SensorNode)) {
            return false;
        }
        Sensor s1 = ((SensorNode) from).getSensor();
        Sensor s2 = ((SensorNode) to).getSensor();
        return s2.equals(s1.getParent()) &&
                ((s1.isConnected() && s2.isConnected())
                        || (s1.isConnected() && s2 instanceof Sink));
    }
}
