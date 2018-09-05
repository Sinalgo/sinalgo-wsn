package projects.tcc.simulation.wsn;

import lombok.Getter;
import projects.tcc.simulation.io.SimulationOutput;
import projects.tcc.simulation.wsn.data.DemandPoint;
import projects.tcc.simulation.wsn.data.DemandPoints;
import projects.tcc.simulation.wsn.data.Sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Getter
public class Simulation {

    private List<Double> residualEnergy;
    private List<Double> consumedEnergy;
    private List<Double> coverage;
    private List<Integer> periods;
    private double currentCoveragePercent;       // porcentagem de cobertura atual
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
        this.currentCoveragePercent = this.getRealConnectedCoverage();

        this.residualEnergy.add(this.networkResidualEnergy);
        this.consumedEnergy.add(this.networkConsumedEnergy);
        this.coverage.add(this.currentCoveragePercent);
        this.activeSensorCount.add(network.getActiveSensorCount());
        this.periods.add(currentStage);

        //gerar impressao na tela
        output.generateConsoleOutput(currentStage);
    }

    private double getRealConnectedCoverage() {
        SensorNetwork network = SensorNetwork.currentInstance();
        DemandPoints demandPoints = DemandPoints.currentInstance();
        boolean[] connectedCoveredPoints = new boolean[demandPoints.getTotalNumPoints()];
        for (Sensor s : network.getSensors()) {
            if (s.isAvailable() && s.isActive() && s.isConnected()) {
                for (DemandPoint d : s.getCoveredPoints()) {
                    connectedCoveredPoints[d.getIndex()] = true;
                }
            }
        }
        int numCoveredPoints = 0;
        for (boolean b : connectedCoveredPoints) {
            if (b) {
                numCoveredPoints += 1;
            }
        }
        return ((double) numCoveredPoints) / ((double) demandPoints.getTotalNumPoints());
    }

    private double getTotalResidualEnergy() {
        return this.getTotalEnergy(Sensor::getBatteryEnergy);
    }

    private double getTotalBatteryCapacity() {
        return this.getTotalEnergy(Sensor::getBatteryCapacity);
    }

    private double getTotalEnergy(Function<Sensor, Double> energyFunction) {
        SensorNetwork network = SensorNetwork.currentInstance();
        double totalEnergy = 0;
        for (Sensor s : network.getSensors()) {
            if (s.isAvailable()) {
                totalEnergy += energyFunction.apply(s);
            }
        }
        return totalEnergy;
    }

}
