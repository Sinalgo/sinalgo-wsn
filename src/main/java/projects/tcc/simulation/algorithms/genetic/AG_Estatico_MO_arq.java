package projects.tcc.simulation.algorithms.genetic;

import projects.tcc.simulation.rssf.RedeSensor;
import projects.tcc.simulation.rssf.Sensor;

import java.util.ArrayList;

public class AG_Estatico_MO_arq {

    public static boolean[] resolveAG_Estatico_MO(RedeSensor rede, int numeroGeracoes, int tamanhoPopulacao, double txCruzamento, double txMutacao) throws Exception {

        ArrayList<Sensor> listSensensores = rede.getAvailableSensors();

        rede.calCustosCaminho(); //atulizando o custo de caminho de cada sensor ao sink;

        ArrayList<Cromossomo> vMelhorPareto;

        int numGer = 0;
        double medFitness = 0;
        int numSA = 0;

        int vNumBits = rede.getAvailableSensors().size();

        Populacao popCromo = new Populacao(tamanhoPopulacao, vNumBits, rede.getVetIdsSensDisp(), txCruzamento);

        double raioSens = listSensensores.get(0).getSensorRadius();

        popCromo.startPop(rede.getArea(), raioSens, rede.getFatorCob());

        calculaFuncaoObjetivo(rede, popCromo.getPopCromossomo());

        calculaFuncaoObjetivo2(rede, popCromo.getPopCromossomo());

        limpaPareto(popCromo.getPopCromossomo());
        gerarParetos(popCromo.getPopCromossomo());
        popCromo.calcularFO_MO_1();

        //			popCromo.ordenaParetos();

        popCromo.setMelhorPareto();
        vMelhorPareto = popCromo.copyMelhorPareto();

        int cont = 0;
        //			while (contConv < 10)
        for (int cNumeroGeracoes = 0; cNumeroGeracoes < numeroGeracoes; cNumeroGeracoes++) {

            popCromo.realizaCasamento();
            popCromo.realizaMutacao();

            calculaFuncaoObjetivo(rede, popCromo.getPopCromossomo());
            calculaFuncaoObjetivo2(rede, popCromo.getPopCromossomo());

            limpaPareto(popCromo.getPopCromossomo());
            gerarParetos(popCromo.getPopCromossomo());


            //popCromo.ordenaParetos(); // Ordena os Paretos!

            popCromo.setMelhorPareto();
            popCromo.incrementarValorPareto();
            popCromo.inserirPopArq(vMelhorPareto);
            popCromo.calcularFO_MO_1();
            //conjMelhorPareto = popCromo.getMelhorPareto();

            //saidaMO.geraArqSaidaMO3("paretoOtimo"+cont+".txt", conjMelhorPareto);
            //saidaMO.geraArqSaidaPop("popTotal"+cont+".txt", popCromo.getPopCromossomo());

            popCromo.setPopCromossomo(selecaoRoleta(popCromo.getPopCromossomo(), popCromo.getTamPopOrig()));
            //operAG.selecaoRoleta(popCromo, popCromo.getTamPopOrig(), vNumBits);

            //				System.out.println("Saiu = " + cont);
            //				System.out.println("size = " + popCromo.getPopCromossomo().size());
            //				System.out.println("Entrou no elitismo = " + cont);
            boolean testeConv = elitismoMelhorPareto(popCromo, vMelhorPareto);

            if (!testeConv) {
            }

            //				System.out.println("size = " + popCromo.getPopCromossomo().size());
            //				System.out.println("contConv = " + contConv);
            //				System.out.println("Saiu do elitismo = " + cont);

            popCromo.ajustarValorPareto();
            popCromo.setMelhorPareto();
            vMelhorPareto = popCromo.copyMelhorPareto();

            cont++;

        }

        //imprimir teste
        limpaPareto(popCromo.getPopCromossomo());
        gerarParetos(popCromo.getPopCromossomo());
        popCromo.calcularFO_MO_3();
        popCromo.ordenaF1(); //ordena pela fitness
        popCromo.setMelhorPareto();
        //			popCromo.ordenaParetos();
        //			for (int k = 0; k < popCromo.getPopSize(); k++) {

        //			System.out.println(k + " pareto = " + popCromo.getPopCromossomo().get(k).getIdPareto()
        //			+ "\t fitnessPareto = " + popCromo.getPopCromossomo().get(k).getFitnessMO());

        //			}

        vMelhorPareto = popCromo.copyMelhorPareto();
        //Separando os melhores paretos para uma m�dia.

        ArrayList<Cromossomo> conjMelhorPareto = new ArrayList<>(vMelhorPareto);


        //			System.out.println("numero de geracaoes = " + cont);


        //imprimindo a Popula��o resultante.
        //popCromo.printPop();

        //Escolher um resultado no pareto
        //			popCromo.ordenaFitness1(); //ordena pela fitness

        popCromo.setMelhorCromo(decSolPareto(conjMelhorPareto, listSensensores, rede));

        //pegando o melhor indiv�duo (melhor Cromossomo)
        Cromossomo vMelhorCromossomo = popCromo.getMelhorIndv();
        //vMelhorCromossomo = popCromo.getMelhorIndvRol();

		/*System.out.println("\n Solucao do AG:");
			popCromo.printMelhorIndv();*/

        //			mAmbiente.incrementaCont();
        //			int contAG = mAmbiente.getCont();

        //output.geraArqSaidaMO("paretoOtimo.txt", conjMelhorPareto);
        //output.geraArqSaidaPopMO("popTotal"+contAG+".txt", popCromo.getPopCromossomo());
        //output.geraArqSaidaMpPareto("melhorPonto.txt", popCromo.getMelhorIndv());

		/*cont--;
			output.geraArqSaidaMpPareto("melhorPonto"+cont+".txt", popCromo.getMelhorIndv());
		 */


		/*System.out.println("\nNumero de Ativos Gen�ico: " + 
					vMelhorCromossomo.getNumeroAtivos());
		 */

        //Fazendo a m�dia dos resultados...


        limpaPareto(conjMelhorPareto);
        gerarParetos(conjMelhorPareto);
        popCromo.calcularFO_MO_3();
        //popCromo.ordenaParetos();


        //geraArqSaidaMO("testeMO.out", conjMelhorPareto);/*
        //geraArqSaidaMO2("testeMO2.out", conjMelhorPareto);*/
        //geraArqSaidaMpPareto("pePareto.out", popCromo.getMelhorIndv());


        return popCromo.getMelhorIndv().getVetorBoolean();

    }


    // Prepara��o para uma nova chamada do AG_Estatico
	/*public static int [] novaSolucao (Ambiente mAmbiente, RedeSensor rede, Saidas output) throws Exception{

		ArrayList<Sensor> popTotal;
		ArrayList<Sensor> popAG;

		popTotal = rede.getPopSensores();

		popAG = new ArrayList<Sensor>();


		for (int i = 0; i < popTotal.size(); i++) {    		
			if (!(popTotal.get(i).estaFalha()))    			
				popAG.add(popTotal.get(i));    		    		
		}


		int [] vetBits_popAG  = new int[popAG.size()-1]; //-1 pois o Sink n�o entra    	
		int [] vetBits_return = new int[rede.getNumSensInicial()];

		for(int i = 0; i < rede.getNumSensInicial(); i++)
			vetBits_return[i] = 0;


		if (popAG.size() > 3) {
			vetBits_popAG = resolveAG_Estatico_MO (rede, mAmbiente, popAG, null, output);
		}

		else
			return vetBits_return;



		// Refazendo o vetBits
		for (int i = 0; i < vetBits_popAG.length; i++) {

			if (vetBits_popAG[i] == 1) {

				int idSens = popAG.get(i).getId();

				vetBits_return[idSens] = 1;

			}

		}

		return vetBits_return;


	}*/


    /*evaluates objective function for each chromossome*/
    static void calculaFuncaoObjetivo(RedeSensor rede, ArrayList<Cromossomo> pCromossomos) {

        ArrayList<Sensor> popSensores = rede.getAvailableSensors();

        double penAtiv = popSensores.get(0).getActivationPower() + popSensores.get(0).getMaintenancePower();
        int penNCob = 0;//100000 utilizado no mono-objetivo;

        for (Cromossomo indv : pCromossomos) {
            // avalia apenas quem precisa
            if (indv.isAvaliarFO()) {
                avaliarIndividuo(rede, indv, penAtiv, penNCob);
            }
        }
    }


    public static void avaliarIndividuo(RedeSensor rede, Cromossomo individuo, double penAtiv, int penNCob) {
        // TODO Auto-generated method stub

        int naoCoberturaAuxiliar = rede.avaliaNaoCoberturaSemConect(individuo.getListIdsAtivo());
        individuo.setNaoCobertura(naoCoberturaAuxiliar);

        rede.ativarSensoresVetBits(individuo.getVetorBits());
        double custoCaminhoTotal = 0;
        for (Sensor sens : rede.getAvailableSensors()) {
            if (sens.isActive())
                custoCaminhoTotal += sens.getPathToSinkCost();
        }

        int penCustoCaminho = 100;

        individuo.setFitness(penAtiv, custoCaminhoTotal * penCustoCaminho, penNCob);

    }


    /*evaluates objective function for each chromossome*/
    static void calculaFuncaoObjetivo2(RedeSensor rede, ArrayList<Cromossomo> pCromossomos) {

        double raioSens;
        int penNCob = 100000;
        double penAtiv = 100000;

        raioSens = rede.getAvailableSensors().get(0).getSensorRadius();

        for (Cromossomo indiv : pCromossomos) {

            double vFitness2 = indiv.getFitness2();

            // avalia apenas quem precisa
            if (vFitness2 < 0) {
                indiv.setFitness2(raioSens, penNCob, penAtiv);

            }
        }

    }

    public static ArrayList<Cromossomo> selecaoRoleta(ArrayList<Cromossomo> popCromo, int tamPopOrig) {

        ArrayList<Cromossomo> popCromoAux = new ArrayList<>();

        double totalFitness = 0;

        for (Cromossomo aPopCromo1 : popCromo) {
            totalFitness = totalFitness + aPopCromo1.getFitnessMO();
        }

        for (int i = 0; i < tamPopOrig; i++) {

            int vRand = (int) (totalFitness * (Math.random()));

            double s = 0;

            for (Cromossomo aPopCromo : popCromo) {

                s = s + aPopCromo.getFitnessMO();

                if (s >= vRand) {
                    Cromossomo cromoCorrente = aPopCromo;
                    Cromossomo cromoClone = new Cromossomo(cromoCorrente);
                    popCromoAux.add(cromoClone);
                    break;
                }


            }

        }

        return popCromoAux;
    }


    public static int[][] gerarMatDomi(ArrayList<Cromossomo> popCromo) {

        int tamPopCromo = popCromo.size();
        int[][] matDomin = new int[tamPopCromo][tamPopCromo];

        for (int j = 0; j < tamPopCromo; j++) {

            Cromossomo cromoA = popCromo.get(j);

            for (int i = 0; i < tamPopCromo; i++) {

                Cromossomo cromoB = popCromo.get(i);

                if (j != i) {
                    //Marca com 1 todos os que s�o dominados pela coluna J!!!
                    if (testeDominancia(cromoA, cromoB)) {
                        matDomin[i][j] = 1;
                    } else
                        matDomin[i][j] = 0;

                } else {
                    matDomin[i][j] = 0;
                }

            }

        }

        return matDomin;

    }


    //Testando se o CromoA domina o CromoB
    public static boolean testeDominancia(Cromossomo cromoA, Cromossomo cromoB) {

        double fitnA1 = cromoA.getFitness();
        double fitnA2 = cromoA.getFitness2();

        double fitnB1 = cromoB.getFitness();
        double fitnB2 = cromoB.getFitness2();

        if (fitnB1 > fitnA1 && fitnB2 > fitnA2)
            return true;
        else if (fitnB1 >= fitnA1 && fitnB2 > fitnA2)
            return true;
        else if (fitnB1 > fitnA1 && fitnB2 >= fitnA2)
            return true;
        else
            return false;


    }

    public static void gerarParetos(ArrayList<Cromossomo> popCromo) {

        int tamPopCromo = popCromo.size();
        int[][] matDomin;


        matDomin = gerarMatDomi(popCromo);


        //separando os paretos
        int numPareto = 0;
        int numCromoPareto = 0;

        //while (numCromoPareto < tamPop) {
        while (numCromoPareto != popCromo.size()) {


            numPareto++;

            for (int i = 0; i < tamPopCromo; i++) {

                //testando se o cromossomo j� est� no pareto
                if (popCromo.get(i).getPresPareto())
                    continue;

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
                    //popCromo.get(i).setFitnessMO((double) numPareto);
                }

            }


            //tirando os pontos q foram para o pareto;
            //for (int i = 0; i < paretoCorrente.size(); i++) {
            for (int i = 0; i < popCromo.size(); i++) {

                //int ind = paretoCorrente.get(i);
                if (popCromo.get(i).getIdPareto() == numPareto) {

                    //contando pontos q est�o indo para um pareto
                    numCromoPareto++;

                    for (int j = 0; j < tamPopCromo; j++) {

                        matDomin[i][j] = -1;
                        matDomin[j][i] = -1;
                    }
                }

            }


            //			System.out.println("numCromoPareto = " + numCromoPareto);
            //			System.out.println("tamPopCromo = " + tamPopCromo);

            //			for (int i = 0; i < tamPopCromo; i++) {
            //			for (int j = 0; j < tamPopCromo; j++) {
            //			System.out.print(matDomin[i][j] + " ");
            //			}
            //			System.out.println("\t" + popCromo.get(i).getPresPareto()
            //			+ "\t" + popCromo.get(i).getIdPareto());
            //			}

        }

    }

    public static void limpaPareto(ArrayList<Cromossomo> popCromo) {

        for (Cromossomo aPopCromo : popCromo) {
            aPopCromo.setIdPareto(Integer.MAX_VALUE);
            aPopCromo.setPresPareto(false);
        }

    }


    public static boolean elitismoMelhorPareto(Populacao popCromo,
                                               ArrayList<Cromossomo> melhorPareto) {

        ArrayList<Cromossomo> paretoPopCorrente = popCromo.getMelhorPareto();

        //		System.out.println("melhorPareto.size() ==" + melhorPareto.size() );
        //		System.out.println("paretoPopCorrente.size() ==" + paretoPopCorrente.size() );

        int contIndvPareto = 0;
        for (Cromossomo isMelhorPareto : melhorPareto) {

            boolean isDominado = false;

            for (Cromossomo isParetoCorrente : paretoPopCorrente) {

                //Verificando se eh dominado e se eh o mesmo ponto
                if (testeDominancia(isParetoCorrente, isMelhorPareto) ||
                        (isMelhorPareto.getFitness() == isParetoCorrente.getFitness() &&
                                isMelhorPareto.getFitness2() == isParetoCorrente.getFitness2())) {

                    isDominado = true;
                    break;
                }

            }

            //insere o cromossomo guardado caso ele ainda seja pareto
            if (!isDominado) {

                popCromo.getPopCromossomo().add(isMelhorPareto);
                contIndvPareto++;

				/*				int vRand = (int)(popCromo.getPopCromossomo().size()*(Math.random()));
				while (popCromo.getPopCromossomo().get(vRand).getIdPareto() < 2){
					vRand = (int)(popCromo.getPopCromossomo().size()*(Math.random()));
				}
				popCromo.getPopCromossomo().remove(vRand);
				popCromo.getPopCromossomo().add(isMelhorPareto);
				contIndvPareto++;
				 */
            }

        }

        return contIndvPareto != melhorPareto.size();

    }

    public static boolean elitismoMelhorPareto_arq(Populacao popCromo,
                                                   ArrayList<Cromossomo> melhorPareto) {

        ArrayList<Cromossomo> paretoPopCorrente = popCromo.getMelhorPareto();

        //		System.out.println("melhorPareto.size() ==" + melhorPareto.size() );
        //		System.out.println("paretoPopCorrente.size() ==" + paretoPopCorrente.size() );

        int contIndvPareto = 0;
        for (int i = 0; i < melhorPareto.size(); i++) {

            Cromossomo isMelhorPareto = melhorPareto.get(i);

            boolean isDominado = false;

            for (Cromossomo isParetoCorrente : paretoPopCorrente) {

                //Verificando se eh dominado e se eh o mesmo ponto
                if (testeDominancia(isParetoCorrente, isMelhorPareto) ||
                        (isMelhorPareto.getFitness() == isParetoCorrente.getFitness() &&
                                isMelhorPareto.getFitness2() == isParetoCorrente.getFitness2())) {

                    isDominado = true;
                    melhorPareto.remove(i);
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

    public static void ajusteCoord(ArrayList<Cromossomo> conjSolPareto, int fatCob, int fatEn) {

        //achando os limites
        double limX = conjSolPareto.get(conjSolPareto.size() - 1).getFitness();
        double limY = conjSolPareto.get(0).getFitness2();

        for (Cromossomo aConjSolPareto : conjSolPareto) {

            double x = aConjSolPareto.getFitness();
            double y = aConjSolPareto.getFitness2();

            aConjSolPareto.setFitness(fatEn * x / limX);
            aConjSolPareto.setFitness2(fatCob * y / limY);

        }

        double desX = conjSolPareto.get(conjSolPareto.size() - 1).getFitness2();
        double desY = conjSolPareto.get(0).getFitness();

        for (Cromossomo aConjSolPareto : conjSolPareto) {

            double x = aConjSolPareto.getFitness();
            double y = aConjSolPareto.getFitness2();

            aConjSolPareto.setFitness(x - desY);
            aConjSolPareto.setFitness2(y - desX);

        }

    }

    public static Cromossomo decSolPareto(ArrayList<Cromossomo> conjSolPareto,
                                          ArrayList<Sensor> popSensores, RedeSensor rede) throws Exception {

        //Ajuste de coordenadas.

        int index;
        //ajusteCoord(conjSolPareto, fatCob, fatEn);

        index = conjSolPareto.size() - 1;

        double fator = 1.0 - rede.getFatorCob(); //% n�o cobertura
        for (int i = 0; i < conjSolPareto.size(); i++) {
            Cromossomo cromoAux = conjSolPareto.get(i);
            int pontosDescobertos = cromoAux.getNaoCobertura();

            if ((double) pontosDescobertos / rede.getNumPontosDemanda() == 0) {
                index = i;
                break;
            } else if ((double) pontosDescobertos / rede.getNumPontosDemanda() <= fator) {
                index = i;
                fator = (double) pontosDescobertos / rede.getNumPontosDemanda();
                break;
            }

            //System.out.println("\n Ponto = " + i);
            //System.out.println("pontosDescobertos = " + pontosDescobertos);

        }

        return conjSolPareto.get(index);

    }

}
