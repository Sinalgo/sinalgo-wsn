package projects.tcc.simulation.wsn;

import lombok.Getter;
import projects.tcc.simulation.io.SimulationOutput;
import projects.tcc.simulation.wsn.data.Sensor;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Simulation {

    private List<Double> residualEnergy;
    private List<Double> consumedEnergy;
    private List<Double> coverageArray;
    private double currentCoveragePercent;       // porcentagem de cobertura atual
    private double networkResidualEnergy;       // Energia Total Residual da rede.
    private double networkConsumedEnergy;      // Energia Total Consumida da rede.
    private int minBatteryThreshold;        // limite que se considera como bateria esgotada.
    private double consumedEnergyThreshold; //usado no teste de reestruturacao da rede.

    private int activeSensorsDelta;
    private boolean restructureNetwork;
    private double previousResidualEnergy;
    private int restructureCount;

    private List<Integer> activeSensorCount;
    private List<Integer> currentStage;

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

    public double getCurrentCoveragePercentage() {
        return SensorNetwork.currentInstance().getCurrentCoveragePercent();
    }

    private Simulation() {
        this.residualEnergy = new ArrayList<>();
        this.consumedEnergy = new ArrayList<>();
        this.coverageArray = new ArrayList<>();

        this.activeSensorCount = new ArrayList<>();
        this.currentStage = new ArrayList<>();

        this.minBatteryThreshold = 10;

        this.activeSensorsDelta = 0;
        this.previousResidualEnergy = 0.0;
        this.consumedEnergyThreshold = 0.05;
        this.restructureNetwork = false;
        this.restructureCount = 0;
    }

    public boolean simulatePeriod(int currentStage, SimulationOutput output) throws Exception {
        output.generateSimulatorOutput(currentStage);

        SensorNetwork network = SensorNetwork.currentInstance();

        // ========= Verificacao e Calculo de Energia no Periodo de tempo =========
        this.networkResidualEnergy = 0;
        this.networkConsumedEnergy = 0;
        for (Sensor sensor : network.getSensors()) {
            if (sensor.isAvailable()) {
                this.networkResidualEnergy += sensor.getBatteryEnergy();
            }
        }

        //Calculando a energia consumida
        this.networkConsumedEnergy = network.getTotalConsumedPowerInRound();

        //////////////////////// necessario para algumas aplicacoes //////////////////
        if (this.testNetworkRestructure(currentStage)) {
            this.restructureCount++;
        }
        ///////////////////////////////////////////////////////////////////////////////

        //Incluindo Energia consumida por Ativacao.
        this.networkConsumedEnergy += network.computePeriodActivationEnergy();
        //-----------------------------------------
        this.currentCoveragePercent = network.computeCoverage();

        this.activeSensorCount.add(network.getActiveSensorCount());
        this.currentStage.add(currentStage);

        this.residualEnergy.add(this.networkResidualEnergy);
        this.consumedEnergy.add(this.networkConsumedEnergy);
        this.coverageArray.add(this.currentCoveragePercent);

        //gerar impressao na tela
        output.generateConsoleOutput(currentStage);

        network.computePeriodConsumedEnergy();

        //Verificando se algum sensor nao estara na proxima simulacao
        return network.removeFailedSensors(this.minBatteryThreshold);
    }

    private boolean testNetworkRestructure(int currentStage) {
        this.restructureNetwork = false;
        //testando se ira reestruturar - nao considerar EA ///////////////////////////
        if (this.networkConsumedEnergy - this.previousResidualEnergy > this.consumedEnergyThreshold * this.previousResidualEnergy) {
            this.previousResidualEnergy = this.networkConsumedEnergy;
            if (currentStage > 1) {
                this.activeSensorsDelta = 0;
                this.restructureNetwork = true;
            }
        }
        if (currentStage > 0) {
            SensorNetwork network = SensorNetwork.currentInstance();
            this.activeSensorsDelta = Math.abs(this.activeSensorCount.get(this.activeSensorCount.size() - 1) - network.getActiveSensorCount());
            if (Double.compare(this.activeSensorsDelta, this.consumedEnergyThreshold * network.getAvailableSensorCount()) > 0) {
                this.activeSensorsDelta = 0;
                this.restructureNetwork = true;
            }
        }
        return this.restructureNetwork;
    }

}
