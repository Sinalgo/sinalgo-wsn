package projects.tcc.nodes;

import projects.tcc.simulation.wsn.data.Sensor;
import sinalgo.exception.WrongConfigurationException;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;

public abstract class SimulationNode extends Node {

    protected abstract Sensor getSensor();

    @Override
    protected String nodeTypeName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void preStep() {

    }

    @Override
    public void handleMessages(Inbox inbox) {

    }

    @Override
    public void neighborhoodChange() {
        // Do not use, we don't have a mobility model.
    }

    @Override
    public void postStep() {

    }

    @Override
    public void checkRequirements() throws WrongConfigurationException {

    }

    protected abstract long getTotalReceivedMessages();

    protected abstract long getTotalSentMessages();

    @Override
    public String toString() {
        return String.format("[%s: Tx=%d, Rx=%d]", super.toString(),
                this.getTotalSentMessages(), this.getTotalReceivedMessages());
    }

}
