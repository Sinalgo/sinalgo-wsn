package projects.tcc.simulation.io;

import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.Getter;
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

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private static String configFileName;

    public static SimulationConfiguration getConfiguration() {
        if (configuration == null) {
            load();
        }
        return configuration;
    }

    public static void overrideConfiguration(String configFileName) {
        setConfigFileName(configFileName);
    }

    private static void load() {
        try {
            if (getConfigFileName() == null) {
                setConfigFileName(Configuration.getStringParameter("inputFile"));
            }
            setConfiguration(load("projects/tcc/input/json/" + getConfigFileName() + ".json"));
        } catch (CorruptConfigurationEntryException e) {
            throw new SinalgoFatalException("Corrupt configuration, could not find file " +
                    "to use as input for the simulation", e);
        }
    }

    private static SimulationConfiguration load(String resourcePath) {
        log.info("Loading configuration from " + resourcePath);
        try (LineNumberReader reader = new LineNumberReader(new InputStreamReader(Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(resourcePath)))) {
            return new Gson().fromJson(reader.lines().collect(Collectors.joining("")), SimulationConfiguration.class);
        } catch (Exception e) {
            log.severe("Error while loading " + resourcePath);
            throw new RuntimeException(e);
        }
    }


}
