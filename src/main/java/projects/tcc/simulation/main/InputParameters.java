package projects.tcc.simulation.main;

import lombok.Getter;

@Getter
public class InputParameters {

    private String inputAmount;
    private String outputPath;
    private int initialTestNumber;
    private int lastTestNumber;

    private double coverageFactor;

    public InputParameters(String[] args) {
        int i = 0;
        this.inputAmount = args[i++];
        this.initialTestNumber = Integer.parseInt(args[i++]);
        this.lastTestNumber = Integer.parseInt(args[i++]);
        this.outputPath = args[i++];
        this.coverageFactor = Double.parseDouble(args[i]) / 100;
    }

}
