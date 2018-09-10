package projects.tcc.simulation.wsn;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import projects.tcc.nodes.SimulationNode;
import projects.tcc.simulation.io.SimulationOutput;
import projects.tcc.simulation.wsn.data.DemandPoints;
import projects.tcc.simulation.wsn.data.Sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Getter
public class Simulation {

    @Getter
    @RequiredArgsConstructor
    public static class CoverageData {
        private final double sinkAwareCoveredPercent;
        private final double realCoveredPercent;
    }

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
        this.currentCoverageData = this.computeCoverageData();

        this.residualEnergy.add(this.networkResidualEnergy);
        this.consumedEnergy.add(this.networkConsumedEnergy);
        this.coverage.add(this.currentCoverageData);
        this.activeSensorCount.add(network.getActiveSensorCount());
        this.periods.add(currentStage);

        //gerar impressao na tela
        output.generateConsoleOutput(currentStage);
    }

    private CoverageData computeCoverageData() {
        int totalNumPoints = DemandPoints.currentInstance().getTotalNumPoints();
        int sinkAwareCoveredPointsNum = this.computeSinkAwareCoverage();
        int realCoveredPointsNum = this.computeRealCoverage();
        return new CoverageData(((double) sinkAwareCoveredPointsNum) / ((double) totalNumPoints),
                ((double) realCoveredPointsNum) / ((double) totalNumPoints));
    }

    private int computeSinkAwareCoverage() {
        return this.computeCoverage(Sensor::isFailed);
    }

    private int computeRealCoverage() {
        return this.computeCoverage(sn -> sn.getNode().isFailed());
    }

    private int computeCoverage(Function<Sensor, Boolean> stopCondition) {
        boolean[] coverageArray = new boolean[DemandPoints.currentInstance().getTotalNumPoints()];
        SensorNetwork.currentInstance().getSinks().forEach(s -> {
            if (s.getChildren() != null) {
                s.getChildren().forEach(c -> this.computeCoverage(c, coverageArray, stopCondition));
            }
        });
        int coveredPoints = 0;
        for (boolean b : coverageArray) {
            if (b) {
                coveredPoints += 1;
            }
        }
        return coveredPoints;
    }

    private void computeCoverage(Sensor s, boolean[] coverageArray, Function<Sensor, Boolean> stopCondition) {
        if (s.isActive() && !stopCondition.apply(s)) {
            s.getCoveredPoints().forEach(d -> coverageArray[d.getIndex()] = true);
            if (s.getChildren() != null) {
                s.getChildren().forEach(c -> this.computeCoverage(c, coverageArray, stopCondition));
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
