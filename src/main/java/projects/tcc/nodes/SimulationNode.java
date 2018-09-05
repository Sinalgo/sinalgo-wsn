package projects.tcc.nodes;

import projects.tcc.CustomGlobal;
import projects.tcc.nodes.nodeImplementations.SinkNode;
import projects.tcc.simulation.wsn.data.Sensor;
import sinalgo.exception.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.runtime.Global;

import java.awt.*;
import java.util.List;

public abstract class SimulationNode extends Node {

    public abstract Sensor getSensor();

    @Override
    protected String nodeTypeName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void neighborhoodChange() {
        // Do not use, we don't have a mobility model.
    }

    @Override
    public void preStep() {
        if (this.isActive() && !this.isSleep()) {
            this.getSensor().drawMaintenanceEnergy();
        }
    }

    @Override
    public void postStep() {
        this.getSensor().updateState();
        this.setWaitTime(Math.max(0, this.getWaitTime() - 1));
    }

    @Override
    public void checkRequirements() throws WrongConfigurationException {

    }

    protected abstract long getTotalReceivedMessages();

    protected abstract long getTotalSentMessages();

    public abstract SimulationNode getParent();

    public abstract List<SimulationNode> getChildren();

    public abstract boolean isActive();

    public abstract boolean isFailed();

    protected abstract void setWaitTime(int waitTime);

    protected abstract int getWaitTime();

    protected boolean isSleep() {
        return this.getWaitTime() > 0;
    }

    @Override
    public String toString() {
        return "[" + super.toString() + ": " + String.format(
                "Tx=%d, Rx=%d, Pwr=%.2f, State=%s",
                this.getTotalSentMessages(),
                this.getTotalReceivedMessages(),
                this.getSensor().getBatteryEnergy(),
                this.getSensor().isAvailable() ? "OK" : "FAILED") + "]";
    }

    @Override
    public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
        super.drawAsDisk(g, pt, highlight, this.getDefaultDrawingSizeInPixels());
        this.drawCommSensRadius(g, pt);
    }

    private void drawCommSensRadius(Graphics g, PositionTransformation pt) {
        CustomGlobal customGlobal = ((CustomGlobal) Global.getCustomGlobal());
        if (customGlobal.isDrawCommRadius() && this.isActive()) {
            drawRadius(g, pt, this, Color.ORANGE, this.getSensor().getCommRadius());
        }
        if (customGlobal.isDrawSensorRadius() && this.isActive() && !(this instanceof SinkNode)) {
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
