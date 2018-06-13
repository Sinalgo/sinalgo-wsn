package projects.tcc.simulation.io;

import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.java.Log;
import sinalgo.configuration.Configuration;
import sinalgo.exception.CorruptConfigurationEntryException;
import sinalgo.exception.SinalgoFatalException;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Log
public class ConfigurationLoader {

    @Setter(AccessLevel.PRIVATE)
    private static SimulationConfiguration configuration;

    public static SimulationConfiguration getConfiguration() {
        if (configuration == null) {
            load();
        }
        return configuration;
    }

    public static void overrideConfiguration(SimulationConfiguration configuration) {
        setConfiguration(configuration);
    }

    private static void load() {
        try {
            setConfiguration(load(Configuration.getStringParameter("inputFile")));
        } catch (CorruptConfigurationEntryException e) {
            throw new SinalgoFatalException("Corrupt configuration, could not find file " +
                    "to use as input for the simulation", e);
        }
    }

    private static SimulationConfiguration load(String name) {
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
