package projects.tcc.simulation.rssf;

import projects.tcc.simulation.data.SensorHolder;

import static projects.tcc.simulation.io.ConfigurationLoader.getConfiguration;

public class Sink extends Sensor {

    public Sink() {
        super();
        this.setPosition(getConfiguration().getSinkPosX(), getConfiguration().getSinkPosY(), 0);
        this.setCommRadius(getConfiguration().getSinkPosX());
        SensorHolder.setCurrentSink(this);
    }

}
