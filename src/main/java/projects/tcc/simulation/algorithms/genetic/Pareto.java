
package projects.tcc.simulation.algorithms.genetic;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class Pareto {

    @Getter
    @Setter
    private List<Cromossomo> popCromoPareto;

    public Pareto() {
        popCromoPareto = new ArrayList<>();
    }

    public void inserirSolPareto(Cromossomo solPareto) {
        popCromoPareto.add(solPareto);
    }

    public int getNumIndv() {
        return popCromoPareto.size();
    }

    public void crowdingDistance() {

        double INF = 1.7976931348623157e+308;
        double iDist, iDistAtual, fmax = 0, fmin = 0, fauxAnt = 0, fauxPos = 0;

        int numIndv = this.getNumIndv();

        for (Cromossomo aPopCromoPareto : popCromoPareto) {
            aPopCromoPareto.setCrowdingDist(0);
        }

        popCromoPareto.get(0).setCrowdingDist(INF);
        popCromoPareto.get(numIndv - 1).setCrowdingDist(INF);

        for (ComparatorFitness.FitnessType type : ComparatorFitness.FitnessType.values()) {

            //Ordenar as solu��es presente no Pareto segundo a fitness k+1
            popCromoPareto.sort(new ComparatorFitness(type));
            /////////////////////////////////////////////////////////////

            //Calculos para F1
            fmin = popCromoPareto.get(0).getFitnessOfType(type);
            fmax = popCromoPareto.get(numIndv - 1).getFitnessOfType(type);

            for (int i = 1; i < numIndv - 1; i++) {
                fauxAnt = popCromoPareto.get(i - 1).getFitnessOfType(type);
                fauxPos = popCromoPareto.get(i + 1).getFitnessOfType(type);

                Cromossomo thisChromosome = popCromoPareto.get(i);

                iDist = (fauxPos - fauxAnt) / (fmax - fmin);
                iDistAtual = thisChromosome.getCrowdingDist();

                thisChromosome.setCrowdingDist(iDist + iDistAtual);
            }

        }


    }

    public void crowdingDistanceRoleta() {

        double iDist, iDistAtual, fmax = 0, fmin = 0, fauxAnt = 0, fauxPos = 0;

        int numIndv = this.getNumIndv();

        for (Cromossomo aPopCromoPareto : popCromoPareto) {
            aPopCromoPareto.setCrowdingDist(0);
        }

        for (ComparatorFitness.FitnessType type : ComparatorFitness.FitnessType.values()) {

            //Ordenar as solu��es presente no Pareto segundo a fitness k+1
            popCromoPareto.sort(new ComparatorFitness(type));
            /////////////////////////////////////////////////////////////

            //Calculos para F1
            fmin = popCromoPareto.get(0).getFitnessOfType(type);
            fmax = popCromoPareto.get(numIndv - 1).getFitnessOfType(type);

            for (int i = 1; i < numIndv - 1; i++) {
                fauxAnt = popCromoPareto.get(i - 1).getFitnessOfType(type);
                fauxPos = popCromoPareto.get(i + 1).getFitnessOfType(type);

                Cromossomo thisChromosome = popCromoPareto.get(i);

                iDist = (fauxPos - fauxAnt) / (fmax - fmin);
                iDistAtual = thisChromosome.getCrowdingDist();

                thisChromosome.setCrowdingDist(iDist + iDistAtual);

            }

            //C�lculo para os extremos
            fauxAnt = popCromoPareto.get(0).getFitness();
            fauxPos = popCromoPareto.get(numIndv - 1).getFitness();

            iDist = (fauxPos - fauxAnt) / (fmax - fmin);

            iDistAtual = popCromoPareto.get(0).getCrowdingDist();
            popCromoPareto.get(0).setCrowdingDist(iDist + iDistAtual);

            iDistAtual = popCromoPareto.get(numIndv - 1).getCrowdingDist();
            popCromoPareto.get(numIndv - 1).setCrowdingDist(iDist + iDistAtual);

        }

    }

}
