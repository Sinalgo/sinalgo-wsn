package projects.wsn.simulation.algorithms.genetic;

import projects.wsn.simulation.io.SimulationConfigurationLoader;
import projects.wsn.simulation.network.SensorNetwork;
import projects.wsn.simulation.network.data.DemandPoints;
import projects.wsn.simulation.network.data.Sensor;

import java.util.ArrayList;
import java.util.List;

import static projects.wsn.simulation.algorithms.genetic.FitnessType.TYPE_1;
import static projects.wsn.simulation.algorithms.genetic.FitnessType.TYPE_2;

public class StaticMultiObjectiveGeneticAlgorithm {

    public static boolean[] runMultiObjectiveGeneticAlgorithm(SensorNetwork network, int numberOfGenerations, int tamanhoPopulacao, double crossoverRate) {

        network.computeCostToSink(); //atulizando o custo de caminho de cada sensor ao sink;

        int sensorCount = network.getAvailableSensorCount();

        Population popCromo = new Population(tamanhoPopulacao, sensorCount, network.getAvailableSensorsArray(), crossoverRate);
        double sensorRadius = SimulationConfigurationLoader.getConfiguration().getSensorRadius();
        popCromo.startPop(DemandPoints.currentInstance().getTotalNumPoints(), sensorRadius, network.getCoverageFactor());
        calculaFuncaoObjetivo(network, popCromo.getPopCromossomo());
        calculaFuncaoObjetivo2(popCromo.getPopCromossomo());
        limpaPareto(popCromo.getPopCromossomo());
        gerarParetos(popCromo.getPopCromossomo());
        popCromo.calcularFO_MO_1();
        popCromo.setMelhorPareto();
        List<Cromossomo> vMelhorPareto = popCromo.copyMelhorPareto();

        for (int cNumeroGeracoes = 0; cNumeroGeracoes < numberOfGenerations; cNumeroGeracoes++) {
            popCromo.realizaCasamento();
            popCromo.realizaMutacao();

            calculaFuncaoObjetivo(network, popCromo.getPopCromossomo());
            calculaFuncaoObjetivo2(popCromo.getPopCromossomo());

            limpaPareto(popCromo.getPopCromossomo());
            gerarParetos(popCromo.getPopCromossomo());

            popCromo.setMelhorPareto();
            popCromo.incrementarValorPareto();
            popCromo.inserirPopArq(vMelhorPareto);
            popCromo.calcularFO_MO_1();

            popCromo.setPopCromossomo(selecaoRoleta(popCromo.getPopCromossomo(), popCromo.getTamPopOrig()));

            elitismoMelhorPareto(popCromo, vMelhorPareto);

            popCromo.ajustarValorPareto();
            popCromo.setMelhorPareto();
            vMelhorPareto = popCromo.copyMelhorPareto();
        }

        //imprimir teste
        limpaPareto(popCromo.getPopCromossomo());
        gerarParetos(popCromo.getPopCromossomo());
        popCromo.calcularFO_MO_3();
        popCromo.ordenaF1(); //ordena pela fitness
        popCromo.setMelhorPareto();

        vMelhorPareto = popCromo.copyMelhorPareto();
        //Separando os melhores paretos para uma média.
        List<Cromossomo> conjMelhorPareto = new ArrayList<>(vMelhorPareto);
        popCromo.setMelhorCromo(decSolPareto(conjMelhorPareto, network));
        limpaPareto(conjMelhorPareto);
        gerarParetos(conjMelhorPareto);
        popCromo.calcularFO_MO_3();

        return popCromo.getMelhorCromo().getVetorBits();
    }

    /*evaluates objective function for each chromossome*/
    private static void calculaFuncaoObjetivo(SensorNetwork rede, List<Cromossomo> pCromossomos) {
        double penAtiv = SimulationConfigurationLoader.getConfiguration().getActivationPower() +
                SimulationConfigurationLoader.getConfiguration().getMaintenancePower();
        int penNCob = 0;//100000 utilizado no mono-objetivo;
        for (Cromossomo indv : pCromossomos) {
            // avalia apenas quem precisa
            if (indv.isAvaliarFO()) {
                avaliarIndividuo(rede, indv, penAtiv, penNCob);
            }
        }
    }

    private static void avaliarIndividuo(SensorNetwork rede, Cromossomo individuo, double penAtiv, int penNCob) {
        int naoCoberturaAuxiliar = rede.computeNonCoverage(individuo.getListIdsAtivo());
        List<Sensor> activeSensors = new ArrayList<>(individuo.getListIdsAtivo().size());
        for (Integer activeSensorId : individuo.getListIdsAtivo()) {
            activeSensors.add(rede.getSensors().get(activeSensorId));
        }
        individuo.setNaoCobertura(naoCoberturaAuxiliar);
        double custoCaminhoTotal = 0;
        for (Sensor sens : activeSensors) {
            custoCaminhoTotal += sens.getCostToSink();
        }
        int penCustoCaminho = 100;
        individuo.setFitness(penAtiv, custoCaminhoTotal * penCustoCaminho, penNCob);
    }

    /*evaluates objective function for each chromossome*/
    private static void calculaFuncaoObjetivo2(List<Cromossomo> pCromossomos) {
        int penNCob = 100000;
        double penAtiv = 100000;
        for (Cromossomo indiv : pCromossomos) {
            double vFitness2 = indiv.getFitness(TYPE_2);
            // avalia apenas quem precisa
            if (vFitness2 < 0) {
                indiv.setFitness2(penNCob, penAtiv);
            }
        }
    }

    private static List<Cromossomo> selecaoRoleta(List<Cromossomo> popCromo, int tamPopOrig) {
        List<Cromossomo> popCromoAux = new ArrayList<>();
        double totalFitness = 0;
        for (Cromossomo aPopCromo1 : popCromo) {
            totalFitness = totalFitness + aPopCromo1.getFitnessMO();
        }
        for (int i = 0; i < tamPopOrig; i++) {
            int vRand = (int) (totalFitness * (Math.random()));
            double s = 0;
            for (Cromossomo aPopCromo : popCromo) {
                s += aPopCromo.getFitnessMO();
                if (s >= vRand) {
                    Cromossomo cromoClone = new Cromossomo(aPopCromo);
                    popCromoAux.add(cromoClone);
                    break;
                }
            }
        }
        return popCromoAux;
    }

    private static int[][] gerarMatDomi(List<Cromossomo> popCromo) {
        int tamPopCromo = popCromo.size();
        int[][] matDomin = new int[tamPopCromo][tamPopCromo];
        for (int j = 0; j < tamPopCromo; j++) {
            Cromossomo cromoA = popCromo.get(j);
            for (int i = 0; i < tamPopCromo; i++) {
                if (j != i) {
                    //Marca com 1 todos os que são dominados pela coluna J!!!
                    Cromossomo cromoB = popCromo.get(i);
                    matDomin[i][j] = testeDominancia(cromoA, cromoB) ? 1 : 0;
                } else {
                    matDomin[i][j] = 0;
                }
            }
        }
        return matDomin;
    }


    //Testando se o CromoA domina o CromoB
    private static boolean testeDominancia(Cromossomo cromoA, Cromossomo cromoB) {
        double fitnA1 = cromoA.getFitness(TYPE_1);
        double fitnA2 = cromoA.getFitness(TYPE_2);

        double fitnB1 = cromoB.getFitness(TYPE_1);
        double fitnB2 = cromoB.getFitness(TYPE_2);

        return (fitnB1 > fitnA1 && fitnB2 > fitnA2)
                || (fitnB1 >= fitnA1 && fitnB2 > fitnA2)
                || (fitnB1 > fitnA1 && fitnB2 >= fitnA2);
    }

    private static void gerarParetos(List<Cromossomo> popCromo) {
        int tamPopCromo = popCromo.size();
        int[][] matDomin = gerarMatDomi(popCromo);

        //separando os paretos
        int numPareto = 0;
        int numCromoPareto = 0;

        while (numCromoPareto != popCromo.size()) {
            numPareto++;
            for (int i = 0; i < tamPopCromo; i++) {
                //testando se o cromossomo já está no pareto
                if (popCromo.get(i).isPresPareto()) {
                    continue;
                }
                boolean testePareto = true;
                for (int j = 0; j < tamPopCromo; j++) {
                    if (matDomin[i][j] == 1) {
                        testePareto = false;
                        break;
                    }
                }
                if (testePareto) {
                    popCromo.get(i).setIdPareto(numPareto);
                    popCromo.get(i).setPresPareto(true);
                }
            }

            //tirando os pontos q foram para o pareto;
            for (int i = 0; i < popCromo.size(); i++) {
                if (popCromo.get(i).getIdPareto() == numPareto) {
                    //contando pontos q estão indo para um pareto
                    numCromoPareto++;
                    for (int j = 0; j < tamPopCromo; j++) {
                        matDomin[i][j] = -1;
                        matDomin[j][i] = -1;
                    }
                }
            }
        }
    }

    private static void limpaPareto(List<Cromossomo> popCromo) {
        for (Cromossomo aPopCromo : popCromo) {
            aPopCromo.setIdPareto(Integer.MAX_VALUE);
            aPopCromo.setPresPareto(false);
        }
    }

    private static boolean elitismoMelhorPareto(Population popCromo,
                                                List<Cromossomo> melhorPareto) {
        List<Cromossomo> paretoPopCorrente = popCromo.getMelhorPareto();

        int contIndvPareto = 0;
        for (Cromossomo isMelhorPareto : melhorPareto) {
            boolean isDominado = false;
            for (Cromossomo isParetoCorrente : paretoPopCorrente) {
                //Verificando se é dominado e se é o mesmo ponto
                if (testeDominancia(isParetoCorrente, isMelhorPareto) ||
                        (isMelhorPareto.getFitness(TYPE_1) == isParetoCorrente.getFitness(TYPE_1)
                                && isMelhorPareto.getFitness(TYPE_2) == isParetoCorrente.getFitness(TYPE_2))) {
                    isDominado = true;
                    break;
                }
            }

            //insere o cromossomo guardado caso ele ainda seja pareto
            if (!isDominado) {
                popCromo.getPopCromossomo().add(isMelhorPareto);
                contIndvPareto++;
            }

        }
        return contIndvPareto != melhorPareto.size();
    }

    private static Cromossomo decSolPareto(List<Cromossomo> conjSolPareto,
                                           SensorNetwork rede) {
        Cromossomo escolhido = conjSolPareto.get(conjSolPareto.size() - 1);
        double fator = 1.0 - rede.getCoverageFactor(); //% não cobertura
        for (Cromossomo candidato : conjSolPareto) {
            double nonCoveredPoints =
                    ((double) candidato.getNaoCobertura()) / ((double) DemandPoints.currentInstance().getTotalNumPoints());
            if (nonCoveredPoints == 0 || nonCoveredPoints <= fator) {
                escolhido = candidato;
                break;
            }
        }
        return escolhido;
    }

}
