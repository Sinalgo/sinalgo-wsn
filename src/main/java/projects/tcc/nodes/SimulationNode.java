package projects.tcc.nodes;

import projects.tcc.CustomGlobal;
import projects.tcc.nodes.nodeImplementations.SinkNode;
import projects.tcc.simulation.wsn.data.Sensor;
import sinalgo.exception.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.runtime.Global;

import java.awt.*;

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

    @Override
    public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
        super.drawAsDisk(g, pt, highlight, this.getDefaultDrawingSizeInPixels());
        this.drawCommSensRadius(g, pt);
    }

    private void drawCommSensRadius(Graphics g, PositionTransformation pt) {
        CustomGlobal customGlobal = ((CustomGlobal) Global.getCustomGlobal());
        if (customGlobal.isDrawCommRadius() && (this.getSensor().isConnected() || this instanceof SinkNode)) {
            drawRadius(g, pt, this, Color.ORANGE, this.getSensor().getCommRadius());
        }
        if (customGlobal.isDrawSensorRadius() && (this.getSensor().isConnected() || this instanceof SinkNode)) {
            drawRadius(g, pt, this, Color.MAGENTA, this.getSensor().getSensRadius());
        }
    }

    private static void drawRadius(Graphics g, PositionTransformation pt, Node s, Color orange, double commRadius) {
        Color backupColor = g.getColor();
        g.setColor(orange);
        pt.drawCircle(g, s.getPosition(), commRadius);
        g.setColor(backupColor);
    }

}
