package projects.tcc.simulation.io;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import projects.tcc.simulation.wsn.SensorNetwork;
import projects.tcc.simulation.wsn.Simulation;

import java.util.function.Consumer;

@Getter(AccessLevel.PROTECTED)
public class SimulationOutput {

    private SensorNetwork network;
    private Simulation simulation;

    @Setter
    private static Consumer<String> printFunction = System.out::print;

    @Setter
    private static Consumer<String> printlnFunction = System.out::println;

    public static void print(String str) {
        printFunction.accept(str);
    }

    public static void println(String str) {
        printlnFunction.accept(str);
    }

    private static SimulationOutput currentInstance;

    public static SimulationOutput currentInstance() {
        if (currentInstance == null) {
            return newInstance();
        }
        return currentInstance;
    }

    public static SimulationOutput newInstance() {
        currentInstance = new SimulationOutput(SensorNetwork.currentInstance(), Simulation.currentInstance());
        return currentInstance;
    }

    private SimulationOutput(SensorNetwork network, Simulation simulation) {
        this.network = network;
        this.simulation = simulation;
    }

    public void generateConsoleOutput(int round) {
        println("\n");
        println(String.format("Round = %d", round));
        println(String.format("Active Sensors: %d", this.network.getActiveSensorCount()));
        println(String.format("Res. Energy: %.3f", this.simulation.getNetworkResidualEnergy()));
        println(String.format("Cons. Energy: %.3f", this.simulation.getNetworkConsumedEnergy()));
        println(String.format("Coverage (Sink): %.5f", this.simulation.getCurrentCoverageData().getSinkAwareCoveredPercent()));
        println(String.format("Coverage (Real): %.5f", this.simulation.getCurrentCoverageData().getUniversalCoveredPercent()));
    }

}
