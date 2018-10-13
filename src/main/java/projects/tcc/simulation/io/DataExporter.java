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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataExporter {

    private static final Gson GSON = new GsonBuilder().create();

    public static void main(String[] args) {
        try {
            List<Path> paths = Files.list(Paths.get(args[0]))
                    .filter(p -> p.getFileName().toString().endsWith(".json"))
                    .collect(Collectors.toList());
            paths.forEach(p -> {
                List<String> csvRepresentation = Stream.concat(
                        Stream.of("Index,Round Delta,Round,Active Sensor Count,Consumed Energy," +
                                "Residual Energy,Real Coverage,Sink Coverage,Coverage Delta (%)"),
                        DataExporter.createCsv(readElementsList(p)))
                        .collect(Collectors.toList());
                Path newPath = p.getParent().resolve(p.getFileName().toString().replace(".json", ".csv"));
                writeFile(csvRepresentation, newPath);
            });
            Map<Path, List<Path>> pathMap = paths.stream()
                    .collect(Collectors.groupingBy(p ->
                            p.getParent().resolve("average/"
                                    + p.getFileName().toString().replaceAll("\\s\\d+\\.json", ".csv"))));
            pathMap.forEach((outputPath, inputPaths) -> {
                List<String> csvRepresentation = Stream.concat(
                        Stream.of("Index,Count,Round Delta,Active Sensor Count,Consumed Energy," +
                                "Residual Energy,Real Coverage,Sink Coverage,Coverage Delta (%)"),
                        DataExporter.createAverageCsvForPath(inputPaths))
                        .collect(Collectors.toList());
                writeFile(csvRepresentation, outputPath);
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
            OutputElementList list = GSON.fromJson(String.join("", Files.readAllLines(p)), OutputElementList.class);
            setIndexAndPreviousRound(list);
            return list;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Stream<String> createCsv(OutputElementList outputElementList) {
        return outputElementList.getElements().stream().map(DataExporter::createCsv);
    }

    private static void setIndexAndPreviousRound(OutputElementList outputElementList) {
        int[] index = new int[]{1};
        int[] previousRound = new int[]{1};
        outputElementList.getElements().forEach(oe -> {
            oe.setIndex(index[0]++);
            oe.setPreviousRoundDelta(oe.getRound() - previousRound[0]);
            previousRound[0] = oe.getRound();
        });
    }

    private static String createCsv(OutputElement element) {
        return String.join(",",
                Integer.toString(element.getIndex()),
                Double.toString(element.getPreviousRoundDelta()),
                Integer.toString(element.getRound()),
                Double.toString(element.getActiveSensorCount()),
                Double.toString(element.getConsumedEnergy()),
                Double.toString(element.getResidualEnergy()),
                Double.toString(element.getRealCoverage()),
                Double.toString(element.getSinkCoverage()),
                Double.toString(element.getSinkCoverage() - element.getRealCoverage()));
    }

    private static String createAverageCsv(OutputElement element) {
        return String.join(",",
                Integer.toString(element.getIndex()),
                Integer.toString(element.getIndexSize()),
                Double.toString(element.getPreviousRoundDelta()),
                Double.toString(element.getActiveSensorCount()),
                Double.toString(element.getConsumedEnergy()),
                Double.toString(element.getResidualEnergy()),
                Double.toString(element.getRealCoverage()),
                Double.toString(element.getSinkCoverage()),
                Double.toString(element.getCoverageDiff()));
    }

    private static Stream<String> createAverageCsvForPath(List<Path> paths) {
        return createAverageCsv(paths.stream().map(DataExporter::readElementsList), paths.size());
    }

    private static Stream<String> createAverageCsv(Stream<OutputElementList> elements, int count) {
        return elements
                .map(OutputElementList::getElements)
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(OutputElement::getIndex))
                .entrySet()
                .stream()
                .map(e -> {
                    List<OutputElement> s = e.getValue();
                    return OutputElement.builder()
                            .index(e.getKey())
                            .indexSize(s.size())
                            .previousRoundDelta(average(s, OutputElement::getPreviousRoundDelta))
                            .activeSensorCount(average(s, OutputElement::getActiveSensorCount))
                            .residualEnergy(average(s, OutputElement::getResidualEnergy))
                            .consumedEnergy(average(s, OutputElement::getConsumedEnergy))
                            .realCoverage(sum(s, OutputElement::getRealCoverage) / count)
                            .sinkCoverage(sum(s, OutputElement::getSinkCoverage) / count)
                            .coverageDiff(average(s, el -> el.getSinkCoverage() - el.getRealCoverage()))
                            .build();
                }).map(DataExporter::createAverageCsv);
    }

    private static <T> double sum(List<T> list, Function<T, Double> extractor) {
        return list.stream().mapToDouble(extractor::apply).sum();
    }

    private static <T> double average(List<T> list, Function<T, Double> extractor) {
        return list.stream().mapToDouble(extractor::apply).average().orElse(0);
    }

}
