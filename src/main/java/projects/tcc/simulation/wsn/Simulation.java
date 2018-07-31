package projects.tcc.simulation.wsn;

import lombok.Getter;
import projects.tcc.simulation.main.SimulationOutput;
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

    private List<Sensor> sensors;

    private SensorNetwork network;

    private List<Integer> activeSensorCount;
    private List<Integer> currentStage;

    private List<Sensor> periodFailedSensors;

    public double getCurrentCoveragePercentage() {
        return this.network.getCurrentCoveragePercent();
    }

    public Simulation(SensorNetwork network) {
        this.residualEnergy = new ArrayList<>();
        this.consumedEnergy = new ArrayList<>();
        this.coverageArray = new ArrayList<>();

        this.activeSensorCount = new ArrayList<>();
        this.currentStage = new ArrayList<>();

        this.network = network;
        this.sensors = network.getAvailableSensors();

        this.minBatteryThreshold = 10;

        this.periodFailedSensors = new ArrayList<>();

        this.activeSensorsDelta = 0;
        this.previousResidualEnergy = 0.0;
        this.consumedEnergyThreshold = 0.05;
        this.restructureNetwork = false;
        this.restructureCount = 0;
    }

    public boolean simulatePeriod(int currentStage, SimulationOutput output) throws Exception {
        output.generateSimulatorOutput(currentStage);
        this.periodFailedSensors.clear();

        // ========= Verificacao e Calculo de Energia no Periodo de tempo =========
        this.networkResidualEnergy = 0;
        this.networkConsumedEnergy = 0;
        for (Sensor sensor : this.sensors) {
            this.networkResidualEnergy += sensor.getBatteryEnergy();
        }

        //Calculando a energia consumida
        this.networkConsumedEnergy = this.network.calculaEnergiaConsPer();

        //////////////////////// necessario para algumas aplicacoes //////////////////
        if (this.testNetworkRestructure(currentStage)) {
            this.restructureCount++;
        }
        ///////////////////////////////////////////////////////////////////////////////

        //Incluindo Energia consumida por Ativacao.
        this.networkConsumedEnergy += this.network.enAtivPeriodo();
        //-----------------------------------------
        this.currentCoveragePercent = this.network.computeCoverage();

        this.activeSensorCount.add(this.network.getActiveSensorCount());
        this.currentStage.add(currentStage);

        this.residualEnergy.add(this.networkResidualEnergy);
        this.consumedEnergy.add(this.networkConsumedEnergy);
        this.coverageArray.add(this.currentCoveragePercent);

        //gerar impressao na tela
        output.generateConsoleOutput(currentStage);

        this.network.computePeriodConsumedEnergy();

        //Verificando se algum sensor nao estara na proxima simulacao
        boolean sensorsFailed = this.network.removeFailedSensors(this.periodFailedSensors, this.minBatteryThreshold);
        this.network.setPeriodFailedSensors(this.periodFailedSensors);

        return sensorsFailed;
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
            this.activeSensorsDelta = Math.abs(this.activeSensorCount.get(this.activeSensorCount.size() - 1) - this.network.getActiveSensorCount());
            if (Double.compare(this.activeSensorsDelta, this.consumedEnergyThreshold * this.network.getAvailableSensors().size()) > 0) {
                this.activeSensorsDelta = 0;
                this.restructureNetwork = true;
            }
        }
        return this.restructureNetwork;
    }

}
