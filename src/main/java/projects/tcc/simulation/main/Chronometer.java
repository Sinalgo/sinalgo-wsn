package projects.tcc.simulation.main;

import java.util.Date;

public class Chronometer {

    private Date start;
    private Date end;
    private long totalTime;

    public Chronometer() {
        this.totalTime = 0;
    }

    public double getTotalTime() {
        return (double) (this.totalTime) / (double) (1000);
    }

    public void start() {
        this.start = new Date();
    }

    public void end() {
        this.end = new Date();
        this.addTime();
    }

    private void addTime() {
        this.totalTime += this.end.getTime() - this.start.getTime();
    }

}
