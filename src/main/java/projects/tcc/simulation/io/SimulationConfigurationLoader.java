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
public class SimulationConfigurationLoader {

    @Setter(AccessLevel.PRIVATE)
    private static SimulationConfiguration configuration;

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private static String configFileName;

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private static Integer dimX;

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private static Integer dimY;

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private static Double coverageFactor;

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private static Double crossoverRate;

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private static Integer numberOfGenerations;

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private static Integer populationSize;

    public static SimulationConfiguration getConfiguration() {
        if (configuration == null) {
            load();
        }
        return configuration;
    }

    public static void overrideConfigurationFile(String configFileName) {
        setConfigFileName(configFileName);
    }

    public static void overrideDimensions(int dimX, int dimY) {
        setDimX(dimX);
        setDimY(dimY);
    }

    public static void overrideCoverageFactor(double coverageFactor) {
        setCoverageFactor(coverageFactor);
    }

    public static void overrideCrossoverRate(double crossoverRate) {
        setCrossoverRate(crossoverRate);
    }

    public static void overrideNumberOfGenerations(int numberOfGenerations) {
        setNumberOfGenerations(numberOfGenerations);
    }

    public static void overridePopulationSize(int populationSize) {
        setPopulationSize(populationSize);
    }


    private static void load() {
        loadFromSinalgoConfig();
        setConfiguration(load("projects/tcc/input/json/" + getConfigFileName() + ".json"));
    }

    private static void loadFromSinalgoConfig() {
        try {
            if (getConfigFileName() == null) {
                setConfigFileName(Configuration.getStringParameter("inputFile"));
            }
            if (getCoverageFactor() == null) {
                setCoverageFactor(Configuration.getDoubleParameter("coverageFactor") / 100);
            }
            if (getDimX() == null) {
                setDimX(Configuration.getDimX());
            }
            if (getDimY() == null) {
                setDimY(Configuration.getDimY());
            }
            if (getCrossoverRate() == null) {
                setCrossoverRate(Configuration.getDoubleParameter("crossoverRate"));
            }
            if (getPopulationSize() == null) {
                setPopulationSize(Configuration.getIntegerParameter("populationSize"));
            }
            if (getNumberOfGenerations() == null) {
                setNumberOfGenerations(Configuration.getIntegerParameter("numberOfGenerations"));
            }
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
            SimulationConfiguration config = new Gson()
                    .fromJson(reader.lines().collect(Collectors.joining("")), SimulationConfiguration.class);
            config.setCoverageFactor(getCoverageFactor());
            config.setDimX(getDimX());
            config.setDimY(getDimY());
            config.setCrossoverRate(getCrossoverRate());
            config.setNumberOfGenerations(getNumberOfGenerations());
            config.setPopulationSize(getPopulationSize());
            return config;
        } catch (Exception e) {
            log.severe("Error while loading " + resourcePath);
            throw new RuntimeException(e);
        }
    }


}
