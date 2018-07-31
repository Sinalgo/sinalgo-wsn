package projects.tcc.nodes.nodeImplementations;

import lombok.Getter;
import lombok.experimental.Delegate;
import projects.tcc.simulation.algorithms.online.SolucaoViaAGMOSinalgo;
import projects.tcc.simulation.io.SimulationConfiguration;
import projects.tcc.simulation.io.SimulationConfigurationLoader;
import projects.tcc.simulation.wsn.SensorNetwork;
import projects.tcc.simulation.wsn.data.Sink;
import projects.tcc.simulation.wsn.data.impl.WSNSink;
import sinalgo.exception.SinalgoWrappedException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.messages.Inbox;

import java.awt.*;

public class SinkNode extends SensorNode implements Sink {

    @Getter
    @Delegate(types = Sink.class)
    private Sink sensor;

    private int stage = 0;

    @Override
    public void init() {
        SimulationConfiguration config = SimulationConfigurationLoader.getConfiguration();
        this.sensor = new WSNSink((int) this.getID() - 1, this.getPosition().getXCoord(),
                this.getPosition().getYCoord(), config.getCommRadius());
        SensorNetwork.currentInstance().addSinks(this);
        this.runSimulation();
    }

    @Override
    public void handleMessages(Inbox inbox) {
        this.runSimulation();
    }

    private void runSimulation() {
        SolucaoViaAGMOSinalgo solucao = SolucaoViaAGMOSinalgo.currentInstance();
        try {
            solucao.simularRede(stage++);
        } catch (Exception e) {
            throw new SinalgoWrappedException(e);
        }
    }

    @Override
    public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
        this.setColor(Color.BLUE);
        super.drawAsDisk(g, pt, highlight, 30);
    }
}
