package projects.tcc.nodes.nodeImplementations;

import lombok.AccessLevel;
import lombok.Getter;
import projects.tcc.nodes.messages.SimulationMessage;
import projects.tcc.simulation.algorithms.online.SolucaoViaAGMOSinalgo;
import projects.tcc.simulation.io.SimulationConfiguration;
import projects.tcc.simulation.io.SimulationConfigurationLoader;
import projects.tcc.simulation.wsn.SensorNetwork;
import projects.tcc.simulation.wsn.data.Sink;
import projects.tcc.simulation.wsn.data.impl.WSNSink;
import sinalgo.exception.SinalgoWrappedException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;

import java.awt.*;

public class SinkNode extends SensorNode {

    @Getter
    private Sink sensor;

    private int stage = 0;

    @Getter(AccessLevel.PROTECTED)
    private long totalReceivedMessages;

    @Override
    public void init() {
        SimulationConfiguration config = SimulationConfigurationLoader.getConfiguration();
        this.sensor = new WSNSink((int) this.getID() - 1, this.getPosition().getXCoord(),
                this.getPosition().getYCoord(), config.getCommRadius());
        SensorNetwork.currentInstance().addSinks(this.getSensor());
        this.runSimulation();
    }

    @Override
    public void handleMessages(Inbox inbox) {
        int size = inbox.size();
        if (size > 0) {
            System.out.println("\nSTART logging received messages for round");
        }
        while (inbox.hasNext()) {
            Message m = inbox.next();
            if (m instanceof SimulationMessage) {
                this.totalReceivedMessages++;
                System.out.println(String.join(", ", ((SimulationMessage) m).getNodes()));
            }
        }
        if (size > 0) {
            System.out.println("END logging received messages for round\n");
            this.runSimulation();
        }
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
