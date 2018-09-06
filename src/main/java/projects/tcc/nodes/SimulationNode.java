package projects.tcc.nodes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import projects.tcc.CustomGlobal;
import projects.tcc.nodes.nodeImplementations.SinkNode;
import projects.tcc.simulation.wsn.data.Sensor;
import sinalgo.exception.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.runtime.Global;

import java.awt.*;
import java.util.List;

@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PROTECTED)
public abstract class SimulationNode extends Node {

    public abstract Sensor getSensor();

    @Getter
    private boolean failed;

    @Getter
    private SimulationNode parent;

    @Getter
    private List<SimulationNode> children;

    @Getter
    private double batteryEnergy;

    @Getter
    private double batteryCapacity;

    private int waitTime;
    private boolean active;
    private long totalReceivedMessages;
    private long totalSentMessages;
    private double activationPower;
    private double receivePower;
    private double maintenancePower;
    private double commRatio; //Taxa de comunicação durante a transmissão em uma u.t.
    private double minBatteryThreshold;

    public boolean isActive() {
        return this.active && this.isAvailable();
    }

    public boolean isAvailable() {
        return !this.isFailed();
    }

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
            this.drawMaintenanceEnergy();
        }
    }

    @Override
    public void postStep() {
        this.updateState();
        this.setWaitTime(Math.max(0, this.getWaitTime() - 1));
    }

    @Override
    public void checkRequirements() throws WrongConfigurationException {

    }

    protected boolean isSleep() {
        return this.getWaitTime() > 0;
    }

    @Override
    public String toString() {
        return "[" + this.superToString() + ": " + String.format(
                "Tx=%d, Rx=%d, Pwr=%.2f, State=%s",
                this.getTotalSentMessages(),
                this.getTotalReceivedMessages(),
                this.getBatteryEnergy(),
                this.isAvailable() ? "OK" : "FAILED") + "]";
    }

    protected String superToString() {
        return super.toString();
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

    private void drawEnergySpent(double energySpent) {
        this.setBatteryEnergy(Math.max(this.getBatteryEnergy() - energySpent, 0));
    }

    private double getTransmitPower(SimulationNode neighbor) {
        double current = this.getSensor().getNeighborhood()
                .get(neighbor.getSensor()).getCurrent();
        return this.getCommRatio() * current;
    }

    protected void drawActivationEnergy() {
        this.drawEnergySpent(this.getActivationPower());
    }

    private void drawMaintenanceEnergy() {
        this.drawEnergySpent(this.getMaintenancePower());
    }

    protected void drawReceiveEnergy() {
        this.drawEnergySpent(this.getReceivePower());
    }

    protected void drawTransmitEnergy(SimulationNode neighbor) {
        this.drawEnergySpent(this.getTransmitPower(neighbor));
    }

    private void updateState() {
        if (this.isAvailable() && this.isActive() &&
                Double.compare(this.getBatteryEnergy(), this.getMinBatteryThreshold() * this.getBatteryCapacity()) <= 0) {
            this.setFailed(true);
        }
    }

    @NodePopupMethod(menuText = "Deactivate")
    public void deactivate() {
        this.setActive(false);
    }

    @NodePopupMethod(menuText = "Force failure")
    public void fail() {
        this.setFailed(true);
    }

}
