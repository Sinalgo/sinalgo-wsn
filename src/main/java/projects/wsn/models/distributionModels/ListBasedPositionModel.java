package projects.wsn.models.distributionModels;

import projects.defaultProject.models.distributionModels.Random;
import projects.wsn.nodes.nodeImplementations.SensorNode;
import projects.wsn.nodes.nodeImplementations.SinkNode;
import projects.wsn.simulation.io.SimulationConfiguration;
import projects.wsn.simulation.io.SimulationConfigurationLoader;
import projects.wsn.simulation.network.data.Sensor;
import projects.wsn.simulation.network.data.SensorIndex;
import projects.wsn.simulation.network.data.Sink;
import sinalgo.nodes.Node;
import sinalgo.nodes.Position;
import sinalgo.tools.Tools;

import java.util.Collections;
import java.util.List;

public class ListBasedPositionModel extends Random {

    public ListBasedPositionModel() {
    }

    private static List<SimulationConfiguration.SensorPosition> getListFor(Class<? extends Node> nodeClass) {
        if (nodeClass.equals(SensorNode.class)) {
            return SimulationConfigurationLoader.getConfiguration().getSensorPositions();
        } else if (nodeClass.equals(SinkNode.class)) {
            return SimulationConfigurationLoader.getConfiguration().getSinkPositions();
        } else {
            return Collections.emptyList();
        }
    }

    private static int getIndexFor(Class<? extends Node> nodeClass) {
        if (nodeClass.equals(SensorNode.class)) {
            return SensorIndex.currentInstance().getIndexFor(Sensor.class);
        } else if (nodeClass.equals(SinkNode.class)) {
            return SensorIndex.currentInstance().getIndexFor(Sink.class);
        } else {
            return 0;
        }
    }

    public Position getNextPosition(Class<? extends Node> nodeClass) {
        List<SimulationConfiguration.SensorPosition> positions = getListFor(nodeClass);
        int index = getIndexFor(nodeClass);
        if (index >= positions.size()) {
            Tools.warning("No list positions available for " + nodeClass.getSimpleName() + " node type. Using randomly generated ones.");
            return this.getNextPosition();
        }
        return positions.get(index).toPosition();
    }
}
