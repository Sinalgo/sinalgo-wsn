
package projects.tcc.algorithms.genetic;

import java.util.ArrayList;

public class Pareto {

    private ArrayList<Cromossomo> popCromoPareto;

    public Pareto() {
        popCromoPareto = new ArrayList<>();
    }

    public void inserirSolPareto(Cromossomo solPareto) {
        popCromoPareto.add(solPareto);
    }

    public ArrayList<Cromossomo> getPopCromoPareto() {
        return popCromoPareto;
    }

    public void setPopCromoPareto(ArrayList<Cromossomo> popCromoPareto) {
        this.popCromoPareto = popCromoPareto;
    }

    public int getNumIndv() {
        return popCromoPareto.size();
    }

    public void crowdingDistance() {

        double INF = 1.7976931348623157e+308;
        double iDist, iDistAtual, fmax = 0, fmin = 0, fauxAnt = 0, fauxPos = 0;
        int numFOs = 2;

        int numIndv = popCromoPareto.size();

        for (Cromossomo aPopCromoPareto : popCromoPareto) {
            aPopCromoPareto.setCrowdingDist(0);
        }

        popCromoPareto.get(0).setCrowdingDist(INF);
        popCromoPareto.get(numIndv - 1).setCrowdingDist(INF);

        for (int k = 0; k < numFOs; k++) {

            //Ordenar as solu��es presente no Pareto segundo a fitness k+1
            popCromoPareto.sort(new ComparatorFitness(k + 1));
            /////////////////////////////////////////////////////////////

            //Calculos para F1
            if (k == 0) {
                fmin = popCromoPareto.get(0).getFitness();
                fmax = popCromoPareto.get(numIndv - 1).getFitness();
            } else if (k == 1) {
                fmin = popCromoPareto.get(0).getFitness2();
                fmax = popCromoPareto.get(numIndv - 1).getFitness2();
            }


            for (int i = 1; i < numIndv - 1; i++) {

                if (k == 0) {
                    fauxAnt = popCromoPareto.get(i - 1).getFitness();
                    fauxPos = popCromoPareto.get(i + 1).getFitness();
                } else if (k == 1) {
                    fauxAnt = popCromoPareto.get(i - 1).getFitness2();
                    fauxPos = popCromoPareto.get(i + 1).getFitness2();
                }

                iDist = (fauxPos - fauxAnt) / (fmax - fmin);
                iDistAtual = popCromoPareto.get(i).getCrowdingDist();

                popCromoPareto.get(i).setCrowdingDist(iDist + iDistAtual);

            }

        }


    }

    public void crowdingDistanceRoleta() {

        double iDist, iDistAtual, fmax = 0, fmin = 0, fauxAnt = 0, fauxPos = 0;
        int numFOs = 2;

        int numIndv = popCromoPareto.size();

        for (Cromossomo aPopCromoPareto : popCromoPareto) {
            aPopCromoPareto.setCrowdingDist(0);
        }

        for (int k = 0; k < numFOs; k++) {

            //Ordenar as solu��es presente no Pareto segundo a fitness k+1
            popCromoPareto.sort(new ComparatorFitness(k + 1));
            /////////////////////////////////////////////////////////////

            //Calculos para F1
            if (k == 0) {
                fmin = popCromoPareto.get(0).getFitness();
                fmax = popCromoPareto.get(numIndv - 1).getFitness();
            } else if (k == 1) {
                fmin = popCromoPareto.get(0).getFitness2();
                fmax = popCromoPareto.get(numIndv - 1).getFitness2();
            }


            for (int i = 1; i < numIndv - 1; i++) {

                if (k == 0) {
                    fauxAnt = popCromoPareto.get(i - 1).getFitness();
                    fauxPos = popCromoPareto.get(i + 1).getFitness();
                } else if (k == 1) {
                    fauxAnt = popCromoPareto.get(i - 1).getFitness2();
                    fauxPos = popCromoPareto.get(i + 1).getFitness2();
                }

                iDist = (fauxPos - fauxAnt) / (fmax - fmin);
                iDistAtual = popCromoPareto.get(i).getCrowdingDist();

                popCromoPareto.get(i).setCrowdingDist(iDist + iDistAtual);

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
