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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataExporter {

    private static final Gson GSON = new GsonBuilder().create();
    private static int index = 1;
    private static int lastRound = 1;

    public static void main(String[] args) {
        try {
            Files.list(Paths.get(args[0])).filter(p -> p.getFileName().toString().endsWith(".json")).forEach(p -> {
                index = 1;
                lastRound = 1;
                List<String> csvRepresentation = Stream.concat(
                        Stream.of("Index,Round Delta,Round,Active Sensor Count,Consumed Energy," +
                                "Residual Energy,Real Coverage, Sink Coverage,Coverage Delta"),
                        readElementsList(p).getElements().stream().map(DataExporter::createCsv))
                        .collect(Collectors.toList());
                Path newPath = p.getParent().resolve(p.getFileName().toString().replace(".json", ".csv"));
                writeFile(csvRepresentation, newPath);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeFile(List<String> csvRepresentation, Path newPath) {
        try {
            Files.deleteIfExists(newPath);
            Files.write(newPath, csvRepresentation, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static OutputElementList readElementsList(Path p) {
        try {
            return GSON.fromJson(String.join("", Files.readAllLines(p)), OutputElementList.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String createCsv(OutputElement element) {
        String csv = String.join(",",
                Integer.toString(index++),
                Integer.toString(element.getRound() - lastRound),
                Integer.toString(element.getRound()),
                Integer.toString(element.getActiveSensorCount()),
                Double.toString(element.getConsumedEnergy()),
                Double.toString(element.getResidualEnergy()),
                Double.toString(element.getRealCoverage()),
                Double.toString(element.getSinkCoverage()),
                Double.toString(element.getSinkCoverage() - element.getRealCoverage()));
        lastRound = element.getRound();
        return csv;
    }

}
