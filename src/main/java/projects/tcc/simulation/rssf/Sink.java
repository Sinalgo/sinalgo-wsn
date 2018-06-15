package projects.tcc.simulation.rssf;

import projects.tcc.CustomGlobal;

import static projects.tcc.simulation.io.ConfigurationLoader.getConfiguration;

public class Sink extends Sensor {

    public Sink() {
        this.setPosition(getConfiguration().getSinkPosX(), getConfiguration().getSinkPosY(), 0);
        this.setCommRadius(getConfiguration().getSinkPosX());
        CustomGlobal.setCurrentSink(this);
    }

}
