package projects.tcc.simulation.io;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SimulationConfiguration {

    @Data
    @Builder
    public static class SensorConfiguration {
        private final long id;
        private final double x;
        private final double y;
    }

    private final double sensorRadius;
    private final double commRadius;
    private final double batteryEnergy;
    private final double activationPower;
    private final double receivePower;
    private final double maintenancePower;
    private final double commRatio;
    private final double sinkPosX;
    private final double sinkPosY;

    private final List<SensorConfiguration> sensors;

}
