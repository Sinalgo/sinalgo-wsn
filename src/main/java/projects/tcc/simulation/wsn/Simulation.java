package projects.tcc.simulation.wsn;

import lombok.Getter;
import projects.tcc.simulation.io.SimulationConfigurationLoader;
import projects.tcc.simulation.io.SimulationOutput;
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
    private double currentCoveragePercent;       // porcentagem de cobertura atual
    private double networkResidualEnergy;       // Energia Total Residual da rede.
    private double networkConsumedEnergy;       // Energia Total Consumida da rede.

    private int activeSensorsDelta;
    private double previousResidualEnergy;
    private int restructureCount;

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
    }

    public void simulatePeriod(int currentStage) {
        SensorNetwork network = SensorNetwork.currentInstance();
        SimulationOutput output = SimulationOutput.currentInstance();

        // ========= Verificacao e Calculo de Energia no Periodo de tempo =========
        this.previousResidualEnergy = currentStage == 1 ? this.getTotalBatteryCapacity() : this.networkResidualEnergy;
        this.networkResidualEnergy = this.getTotalResidualEnergy();
        this.networkConsumedEnergy = this.previousResidualEnergy - this.networkResidualEnergy;
        this.currentCoveragePercent = DemandPoints.currentInstance().getCoveragePercent();

        this.residualEnergy.add(this.networkResidualEnergy);
        this.consumedEnergy.add(this.networkConsumedEnergy);
        this.coverage.add(this.currentCoveragePercent);
        this.activeSensorCount.add(network.getActiveSensorCount());

        //gerar impressao na tela
        output.generateConsoleOutput(currentStage);
    }

    public boolean restructureTest(int currentStage) {
        boolean restructure = this.testNetworkRestructure(currentStage);
        if (restructure) {
            this.restructureCount++;
        }
        return restructure;
    }

    private boolean testNetworkRestructure(int currentStage) {
        double consumedEnergyThreshold = SimulationConfigurationLoader.getConfiguration().getConsumedEnergyThreshold();
        boolean restructureNetwork = false;
        //testando se ira reestruturar - nao considerar EA ///////////////////////////
        if (currentStage > 2
                && this.estimateRoundConsumedEnergy() - this.previousResidualEnergy > consumedEnergyThreshold * this.previousResidualEnergy) {
            this.activeSensorsDelta = 0;
            restructureNetwork = true;
        }
        if (currentStage > 1) {
            SensorNetwork network = SensorNetwork.currentInstance();
            this.activeSensorsDelta = Math.abs(this.activeSensorCount.get(this.activeSensorCount.size() - 1) - network.getActiveSensorCount());
            if (Double.compare(this.activeSensorsDelta, consumedEnergyThreshold * network.getAvailableSensorCount()) > 0) {
                this.activeSensorsDelta = 0;
                restructureNetwork = true;
            }
        }
        return restructureNetwork;
    }

    private double estimateRoundConsumedEnergy() {
        return this.getTotalEnergy(Sensor::getEnergyConsumptionEstimate);
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
