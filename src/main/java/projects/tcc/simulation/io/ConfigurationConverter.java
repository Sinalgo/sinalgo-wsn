package projects.tcc.simulation.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigurationConverter {

    public static void main(String[] args) {
        try {
            URL inputFolder = Thread.currentThread()
                    .getContextClassLoader()
                    .getResource("projects/tcc/entrada/text");
            if (inputFolder != null) {
                Map<String, SimulationConfiguration> simulationConfigurations = new HashMap<>();
                for (Path file : Files.newDirectoryStream(Paths.get(inputFolder.toURI()))) {
                    LineNumberReader input = new LineNumberReader(new InputStreamReader(Files.newInputStream(file)));
                    List<SimulationConfiguration.SensorConfiguration> sensorConfigurations = new ArrayList<>();
                    input.readLine(); // skipping the number of nodes
                    double raioSens = Double.valueOf(input.readLine());
                    double raioCom = Double.valueOf(input.readLine());
                    double energBat = Double.valueOf(input.readLine());
                    double potAtiv = Double.valueOf(input.readLine());
                    double potRec = Double.valueOf(input.readLine());
                    double potManut = Double.valueOf(input.readLine());
                    double taxaCom = Double.valueOf(input.readLine());

                    String aLine;
                    while ((aLine = input.readLine()) != null) {
                        String[] values = aLine.split("\t");

                        long id = Long.parseLong(values[0]);
                        double x = Double.parseDouble(values[1]);
                        double y = Double.parseDouble(values[2]);

                        SimulationConfiguration.SensorConfiguration aux = SimulationConfiguration.SensorConfiguration
                                .builder()
                                .id(id)
                                .x(x)
                                .y(y)
                                .build();
                        sensorConfigurations.add(aux);
                    }

                    String path = file.toString();
                    simulationConfigurations.put(path.substring(path.lastIndexOf(File.separatorChar) + 1),
                            SimulationConfiguration.builder()
                                    .sensorRadius(raioSens)
                                    .activationPower(potAtiv)
                                    .batteryEnergy(energBat)
                                    .commRadius(raioCom)
                                    .commRatio(taxaCom)
                                    .maintenancePower(potManut)
                                    .receivePower(potRec)
                                    .sensors(sensorConfigurations)
                                    .sinkPosX(0)
                                    .sinkPosY(0)
                                    .build());
                }

                Gson gson = new GsonBuilder().setPrettyPrinting().create();

                for (Map.Entry<String, SimulationConfiguration> entry : simulationConfigurations.entrySet()) {
                    File f = Paths.get(Paths.get("").toAbsolutePath().toString(),
                            "src/main/resources/projects/tcc/entrada/json",
                            entry.getKey() + ".json").toFile();
                    String json = gson.toJson(entry.getValue());
                    BufferedWriter writer = new BufferedWriter(new FileWriter(f));
                    writer.write(json);
                    writer.flush();
                    writer.close();
                }
            }

        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
