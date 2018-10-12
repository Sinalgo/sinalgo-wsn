package projects.tcc.simulation.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import projects.tcc.simulation.wsn.SensorNetwork;
import projects.tcc.simulation.wsn.Simulation;

import java.util.ArrayList;
import java.util.List;
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
        println(String.format("Coverage (Real): %.5f", this.simulation.getCurrentCoverageData().getRealCoveredPercent()));
    }

    @Data
    @Builder
    public static class OutputElement {
        private transient int index;
        private transient double previousRoundDelta;
        private transient int indexSize;
        private final int round;
        private final double activeSensorCount;
        private final double residualEnergy;
        private final double consumedEnergy;
        private final double sinkCoverage;
        private final double realCoverage;
    }

    @Getter
    @NoArgsConstructor
    public static class OutputElementList {
        private final List<OutputElement> elements = new ArrayList<>();
    }

    public void generateFinalOutput() {
        System.out.println("\n");
        System.out.println("Final Simulation Results:");
        OutputElementList outputElements = new OutputElementList();
        for (int i = 0; i < this.simulation.getPeriods().size(); i++) {
            outputElements.getElements().add(OutputElement.builder()
                    .round(this.simulation.getPeriods().get(i))
                    .activeSensorCount(this.simulation.getActiveSensorCount().get(i))
                    .residualEnergy(this.simulation.getResidualEnergy().get(i))
                    .consumedEnergy(this.simulation.getConsumedEnergy().get(i))
                    .sinkCoverage(this.simulation.getCoverage().get(i).getSinkAwareCoveredPercent())
                    .realCoverage(this.simulation.getCoverage().get(i).getRealCoveredPercent())
                    .build());
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(outputElements));
    }

}
