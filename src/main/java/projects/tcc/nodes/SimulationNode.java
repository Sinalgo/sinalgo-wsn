package projects.tcc.nodes;

import sinalgo.nodes.Node;

public abstract class SimulationNode extends Node {

    @Override
    protected String nodeTypeName() {
        return this.getClass().getSimpleName();
    }

}
