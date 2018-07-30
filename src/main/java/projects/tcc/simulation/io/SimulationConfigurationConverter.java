package projects.tcc.simulation.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.java.Log;
import projects.tcc.simulation.io.SimulationConfiguration.SensorConfiguration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class to convert the legacy input format to JSON
 */
@Log
public class SimulationConfigurationConverter {

    public static void main(String[] args) {
        try {
            String folder = "projects/tcc/input/text";
            log.info("Loading configurations at " + folder);
            URL inputFolder = Thread.currentThread()
                    .getContextClassLoader()
                    .getResource(folder);
            if (inputFolder != null) {
                Map<String, SimulationConfiguration> simulationConfigurations = new LinkedHashMap<>();
                for (Path inputFilePath : Files.newDirectoryStream(Paths.get(inputFolder.toURI()))) {
                    log.info("Configuration found: " + inputFilePath.getFileName());
                    LineNumberReader input =
                            new LineNumberReader(new InputStreamReader(Files.newInputStream(inputFilePath)));
                    List<SimulationConfiguration.SensorConfiguration> sensorConfigurations = new ArrayList<>();

                    input.readLine(); // skipping the number of nodes
                    double sensorRadius = Double.valueOf(input.readLine());
                    double commRadius = Double.valueOf(input.readLine());
                    double batteryEnergy = Double.valueOf(input.readLine());
                    double activationPower = Double.valueOf(input.readLine());
                    double receivePower = Double.valueOf(input.readLine());
                    double maintenancePower = Double.valueOf(input.readLine());
                    double commRatio = Double.valueOf(input.readLine());

                    String aLine;
                    while ((aLine = input.readLine()) != null) {
                        String[] values = aLine.split("\t");

                        double x = Double.parseDouble(values[1]);
                        double y = Double.parseDouble(values[2]);

                        sensorConfigurations.add(SimulationConfiguration.SensorConfiguration.builder()
                                .x(x)
                                .y(y)
                                .build());
                    }

                    simulationConfigurations.put(inputFilePath.getFileName().toString(),
                            SimulationConfiguration.builder()
                                    .sensorRadius(sensorRadius)
                                    .activationPower(activationPower)
                                    .batteryEnergy(batteryEnergy)
                                    .commRadius(commRadius)
                                    .commRatio(commRatio)
                                    .maintenancePower(maintenancePower)
                                    .receivePower(receivePower)
                                    .sensorConfigurations(sensorConfigurations)
                                    .sinkConfigurations(Collections.singletonList(
                                            SensorConfiguration.builder()
                                                    .x(0)
                                                    .y(0)
                                                    .build()))
                                    .build());
                }

                Gson gson = new GsonBuilder().setPrettyPrinting().create();

                String outputPath = "src/main/resources/projects/tcc/input/json";
                log.info("Saving configurations to " + outputPath);
                for (Map.Entry<String, SimulationConfiguration> entry : simulationConfigurations.entrySet()) {
                    log.info("Saving configuration as " + entry.getKey() + ".json");
                    File f = Paths.get(Paths.get("").toAbsolutePath().toString(),
                            outputPath, entry.getKey() + ".json").toFile();
                    String json = gson.toJson(entry.getValue());
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(f))) {
                        writer.write(json);
                        writer.flush();
                    }
                }
            }

        } catch (Exception e) {
            log.severe("Error while trying to convert configuration");
            throw new RuntimeException(e);
        }

    }
}
