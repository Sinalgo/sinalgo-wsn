package projects.tcc.simulation.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import projects.tcc.simulation.io.SimulationOutput.OutputElement;
import projects.tcc.simulation.io.SimulationOutput.OutputElementList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataExporter {

    private static final Gson GSON = new GsonBuilder().create();

    public static void main(String[] args) {
        try {
            Files.list(Paths.get(args[0])).filter(p -> p.getFileName().toString().endsWith(".json")).forEach(p -> {
                List<String> csvRepresentation = Stream.concat(
                        Stream.of("Round,Active Sensor Count,Consumed Energy,Residual Energy,Real Coverage, Sink Coverage"),
                        readElementsList(p).stream().map(DataExporter::createCsv))
                        .collect(Collectors.toList());
                Path newPath = p.getParent().resolve(p.getFileName().toString().replace(".json", ".csv"));
                writeFile(csvRepresentation, newPath);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeFile(List<String> csvRepresentation, Path newPath) {
        try {
            Files.write(newPath, csvRepresentation, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<OutputElement> readElementsList(Path p) {
        try {
            OutputElementList elementList = GSON.fromJson(String.join("", Files.readAllLines(p)), OutputElementList.class);
            List<OutputElement> outputElements = new ArrayList<>();
            int lastTime = 1;
            for (OutputElement e : elementList.getElements()) {
                while (e.getRound() > lastTime + 1) {
                    outputElements.add(OutputElement.builder().round(++lastTime)
                            .residualEnergy(e.getResidualEnergy()).build());
                }
                outputElements.add(e);
                lastTime = e.getRound();
            }
            return outputElements;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String createCsv(OutputElement element) {
        return String.join(",", Integer.toString(element.getRound()), Integer.toString(element.getActiveSensorCount()),
                Double.toString(element.getConsumedEnergy()), Double.toString(element.getResidualEnergy()),
                Double.toString(element.getRealCoverage()), Double.toString(element.getSinkCoverage()));
    }

}
