package projects.tcc.simulation.rssf.sensor.impl;

import projects.tcc.simulation.rssf.sensor.Sink;

import static projects.tcc.simulation.io.ConfigurationLoader.getConfiguration;

public class RSSFSink extends RSSFSensor implements Sink {

    @Override
    protected void performInitialization() {
        super.performInitialization();
        this.setPosition(getConfiguration().getSinkPosX(), getConfiguration().getSinkPosY(), 0);
    }

    @Override
    protected void computeCoveredPoints() {

    }

}
