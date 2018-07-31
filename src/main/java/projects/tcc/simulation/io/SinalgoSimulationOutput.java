package projects.tcc.simulation.io;

import projects.tcc.simulation.wsn.SensorNetwork;
import projects.tcc.simulation.wsn.Simulation;

import java.io.IOException;

public class SinalgoSimulationOutput extends SimulationOutput {

    public SinalgoSimulationOutput() {
        super(SensorNetwork.currentInstance(), Simulation.currentInstance(), null);
    }

    @Override
    public void generateSimulatorOutput(int currentStage) throws IOException {

    }

    @Override
    public void generateSimulatorOutput(int testNumber, String algorithmName) throws IOException {

    }

    @Override
    public void generateTimeOutput(String fileName, double time) throws IOException {

    }
}
