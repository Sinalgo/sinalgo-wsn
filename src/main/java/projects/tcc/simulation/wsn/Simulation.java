package projects.tcc.simulation.wsn;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import projects.tcc.nodes.SimulationNode;
import projects.tcc.simulation.io.SimulationOutput;
import projects.tcc.simulation.wsn.data.DemandPoint;
import projects.tcc.simulation.wsn.data.DemandPoints;
import projects.tcc.simulation.wsn.data.Sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Getter
public class Simulation {

    @Getter
    @RequiredArgsConstructor
    public static class CoverageData {
        private final double sinkAwareCoveredPercent;
        private final double universalCoveredPercent;
    }

    private final static BiConsumer<DemandPoint, boolean[]> ADD_COVERAGE = (d, v) -> v[d.getIndex()] = true;

    private List<Double> residualEnergy;
    private List<Double> consumedEnergy;
    private List<CoverageData> coverage;
    private List<Integer> periods;
    private CoverageData currentCoverageData;       // porcentagem de cobertura atual
    private double networkResidualEnergy;       // Energia Total Residual da rede.
    private double networkConsumedEnergy;       // Energia Total Consumida da rede.
    private double previousResidualEnergy;

    private List<Integer> activeSensorCount;

    private static Simulation currentInstance;

    public static Simulation currentInstance() {
        if (currentInstance == null) {
            return newInstance();
        }
        return currentInstance;
    }

    public static Simulation newInstance() {
        currentInstance = new Simulation();
        return currentInstance;
    }

    private Simulation() {
        this.activeSensorCount = new ArrayList<>();
        this.residualEnergy = new ArrayList<>();
        this.consumedEnergy = new ArrayList<>();
        this.coverage = new ArrayList<>();
        this.periods = new ArrayList<>();
    }

    public void simulatePeriod(int currentStage) {
        SensorNetwork network = SensorNetwork.currentInstance();
        SimulationOutput output = SimulationOutput.currentInstance();

        // ========= Verificacao e Calculo de Energia no Periodo de tempo =========
        this.previousResidualEnergy = currentStage == 1 ? this.getTotalBatteryCapacity() : this.networkResidualEnergy;
        this.networkResidualEnergy = this.getTotalResidualEnergy();
        this.networkConsumedEnergy = this.previousResidualEnergy - this.networkResidualEnergy;
        this.currentCoverageData = this.getRealConnectedCoverage();

        this.residualEnergy.add(this.networkResidualEnergy);
        this.consumedEnergy.add(this.networkConsumedEnergy);
        this.coverage.add(this.currentCoverageData);
        this.activeSensorCount.add(network.getActiveSensorCount());
        this.periods.add(currentStage);

        //gerar impressao na tela
        output.generateConsoleOutput(currentStage);
    }

    private CoverageData getRealConnectedCoverage() {
        DemandPoints demandPoints = DemandPoints.currentInstance();
        boolean[] sinkAwareCoveredPoints = new boolean[demandPoints.getTotalNumPoints()];
        boolean[] universalCoveredPoints = new boolean[demandPoints.getTotalNumPoints()];
        SensorNetwork.currentInstance().getSinks().forEach(s -> {
            if (s.getNode().getChildren() != null) {
                for (SimulationNode c : s.getNode().getChildren()) {
                    this.computeCoveredPoints(c, sinkAwareCoveredPoints, universalCoveredPoints);
                }
            }
        });
        int sinkAwareCoveredPointsNum = 0;
        for (boolean b : sinkAwareCoveredPoints) {
            if (b) {
                sinkAwareCoveredPointsNum += 1;
            }
        }
        int universalCoveredPointsNum = 0;
        for (boolean b : universalCoveredPoints) {
            if (b) {
                universalCoveredPointsNum += 1;
            }
        }
        return new CoverageData(((double) sinkAwareCoveredPointsNum) / ((double) demandPoints.getTotalNumPoints()),
                ((double) universalCoveredPointsNum) / ((double) demandPoints.getTotalNumPoints()));
    }

    private void computeCoveredPoints(SimulationNode n,
                                      boolean[] sinkAwareCoveredPoints,
                                      boolean[] universalCoveredPoints) {
        if (n.isActive() && n.getSensor().isAvailable()) {
            if (n.isAvailable()) {
                n.getSensor().getCoveredPoints().forEach(d -> ADD_COVERAGE.accept(d, sinkAwareCoveredPoints));
                n.getSensor().getCoveredPoints().forEach(d -> ADD_COVERAGE.accept(d, universalCoveredPoints));
                if (n.getChildren() != null) {
                    n.getChildren().forEach(c -> this.computeCoveredPoints(c, sinkAwareCoveredPoints, universalCoveredPoints));
                }
            } else {
                n.getSensor().getCoveredPoints().forEach(d -> ADD_COVERAGE.accept(d, sinkAwareCoveredPoints));
                if (n.getChildren() != null) {
                    n.getChildren().forEach(c -> this.computeCoveredPoints(c, sinkAwareCoveredPoints, universalCoveredPoints));
                }
            }
        }
    }

    private double getTotalResidualEnergy() {
        return this.getTotalEnergy(SimulationNode::getBatteryEnergy);
    }

    private double getTotalBatteryCapacity() {
        return this.getTotalEnergy(SimulationNode::getBatteryCapacity);
    }

    private double getTotalEnergy(Function<SimulationNode, Double> energyFunction) {
        SensorNetwork network = SensorNetwork.currentInstance();
        double totalEnergy = 0;
        for (Sensor s : network.getSensors()) {
            SimulationNode sn = s.getNode();
            if (sn.isAvailable()) {
                totalEnergy += energyFunction.apply(sn);
            }
        }
        return totalEnergy;
    }

}
