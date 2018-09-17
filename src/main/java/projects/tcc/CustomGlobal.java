/*
BSD 3-Clause License

Copyright (c) 2007-2013, Distributed Computing Group (DCG)
                         ETH Zurich
                         Switzerland
                         dcg.ethz.ch
              2017-2018, André Brait

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

* Neither the name of the copyright holder nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package projects.tcc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import projects.tcc.nodes.NodeStatus;
import projects.tcc.simulation.io.SimulationOutput;
import projects.tcc.simulation.wsn.SensorNetwork;
import projects.tcc.simulation.wsn.data.DemandPoints;
import sinalgo.configuration.Configuration;
import sinalgo.exception.SinalgoFatalException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Position;
import sinalgo.runtime.AbstractCustomGlobal;
import sinalgo.runtime.Global;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.Logging;

import java.awt.*;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Arrays;

@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
public class CustomGlobal extends AbstractCustomGlobal {

    private Logging log = Logging.getLogger("tcc_log.txt");

    private boolean drawPoints;

    @Getter
    private boolean drawCommRadius;

    @Getter
    private boolean drawSensorRadius;

    @Getter
    private boolean drawAll;

    @Getter
    private boolean stopSimulationOnFailure;

    @Getter
    private boolean drawActivationTree;

    @Getter
    private boolean drawLegend = true;

    @GlobalMethod(menuText = "Toggle Draw Activation Tree", subMenu = "View")
    public void toggleDrawActivationTree() {
        this.drawActivationTree = !this.drawActivationTree;
        this.drawAll &= this.drawActivationTree;
    }

    @GlobalMethod(menuText = "Toggle Draw Demand Points", subMenu = "View")
    public void toggleDrawDemandPoints() {
        this.drawPoints = !this.drawPoints;
        this.drawAll &= this.drawPoints;
    }

    @GlobalMethod(menuText = "Toggle Draw Comm Radius", subMenu = "View", order = 1)
    public void toggleDrawCommRadius() {
        this.drawCommRadius = !this.drawCommRadius;
        this.drawAll &= this.drawCommRadius;
    }

    @GlobalMethod(menuText = "Toggle Draw Sensor Radius", subMenu = "View", order = 2)
    public void toggleDrawSensorRadius() {
        this.drawSensorRadius = !this.drawSensorRadius;
        this.drawAll &= this.drawSensorRadius;
    }

    @GlobalMethod(menuText = "Toggle Draw Sensor Radius", subMenu = "View", order = 2)
    public void toggleDrawLegend() {
        this.drawLegend = !this.drawLegend;
    }

    @GlobalMethod(menuText = "Toggle All", subMenu = "View", order = 3)
    public void toggleAll() {
        if (this.drawAll) {
            this.drawSensorRadius = false;
            this.drawCommRadius = false;
            this.drawPoints = false;
            this.drawActivationTree = false;
        } else {
            this.drawSensorRadius = true;
            this.drawCommRadius = true;
            this.drawPoints = true;
            this.drawActivationTree = true;
        }
        this.drawAll = !this.drawAll;
    }

    @GlobalMethod(menuText = "Toggle stop simulation on Sensor failure", subMenu = "Simulation")
    public void togglePauseOnSensorFailure() {
        this.stopSimulationOnFailure = !this.stopSimulationOnFailure;
    }

    @Override
    public void customPaint(Graphics g, PositionTransformation pt) {
        Color backupColor = g.getColor();
        if (this.drawPoints) {
            g.setColor(Color.DARK_GRAY);
            for (Position p : DemandPoints.currentInstance().getPoints()) {
                pt.drawLine(g, p, p);
            }
        }
        if (this.drawLegend) {
            Font font = new Font(null, Font.PLAIN, (int) (1.2 * pt.getZoomFactor()));
            g.setFont(font);
            FontMetrics fm = g.getFontMetrics(font);
            int maxWidth = Arrays.stream(NodeStatus.values())
                    .map(NodeStatus::getDescription)
                    .mapToInt(d -> (int) Math.ceil(fm.stringWidth(d)))
                    .max()
                    .orElse(0);
            double offset = 2.2;
            double x = Configuration.getDimX() * 1.05;
            double y = Configuration.getDimY() / 2.0;
            int height = (int) (offset * 0.95 * (NodeStatus.values().length + 1) * pt.getZoomFactor());
            pt.translateToGUIPosition(x, y, 0);
            g.setColor(Color.WHITE);
            g.fillRect(pt.getGuiX(), pt.getGuiY(), (int) (maxWidth + (0.5 + 1.8 + 1.2) * pt.getZoomFactor()), height);
            g.setColor(Color.BLACK);
            g.drawRect(pt.getGuiX(), pt.getGuiY(), (int) (maxWidth + (0.5 + 1.8 + 1.2) * pt.getZoomFactor()), height);
            double legendX = x + 0.5;
            double legendY = y;
            boolean firstLine = true;
            for (NodeStatus s : NodeStatus.values()) {
                legendY += offset;
                if (firstLine) {
                    firstLine = false;
                    legendY -= offset / 2;
                }
                pt.translateToGUIPosition(legendX, legendY, 0);
                g.setColor(s.getColor());
                g.fillOval(pt.getGuiX(), pt.getGuiY(), (int) (1.2 * pt.getZoomFactor()), (int) (1.2 * pt.getZoomFactor()));
                pt.translateToGUIPosition(legendX + 1.8, legendY + 1, 0);
                g.setColor(Color.BLACK);
                g.drawChars(s.getDescription().toCharArray(), 0, s.getDescription().length(), pt.getGuiX(), pt.getGuiY());
            }
        }
        g.setColor(backupColor);
    }

    @Override
    public boolean hasTerminated() {
        if (Double.compare(DemandPoints.currentInstance().getCoveragePercent(), SensorNetwork.currentInstance().getCoverageFactor()) < 0) {
            Tools.minorError("The coverage could not be kept above the desired factor anymore. Stopping simulation.");
            SimulationOutput.currentInstance().generateFinalOutput();
            return true;
        }
        return false;
    }

    @Override
    public void preRun() {
        PrintStream ps = Tools.getTextOutputPrintStream();
        SimulationOutput.setPrintFunction(ps::print);
        SimulationOutput.setPrintlnFunction(ps::println);
    }

    @Override
    public void preRound() {
    }

    @Override
    public void postRound() {
    }

    @Override
    public String includeGlobalMethodInMenu(Method m, String defaultText) {
        switch (m.getName()) {
            case "toggleDrawDemandPoints":
                return getEnableDisableString(this.drawPoints) + "Demand Points";
            case "toggleDrawCommRadius":
                return getEnableDisableString(this.drawCommRadius) + "Comm. Radius";
            case "toggleDrawSensorRadius":
                return getEnableDisableString(this.drawSensorRadius) + "Sensorinig Radius";
            case "toggleAll":
                return getEnableDisableString(this.drawAll) + "All";
            case "togglePauseOnSensorFailure":
                return (this.stopSimulationOnFailure
                        ? "Disable" : "Enable") + " stop simulation on Sensor failure";
            case "toggleDrawActivationTree":
                return getEnableDisableString(this.drawActivationTree) + "Activation Tree";
            case "toggleDrawLegend":
                return getEnableDisableString(this.drawLegend) + "Legend";
        }
        return defaultText;
    }

    private String getEnableDisableString(boolean toggleStatus) {
        return toggleStatus ? "Disable drawing " : "Enable drawing ";
    }

    @Override
    public void checkProjectRequirements() {
        if (Global.isAsynchronousMode()) {
            throw new SinalgoFatalException(
                    "TCC is written to be executed in synchronous mode. It doesn't work in asynchronous mode.");
        }
    }

    @Override
    public void onExit() {
    }
}
