package projects.tcc.simulation.algorithms.genetic;

import java.util.ArrayList;

public class Populacao {


    private double mProbCruz;
    private double mProbCruzIndv;
    private int tamPop;
    private int numBits;
    private ArrayList<Cromossomo> popCromossomo;
    private Cromossomo melhorCromo;
    private ArrayList<Cromossomo> melhorPareto;
    private long[] idsVetBits;
    private ArrayList<Pareto> solPareto;
    private int minSensAtiv;

    public Populacao(int vTamPop, int vNumBits, long[] vetIds, double mProbCruz) {

        this.mProbCruz = mProbCruz;
        mProbCruzIndv = 0.7;
        tamPop = vTamPop;
        numBits = vNumBits;
        popCromossomo = new ArrayList<>();
        melhorPareto = new ArrayList<>();
        melhorCromo = new Cromossomo(numBits, idsVetBits);
        solPareto = new ArrayList<>();

        idsVetBits = vetIds;


        minSensAtiv = 0;

    }

    public int getMinSensAtiv() {
        return this.minSensAtiv;
    }

    public void setMelhorCromo() {
        this.melhorCromo = popCromossomo.get(0);
    }

    public void setMelhorCromo(Cromossomo mCromo) {
        this.melhorCromo = mCromo;
    }

    public ArrayList<Cromossomo> getPopCromossomo() {
        return popCromossomo;
    }

    public void setPopCromossomo(ArrayList<Cromossomo> novaPopCromo) {
        popCromossomo = novaPopCromo;
    }

    public void zerarPopCromossomo() {
        popCromossomo.clear();
    }

    public void ordenaF1() {
        //Ordenar as soluções presente no Pareto segundo a fitness 1
        popCromossomo.sort(new ComparatorFitness(1));
        /////////////////////////////////////////////////////////////
    }

    public void ordenaParetos() {

        //Ordenar as soluções de acordo com o Pareto
        popCromossomo.sort(new ComparatorPareto());
        /////////////////////////////////////////////////////////////
    }

    public int getPopSize() {
        return popCromossomo.size();
    }

	/*	public void viabPop (ArrayList<Sensor> popSensores) {

		int ajusteCol = 0;
		for (int i = 0; i < idsVetBits.length; i++){

			//se sensor estiver falho, retirar a coluna da populacao
			if (popSensores.get(idsVetBits[i]).estaFalha()) {

				numBits--;

				ajusteCol = i - ajusteCol;
				for (int j = 0; j < tamPop; j++) {
					System.out.println("numBits = " + numBits);

					popCromossomo.get(j).retirarColuna(ajusteCol);

				}

				ajusteCol++;
			}


		}

		System.out.println("numSensAtual = " + popSensores.size() +
				"\nnumBits = " + numBits);

	}*/

    //Cria a População dos Cromossomos
    public void startPop(double area, double raioSens, double mFatorCobMO) {

        double areaSens = Math.PI * (Math.pow(raioSens, 2.));
        double minSensAtivCalc = (area * mFatorCobMO) / areaSens;

        //correção
        minSensAtivCalc = minSensAtivCalc * Math.PI / 2.;

        this.minSensAtiv = (int) Math.ceil(minSensAtivCalc);


        for (int i = 0; i < tamPop; i++) {

            Cromossomo vCromossomo = new Cromossomo(numBits, idsVetBits);

            vCromossomo.start(minSensAtiv);

            popCromossomo.add(vCromossomo);

        }
    }


    public int tamPopAtual() {
        return popCromossomo.size();
    }

    public int getTamPopOrig() {
        return tamPop;
    }

    public int getNumBits() {
        return numBits;
    }

    public void printPop() {

        for (int i = 0; i < tamPopAtual(); i++) {

            Cromossomo indiv = popCromossomo.get(i);

            indiv.print();
        }

    }

    public void printMelhorIndv() {

        Cromossomo indiv = melhorCromo;

        indiv.print();

    }

    public Cromossomo getMelhorIndv() {

        return melhorCromo;
    }

    public Cromossomo getMelhorIndvRol() {

        double fitness = Double.MAX_VALUE;
        int indMelhor = 0;

        for (int i = 0; i < popCromossomo.size(); i++) {

            if (popCromossomo.get(i).getFitness() < fitness) {

                fitness = popCromossomo.get(i).getFitness();
                indMelhor = i;

            }

        }
        return popCromossomo.get(indMelhor);
    }

	/*
	re-evaluate fitness of the best chromosome since it can 
	have a new coverage, fitness and so.
	 */	
	/*	public static void reestMelhorCromossomo (ArrayList<Sensor> popSensores)
	{

		for (int i=0; i< (int) popSensores.size(); i++)
		{

			if (popSensores.get(i).estaAtivo())	
				popCromossomo.get(0).ativaPosicao(i);		
		}

	}*/


    public void elitismoMelhorCromo() {

        //fazendo elitismo...
        if (melhorCromo.getFitness() != -1 &&
                popCromossomo.get(0).getFitness() > melhorCromo.getFitness()) {

            //retirando o pior indivíduo da população
            popCromossomo.remove(tamPop - 1);
            //inserindo o melhor indivíduo. (Elitismo)
            popCromossomo.add(0, melhorCromo);

            //System.out.println("PASSOU");

        } else
            melhorCromo = popCromossomo.get(0);

    }

    public void setMelhorPareto() {

        this.melhorPareto.clear();

        for (int i = 0; i < popCromossomo.size(); i++) {

            if (popCromossomo.get(i).getIdPareto() == 1) {
                boolean testeInserir = true;
                for (int j = 0; j < melhorPareto.size(); j++) {

                    if (popCromossomo.get(i).getFitness() == melhorPareto.get(j).getFitness() &&
                            popCromossomo.get(i).getFitness2() == melhorPareto.get(j).getFitness2()) {
                        //System.out.println("testando = " + cont);
                        testeInserir = false;
                        break;
                    }

                }

                if (testeInserir) {
                    this.melhorPareto.add(popCromossomo.get(i));
                }

            }

//			else
//			break;

        }
    }

    public ArrayList<Cromossomo> getMelhorPareto() {
        return this.melhorPareto;
    }

    /*
    crossing over
     */
    Cromossomo realizaCrossingOver(Cromossomo pPai, Cromossomo pMae) {

        Cromossomo vFilho = new Cromossomo(pPai.getTamanhoCromossomo(), idsVetBits);
        int[] vBitsPai = pPai.getVetorBits();
        int[] vBitsMae = pMae.getVetorBits();
        int[] vBitsFilho = vFilho.getVetorBits();
        int vNumeroAtivos = 0;


        //testando substituir pPai.getTamanhoCromossomo()/2 por uma posi��o aleat�ria.

        //int vPosicaoAleatoria = (int) (numBits*(Math.random()));


        for (int i = 0; i < pPai.getTamanhoCromossomo() / 2; i++) {
            vBitsFilho[i] = vBitsPai[i];
            if (vBitsFilho[i] == 1)
                vNumeroAtivos++;
        }

        for (int j = pPai.getTamanhoCromossomo() / 2; j < pPai.getTamanhoCromossomo(); j++) {
            vBitsFilho[j] = vBitsMae[j];
            if (vBitsFilho[j] == 1)
                vNumeroAtivos++;
        }

        vFilho.setNaoCobertura(0);
        vFilho.setNumeroAtivos(vNumeroAtivos);
        vFilho.setAvaliarFO(true);
        return vFilho;
    }


    public void realizaCasamentoComMelhores() {

        int vTamanhoPopulacao = popCromossomo.size();
        if (vTamanhoPopulacao < 2) {
            System.out.println("Populacao pequena. Quebre meu galho\n");
            System.exit(1);
        }

        for (int i = 0; i < 10; i += 2) {

            /*matching criteria ... the best ones!*/
            int vPai = i;
            int vMae = i + 1;

            Cromossomo pPai = popCromossomo.get(vPai);
            Cromossomo pMae = popCromossomo.get(vMae);

            popCromossomo.add(realizaCrossingOver(pPai, pMae));
            popCromossomo.add(realizaCrossingOver(pMae, pPai));

        }

    }

    public void realizaCasamentoIndv() {

        int vTamanhoPopulacao = popCromossomo.size();
        if (vTamanhoPopulacao < 2) {
            System.out.println("Populacao pequena. Quebre meu galho\n");
            System.exit(1);
        }


        for (int i = 0; i < vTamanhoPopulacao; i++) {

            double cDado = Math.random();


            if (cDado < mProbCruzIndv) {

                /*matching criteria ... the best ones!*/
                int vRand = (int) (vTamanhoPopulacao * (Math.random()));
                int vPai = i;
                int vMae = vRand;

                Cromossomo pPai = popCromossomo.get(vPai);
                Cromossomo pMae = popCromossomo.get(vMae);

                popCromossomo.add(realizaCrossingOver(pPai, pMae));
                popCromossomo.add(realizaCrossingOver(pMae, pPai));
            }
        }


    }

    public void realizaCasamento() {

        int vTamanhoPopulacao = popCromossomo.size();
        if (vTamanhoPopulacao < 2) {
            System.out.println("Populacao pequena. Quebre meu galho\n");
            System.exit(1);
        }


        //Preparando a População para Cruzamento.

        //Criando uma poupulação paralela.
        ArrayList<Cromossomo> popCromossomoAux = new ArrayList<>();

        for (Cromossomo aPopCromossomo : popCromossomo) {
            popCromossomoAux.add(new Cromossomo(aPopCromossomo));
        }


        //Gerando os casais:
        ArrayList<Cromossomo> filaPai = new ArrayList<>();
        ArrayList<Cromossomo> filaMae = new ArrayList<>();

        //System.out.println("popCromossomoAux.size() = " + popCromossomoAux.size());
        while (popCromossomoAux.size() > 1) {

            int vRand;

            vRand = (int) (popCromossomoAux.size() * (Math.random()));
            filaPai.add(new Cromossomo(popCromossomoAux.get(vRand)));
            popCromossomoAux.remove(vRand);

            vRand = (int) (popCromossomoAux.size() * (Math.random()));
            filaMae.add(new Cromossomo(popCromossomoAux.get(vRand)));
            popCromossomoAux.remove(vRand);


        }

        //Verificando se cada casal irá cruzar.
        for (int i = 0; i < vTamanhoPopulacao / 2; i++) {

            double cDado = Math.random();

            Cromossomo pPai = filaPai.get(i);
            Cromossomo pMae = filaMae.get(i);

            if (cDado < mProbCruz) {

                /*matching criteria ... the best ones!*/

                popCromossomoAux.add(realizaCrossingOver(pPai, pMae));
                popCromossomoAux.add(realizaCrossingOver(pMae, pPai));
            } else {

                popCromossomoAux.add(new Cromossomo(pPai));
                popCromossomoAux.add(new Cromossomo(pMae));

            }
        }
        popCromossomo = popCromossomoAux;
    }


    /*good by bad choromosomes*/
    public void removePioresPopulacao() {


		/*		for (int i=tamPopAtual()-1; i>=0; i--)
		{
			Cromossomo vCromossomo = popCromossomo.get(i);	

			double vFitness = vCromossomo.getFitness();
			if (vFitness > pObjetivoMedio)
			{
				popCromossomo.remove(tamPopAtual()-1);	
			}
		}*/

        for (int i = tamPopAtual(); i > tamPop; i--) {

            popCromossomo.remove(tamPopAtual() - 1);

        }

    }

    /*good by bad choromosomes*/
    public void removePioresPopulacao(float pObjetivoMedio) {


		/*		for (int i=tamPopAtual()-1; i>=0; i--)
		{
			Cromossomo vCromossomo = popCromossomo.get(i);	

			double vFitness = vCromossomo.getFitness();
			if (vFitness > pObjetivoMedio)
			{
				popCromossomo.remove(tamPopAtual()-1);	
			}
		}*/

        for (int i = tamPopAtual(); i > tamPop; i--) {
            popCromossomo.remove(tamPopAtual() - 1);
        }

    }


    public void realizaMutacao() {
        for (int i = 1; i < tamPopAtual(); i++) {
            Cromossomo vCromossomo = popCromossomo.get(i);
            //vCromossomo.mutacaoAleatoria();
            //vCromossomo.mutacaoBB();
            vCromossomo.mutacaoAtivDest(this.minSensAtiv);
        }

    }


    public void setNumBits(int numBits) {
        this.numBits = numBits;
    }


    public ArrayList<Cromossomo> copyMelhorPareto() {

        ArrayList<Cromossomo> copyMelhorPareto = new ArrayList<>();

        for (Cromossomo cromoPareto : melhorPareto) {

            Cromossomo cromoCopy = new Cromossomo(cromoPareto.getTamanhoCromossomo(), idsVetBits);

            //copiando os campos
            cromoCopy.setFitness(cromoPareto.getFitness());
            cromoCopy.setFitness2(cromoPareto.getFitness2());
            cromoCopy.setIdPareto(cromoPareto.getIdPareto());
            cromoCopy.setNumeroAtivos(cromoPareto.getNumeroAtivos());
            cromoCopy.setPresPareto(cromoPareto.getPresPareto());
            cromoCopy.setNaoCobertura(cromoPareto.getNaoCobertura());
            cromoCopy.setFitnessMO(cromoPareto.getFitnessMO());

            //copiando o vetor de bits
            int[] vetBitsCromoPareto = cromoPareto.getVetorBits();
            int[] vetBitsCopy = new int[vetBitsCromoPareto.length];

            for (int k = 0; k < vetBitsCromoPareto.length; k++) {
                vetBitsCopy[k] = vetBitsCromoPareto[k];
            }

            cromoCopy.setVetorBits(vetBitsCopy);

            copyMelhorPareto.add(cromoCopy);

        }

        return copyMelhorPareto;

    }

    public void addIndiv(Cromossomo indv) {

        Cromossomo cromoClone = new Cromossomo(indv);
        popCromossomo.add(cromoClone);

    }

    public int[] getCopyVetBitsMelhorCromo() {

        int[] copy = new int[numBits];

        for (int i = 0; i < numBits; i++)
            copy[i] = melhorCromo.getVetorBits()[i];

        return copy;

    }

    public void fastNonDominatedSort() {

        //Conjunto das solu��es ordenadas por pareto
        Pareto paretoAux = new Pareto();
        ArrayList<Cromossomo> sp;

        this.solPareto.clear();
        for (int i = 0; i < popCromossomo.size(); i++) {

            // np <- numero pontos que o dominam
            popCromossomo.get(i).setNp(0);
            int contNp = 0;
            // sp <- lista dos pontos dominados
            sp = popCromossomo.get(i).getSp();
            sp.clear();


            for (int j = 0; j < popCromossomo.size(); j++) {
                if (i != j) {
                    if (testeDominancia(popCromossomo.get(i), popCromossomo.get(j))) {
                        sp.add(popCromossomo.get(j));
                    } else if (testeDominancia(popCromossomo.get(j), popCromossomo.get(i))) {
                        contNp++;
                    }
                }
            }

            if (contNp == 0) {
                popCromossomo.get(i).setNp(contNp);
                popCromossomo.get(i).setIdPareto(1);
                paretoAux.inserirSolPareto(popCromossomo.get(i));
            }

            popCromossomo.get(i).setNp(contNp);


        }
        //Adicionando o primeiro pareto -> np = 0
        this.solPareto.add(paretoAux);

        int k = 0; //contagem do pareto


        while (paretoAux.getNumIndv() != 0) {

            paretoAux = new Pareto();

            for (int i = 0; i < this.solPareto.get(k).getNumIndv(); i++) {

                sp = solPareto.get(k).getPopCromoPareto().get(i).getSp();

                for (int j = 0; j < sp.size(); j++) {

                    int npAtual = sp.get(j).getNp();
                    sp.get(j).setNp(npAtual - 1);

                    if (sp.get(j).getNp() == 0) {
                        paretoAux.inserirSolPareto(sp.get(j));
                        sp.get(j).setIdPareto(k + 2);
                    }

                }
            }

            k++;
            if (paretoAux.getNumIndv() != 0) {
                this.solPareto.add(paretoAux);
            }

        }

    }

    public void fastNonDominatedSortNSGA() {

        int tampop = popCromossomo.size();

        //Conjunto das solu��es ordenadas por pareto
        Pareto paretoAux = new Pareto();
        ArrayList<Cromossomo> sp;

        this.solPareto.clear();
        int cont = 0;
        for (int i = 0; i < popCromossomo.size(); i++) {

            // np <- numero pontos que o dominam
            popCromossomo.get(i).setNp(0);
            int contNp = 0;
            // sp <- lista dos pontos dominados
            sp = popCromossomo.get(i).getSp();
            sp.clear();


            for (int j = 0; j < popCromossomo.size(); j++) {
                if (i != j) {
                    if (testeDominancia(popCromossomo.get(i), popCromossomo.get(j))) {
                        sp.add(popCromossomo.get(j));
                    } else if (testeDominancia(popCromossomo.get(j), popCromossomo.get(i))) {
                        contNp++;
                    }
                }
            }

            if (contNp == 0) {
                popCromossomo.get(i).setNp(contNp);
                popCromossomo.get(i).setIdPareto(1);
                cont++;
                paretoAux.inserirSolPareto(popCromossomo.get(i));
            }

            popCromossomo.get(i).setNp(contNp);


        }
        //Adicionando o primeiro pareto -> np = 0
        this.solPareto.add(paretoAux);

        int k = 0; //contagem do pareto


        while (paretoAux.getNumIndv() != 0) {

            paretoAux = new Pareto();

            for (int i = 0; i < this.solPareto.get(k).getNumIndv(); i++) {

                sp = solPareto.get(k).getPopCromoPareto().get(i).getSp();

                for (Cromossomo aSp : sp) {

                    int npAtual = aSp.getNp();
                    aSp.setNp(npAtual - 1);

                    if (aSp.getNp() == 0) {
                        paretoAux.inserirSolPareto(aSp);
                        aSp.setIdPareto(k + 2);
                        cont++;
                    }

                }
            }

            k++;
            if (paretoAux.getNumIndv() != 0) {
                this.solPareto.add(paretoAux);
            } else
                System.out.print("");

        }
        //System.out.println("Numero de Individuos Antes = " + popCromossomo.size());
        //System.out.println("cont = " + cont);

        if (cont < tampop) {
            System.out.println("cont = " + cont);
        }

        popCromossomo.clear();
        for (Pareto aSolPareto : solPareto) {
            for (int j = 0; j < aSolPareto.getNumIndv(); j++) {
                ArrayList<Cromossomo> cromoPareto = aSolPareto.getPopCromoPareto();
                popCromossomo.add(cromoPareto.get(j));
            }
        }
        if (popCromossomo.size() < tampop) {
            System.out.println("Numero de Individuos = " + popCromossomo.size());
            System.out.println("ERRO 1.2 - N�o selecionou todos os indiv�duos");
            System.exit(0);
        }


    }

    //Testando se o CromoB � dominado pelo CromoA
    public boolean testeDominancia(Cromossomo cromoA, Cromossomo cromoB) {

        double fitnA1 = cromoA.getFitness();
        double fitnA2 = cromoA.getFitness2();

        double fitnB1 = cromoB.getFitness();
        double fitnB2 = cromoB.getFitness2();

        if (fitnB1 > fitnA1 && fitnB2 > fitnA2)
            return true;
        else if (fitnB1 == fitnA1 && fitnB2 > fitnA2)
            return true;
        else if (fitnB1 > fitnA1 && fitnB2 == fitnA2)
            return true;
        else
            return false;


    }

    public void doCrowdingDistance() {
        for (Pareto aSolPareto : solPareto) {
            aSolPareto.crowdingDistance();
        }
    }

    public void doCrowdingDistanceRoleta() {
        for (Pareto aSolPareto : solPareto) {
            aSolPareto.crowdingDistanceRoleta();
        }
    }


    public void calcularFO_MO_1() {
        //mFitnessMO = 1/numPareto*(1/(Math.pow(2., (1/(0.8*numPareto)))))

        for (Cromossomo aPopCromossomo : popCromossomo) {
            int idPareto = aPopCromossomo.getIdPareto();
            double convIdPareto = (double) idPareto;
            double fitnessMO = 1 / convIdPareto * (1 / (Math.pow(2., (1 / (0.8 * convIdPareto)))));
            aPopCromossomo.setFitnessMO(fitnessMO);
        }

    }

    public void calcularFO_MO_2() {
        //mFitnessMO = 1/numPareto;

        for (Cromossomo aPopCromossomo : popCromossomo) {
            int idPareto = aPopCromossomo.getIdPareto();
            double convIdPareto = (double) idPareto;
            double fitnessMO = 1 / convIdPareto;
            aPopCromossomo.setFitnessMO(fitnessMO);
        }

    }

    public void calcularFO_MO_3() {
        //mFitnessMO = numPareto;

        //Ordenar o Pareto vigente pelo Crowding-distance
        popCromossomo.sort(new ComparatorPareto());
        ////////////////////////////////////////////////

        int numDeParetos = popCromossomo.get(popCromossomo.size() - 1).getIdPareto();

        for (Cromossomo aPopCromossomo : popCromossomo) {
            int idPareto = aPopCromossomo.getIdPareto();
            aPopCromossomo.setFitnessMO((numDeParetos - idPareto + 1) / numDeParetos);
        }

    }

    public void crowdingDistanceRoleta_MO() {

        double INF = 1.7976931348623157e+308;
        double iDist, iDistAtual, fmax = 0, fmin = 0, fauxAnt = 0, fauxPos = 0;
        int numFOs = 2;

        int numIndv = popCromossomo.size();

        for (Cromossomo aPopCromossomo : popCromossomo) {
            aPopCromossomo.setCrowdingDist(0);
        }

        popCromossomo.get(0).setExtremoPareto(true);
        popCromossomo.get(numIndv - 1).setExtremoPareto(true);

        popCromossomo.get(0).setCrowdingDist(INF);
        popCromossomo.get(numIndv - 1).setCrowdingDist(INF);

        for (int k = 0; k < numFOs; k++) {

            //Ordenar as solu��es presente no Pareto segundo a fitness k+1
            popCromossomo.sort(new ComparatorFitness(k + 1));
            /////////////////////////////////////////////////////////////

            //Calculos para F1
            if (k == 0) {
                fmin = popCromossomo.get(0).getFitness();
                fmax = popCromossomo.get(numIndv - 1).getFitness();
            } else if (k == 1) {
                fmin = popCromossomo.get(0).getFitness2();
                fmax = popCromossomo.get(numIndv - 1).getFitness2();
            }


            for (int i = 1; i < numIndv - 1; i++) {

                if (k == 0) {
                    fauxAnt = popCromossomo.get(i - 1).getFitness();
                    fauxPos = popCromossomo.get(i + 1).getFitness();
                } else if (k == 1) {
                    fauxAnt = popCromossomo.get(i - 1).getFitness2();
                    fauxPos = popCromossomo.get(i + 1).getFitness2();
                }

                iDist = (fauxPos - fauxAnt) / (fmax - fmin);
                iDistAtual = popCromossomo.get(i).getCrowdingDist();

                popCromossomo.get(i).setCrowdingDist(iDist + iDistAtual);

            }

        }

    }

    public void addPopulacao(Populacao pop) {

        ArrayList<Cromossomo> listCromosAdd = pop.getPopCromossomo();

        for (Cromossomo aListCromosAdd : listCromosAdd) {
            addIndiv(aListCromosAdd);
        }
    }


    public ArrayList<Pareto> getSolPareto() {
        return solPareto;
    }


    public void setSolPareto(ArrayList<Pareto> solPareto) {
        this.solPareto = solPareto;
    }


    public void inserirPopArq(ArrayList<Cromossomo> popArq) {
        for (Cromossomo aPopArq : popArq) {
            popCromossomo.add(new Cromossomo(aPopArq));
        }

    }


    public void incrementarValorPareto() {
        /* Ajustando o valor dos paretos para priorizar o pareto arquivo */
        for (Cromossomo aPopCromossomo : popCromossomo) {
            int idPareto = aPopCromossomo.getIdPareto();
            idPareto++;
            aPopCromossomo.setIdPareto(idPareto);
        }
    }

    public void ajustarValorPareto() {
        /* Ajustando o valor dos paretos para priorizar o pareto arquivo */
        for (Cromossomo aPopCromossomo : popCromossomo) {
            if (aPopCromossomo.getIdPareto() > 1) {
                int idPareto = aPopCromossomo.getIdPareto();
                idPareto--;
                aPopCromossomo.setIdPareto(idPareto);
            }
            if (aPopCromossomo.getIdPareto() < 1) {
                System.out.println("ERRO: valor de idPareto < 1");
                System.exit(0);
            }

        }
    }


}
