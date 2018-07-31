package projects.tcc.simulation.main;

import projects.tcc.simulation.wsn.SensorNetwork;
import projects.tcc.simulation.wsn.Simulation;
import projects.tcc.simulation.wsn.data.Sensor;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class SimulationOutput {

    private SensorNetwork network;
    private Simulation simulation;
    private String folder;

    public SimulationOutput(SensorNetwork network, Simulation simulation, String folder) {
        this.network = network;
        this.simulation = simulation;
        this.folder = folder;
        this.deleteSimulationFile();
    }

    private void deleteSimulationFile() {
        try {
            //Apagar um arquivo
            Files.deleteIfExists(Paths.get(this.folder, "simulacao"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateConsoleOutput(int periodo) {
        System.out.println("\n\n\n\n");
        System.out.println("Tempo = " + periodo);
        System.out.println("Numero de Sensores Ativos: " + this.network.getActiveSensorCount());
        System.out.println("Energia Residual: " + this.simulation.getNetworkResidualEnergy());
        System.out.println("Energia Consumida: " + this.simulation.getNetworkConsumedEnergy());
        System.out.println("Cobertura Atual: " + this.simulation.getCurrentCoveragePercentage());
    }

    public void generateSimulatorOutput(int currentStage) throws IOException {
        String outputFolder = this.folder + "simulacao";
        FileWriter fw = new FileWriter(outputFolder, true);
        PrintWriter pw = new PrintWriter(fw, true);
        pw.println(currentStage);
        int pCob = (int) (this.simulation.getCurrentCoveragePercentage() * 100);
        pw.println(pCob);
        for (Sensor s : this.network.getSensors()) {
            int sensorState = s.isFailed() ? 3 : s.isActive() ? 1 : 2;
            int parentId = s.isActive() && s.getParent() != null ? s.getParent().getSensorId() : -1;
            double batteryEnergy = s.getBatteryEnergy();

            pw.print(sensorState + "\t" + batteryEnergy + "\t" + parentId);
            pw.println();
        }
        pw.println();
        pw.close();
        fw.close();
    }

    public void generateSimulatorOutput(int testNumber, String algorithmName) throws IOException {
        List<Double> coverageArray = this.simulation.getCoverageArray();
        List<Double> consumedEnergy = this.simulation.getConsumedEnergy();
        List<Double> residualEnergy = this.simulation.getResidualEnergy();

        //Hibrido
        String nomeArq = this.folder + algorithmName + testNumber + ".out";
        FileWriter fw = new FileWriter(nomeArq);
        PrintWriter pw = new PrintWriter(fw);

        //Informando quantas linhas de dados irao ter no arquivo
        pw.print(coverageArray.size() + "\n");
        for (int i = 0; i < coverageArray.size(); i++) {
            pw.print(coverageArray.get(i) + "\t" +
                    consumedEnergy.get(i) + "\t" +
                    residualEnergy.get(i) + "\n");
        }
        pw.close();
    }

    public static void generateTimeOutput(String fileName, String folder, double time) throws IOException {
        String arq = folder + fileName;
        FileWriter fw = new FileWriter(arq);
        PrintWriter pw = new PrintWriter(fw);
        pw.print(time);
        pw.close();
    }

}
