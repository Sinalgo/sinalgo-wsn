package projects.tcc.simulation.wsn;

import lombok.Getter;
import projects.tcc.simulation.principal.Saidas;
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

    private int somaModDiffAtivos;
    private boolean reestrutrarRede;
    private double energiaResAnt;
    private int contChamadaReest;

    private List<Sensor> listSensores;

    private SensorNetwork rede;

    private List<Integer> nSensorAtivos;
    private List<Integer> nEstagio;

    private List<Sensor> listSensFalhosNoPer;

    public double getPorcCobAtual() {
        return this.rede.getCurrentCoveragePercent();
    }

    public Simulation(SensorNetwork rede) {
        this.residualEnergy = new ArrayList<>();
        this.consumedEnergy = new ArrayList<>();
        this.coverageArray = new ArrayList<>();

        this.nSensorAtivos = new ArrayList<>();
        this.nEstagio = new ArrayList<>();

        this.rede = rede;
        this.listSensores = rede.getAvailableSensors();

        this.minBatteryThreshold = 10;

        this.listSensFalhosNoPer = new ArrayList<>();

        this.somaModDiffAtivos = 0;
        this.energiaResAnt = 0.0;
        this.consumedEnergyThreshold = 0.05;
        this.reestrutrarRede = false;
        this.contChamadaReest = 0;
    }

    public boolean simulaUmPer(int estagioAtual, Saidas saida) throws Exception {
        saida.generateSimulatorOutput(estagioAtual);
        this.listSensFalhosNoPer.clear();

        // ========= Verificacao e Calculo de Energia no Periodo de tempo =========
        this.networkResidualEnergy = 0;
        this.networkConsumedEnergy = 0;
        for (Sensor sensor : this.listSensores) {
            this.networkResidualEnergy += sensor.getBatteryEnergy();
        }

        //Calculando a energia consumida
        this.networkConsumedEnergy = this.rede.calculaEnergiaConsPer();

        //////////////////////// necessario para algumas aplicacoes //////////////////
        if (this.testeReestruturarRede(estagioAtual)) {
            this.contChamadaReest++;
        }
        ///////////////////////////////////////////////////////////////////////////////

        //Incluindo Energia consumida por Ativacao.
        this.networkConsumedEnergy += this.rede.enAtivPeriodo();
        //-----------------------------------------
        this.currentCoveragePercent = this.rede.calcCobertura();

        this.nSensorAtivos.add(this.rede.getNumSensAtivos());
        this.nEstagio.add(estagioAtual);

        this.residualEnergy.add(this.networkResidualEnergy);
        this.consumedEnergy.add(this.networkConsumedEnergy);
        this.coverageArray.add(this.currentCoveragePercent);

        //gerar impressao na tela
        saida.gerarSaidaTela(estagioAtual);

        this.rede.calculaEnergiaPeriodo();

        //Verificando se algum sensor nao estara na proxima simulacao
        boolean evento = this.rede.retirarSensoresFalhaEnergia(this.listSensFalhosNoPer, this.minBatteryThreshold);
        this.rede.setPeriodFailedSensors(this.listSensFalhosNoPer);

        return evento;
    }

    private boolean testeReestruturarRede(int estagioAtual) {
        this.reestrutrarRede = false;
        //testando se ira reestruturar - nao considerar EA ///////////////////////////
        if (this.networkConsumedEnergy - this.energiaResAnt > this.consumedEnergyThreshold * this.energiaResAnt) {
            this.energiaResAnt = this.networkConsumedEnergy;
            if (estagioAtual > 1) {
                this.somaModDiffAtivos = 0;
                this.reestrutrarRede = true;
            }
        }
        if (estagioAtual > 0) {
            this.somaModDiffAtivos = Math.abs(this.nSensorAtivos.get(this.nSensorAtivos.size() - 1) - this.rede.getNumSensAtivos());
            if (this.somaModDiffAtivos > this.consumedEnergyThreshold * this.rede.getAvailableSensors().size()) {
                this.somaModDiffAtivos = 0;
                this.reestrutrarRede = true;
            }
        }
        return this.reestrutrarRede;
    }

}
