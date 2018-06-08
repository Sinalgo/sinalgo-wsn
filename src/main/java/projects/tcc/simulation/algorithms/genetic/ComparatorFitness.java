package projects.tcc.simulation.algorithms.genetic;

import java.util.Comparator;

public class ComparatorFitness implements Comparator<Cromossomo> {

    private int tipoFitness;

    public ComparatorFitness(int tipoFitness) {

        this.tipoFitness = tipoFitness;

    }

    public int compare(Cromossomo cromo1, Cromossomo cromo2) {

        double difFitness = 0;

        if (tipoFitness == 1) {
            difFitness = cromo1.getFitness() - cromo2.getFitness();
        } else if (tipoFitness == 2) {
            difFitness = cromo1.getFitness2() - cromo2.getFitness2();
        } else {
            System.out.println("Erro na escolha da Fitness na compara��o");
            System.exit(0);
        }


        if (difFitness == 0)
            return 0;
        else if (difFitness > 0)
            return 1;
        else
            return -1;

    }

}
