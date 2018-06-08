package projects.tcc.simulation.io;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Log
public class ConfigurationLoader {

    public static SimulationConfiguration load(String name) {
        String filePath = "projects/tcc/input/json/" + name + ".json";
        log.info("Loading configuration from " + filePath);
        try (LineNumberReader reader = new LineNumberReader(new InputStreamReader(Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(filePath)))) {
            return new Gson().fromJson(reader.lines().collect(Collectors.joining("")), SimulationConfiguration.class);
        } catch (Exception e) {
            log.severe("Error while loading" + filePath);
            throw new RuntimeException(e);
        }
    }

}
