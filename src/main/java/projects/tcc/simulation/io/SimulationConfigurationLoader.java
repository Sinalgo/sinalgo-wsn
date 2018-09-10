package projects.tcc.simulation.io;

import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.java.Log;
import sinalgo.configuration.Configuration;
import sinalgo.exception.SinalgoWrappedException;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Log
public class SimulationConfigurationLoader {

    @Setter(AccessLevel.PRIVATE)
    private static SimulationConfiguration configuration;

    public static SimulationConfiguration getConfiguration() {
        if (configuration == null) {
            load();
        }
        return configuration;
    }

    private static void load() {
        String resourcePath = "projects/tcc/positions.json";
        log.info("Loading configuration from " + resourcePath);
        try (LineNumberReader reader = new LineNumberReader(new InputStreamReader(Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(resourcePath)))) {
            SimulationConfiguration config = new Gson()
                    .fromJson(reader.lines().collect(Collectors.joining("")), SimulationConfiguration.class);
            config.setCoverageFactor(Configuration.getDoubleParameter("CoverageFactor"));
            config.setActivationPower(Configuration.getDoubleParameter("ActivationPower"));
            config.setSensorRadius(Configuration.getDoubleParameter("SensorRadius"));
            config.setCommRadius(Configuration.getDoubleParameter("CommRadius"));
            config.setSinkCommRadius(Configuration.getDoubleParameter("SinkCommRadius"));
            config.setBatteryEnergy(Configuration.getDoubleParameter("BatteryEnergy"));
            config.setActivationPower(Configuration.getDoubleParameter("ActivationPower"));
            config.setReceivePower(Configuration.getDoubleParameter("ReceivePower"));
            config.setMaintenancePower(Configuration.getDoubleParameter("MaintenancePower"));
            config.setCommRatio(Configuration.getDoubleParameter("CommRatio"));
            config.setCoverageFactor(Configuration.getDoubleParameter("CoverageFactor"));
            config.setPopulationSize(Configuration.getIntegerParameter("PopulationSize"));
            config.setNumberOfGenerations(Configuration.getIntegerParameter("NumberOfGenerations"));
            config.setCrossoverRate(Configuration.getDoubleParameter("CrossoverRate"));
            config.setConsumedEnergyThreshold(Configuration.getDoubleParameter("ConsumedEnergyThreshold"));
            config.setMinBatteryThreshold(Configuration.getDoubleParameter("MinBatteryThreshold"));
            config.setTransmitSpeedBps(Configuration.getIntegerParameter("TransmitSpeedBps"));
            config.setMinimizeActivationTree(Configuration.getBooleanParameter("MinimizeActivationTree"));
            setConfiguration(config);
        } catch (Exception e) {
            log.severe("Error while loading " + resourcePath);
            throw new SinalgoWrappedException(e);
        }
    }

}
