package projects.tcc.simulation.wsn;

import lombok.Getter;
import projects.tcc.simulation.io.SimulationConfigurationLoader;
import projects.tcc.simulation.io.SimulationOutput;
import projects.tcc.simulation.wsn.data.DemandPoints;
import projects.tcc.simulation.wsn.data.Sensor;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Simulation {

    private double currentCoveragePercent;       // porcentagem de cobertura atual
    private double networkResidualEnergy;       // Energia Total Residual da rede.
    private double networkConsumedEnergy;      // Energia Total Consumida da rede.

    private int activeSensorsDelta;
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

    private Simulation() {
        this.activeSensorCount = new ArrayList<>();
        this.currentStage = new ArrayList<>();

        this.activeSensorsDelta = 0;
        this.previousResidualEnergy = 0.0;
        this.restructureCount = 0;
    }

    public boolean simulatePeriod(int currentStage) {
        SensorNetwork network = SensorNetwork.currentInstance();
        SimulationOutput output = SimulationOutput.currentInstance();

        // ========= Verificacao e Calculo de Energia no Periodo de tempo =========
        this.networkResidualEnergy = 0;
        this.networkConsumedEnergy = 0;
        for (Sensor sensor : network.getSensors()) {
            if (sensor.isAvailable()) {
                this.networkResidualEnergy += sensor.getBatteryEnergy();
            }
        }

        //Calculando a energia consumida
        this.networkConsumedEnergy = this.estimateRoundConsumedEnergy();

        //////////////////////// necessario para algumas aplicacoes //////////////////
        boolean restructure = this.testNetworkRestructure(currentStage);
        if (restructure) {
            this.restructureCount++;
        }
        ///////////////////////////////////////////////////////////////////////////////

        //Incluindo Energia consumida por Ativacao.
        this.networkConsumedEnergy += this.estimateRoundConsumedActivationEnergy();
        //-----------------------------------------
        this.currentCoveragePercent = DemandPoints.currentInstance().getCoveragePercent();

        this.activeSensorCount.add(network.getActiveSensorCount());
        this.currentStage.add(currentStage);

        //gerar impressao na tela
        output.generateConsoleOutput(currentStage);

        return restructure;
    }

    private boolean testNetworkRestructure(int currentStage) {
        double consumedEnergyThreshold = SimulationConfigurationLoader.getConfiguration().getConsumedEnergyThreshold();
        boolean restructureNetwork = false;
        //testando se ira reestruturar - nao considerar EA ///////////////////////////
        if (this.networkConsumedEnergy - this.previousResidualEnergy > consumedEnergyThreshold * this.previousResidualEnergy) {
            this.previousResidualEnergy = this.networkConsumedEnergy;
            if (currentStage > 1) {
                this.activeSensorsDelta = 0;
                restructureNetwork = true;
            }
        }
        if (currentStage > 0) {
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
        SensorNetwork network = SensorNetwork.currentInstance();
        double totalEnergySpent = 0;
        for (Sensor s : network.getSensors()) {
            if (s.isAvailable() && s.isActive()) {
                int childrenCount = s.queryDescendants();
                double receivePower = s.getReceivePower() * childrenCount;
                double transmitPower = s.getTransmitPower(s.getParent()) * (childrenCount + 1);
                double maintenancePower = s.getMaintenancePower();
                totalEnergySpent += receivePower + transmitPower + maintenancePower;
            }
        }
        return totalEnergySpent;
    }

    private double estimateRoundConsumedActivationEnergy() {
        SensorNetwork network = SensorNetwork.currentInstance();
        double totalActivationEnergy = 0;
        for (Sensor s : network.getSensors()) {
            if (s.isAvailable() && s.isUseActivationPower() && s.isActive()) {
                totalActivationEnergy += s.getActivationPower();
            }
        }
        return totalActivationEnergy;
    }

}
