package projects.tcc.simulation.algorithms.genetic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static projects.tcc.simulation.algorithms.genetic.FitnessType.TYPE_1;
import static projects.tcc.simulation.algorithms.genetic.FitnessType.TYPE_2;


public class Populacao {

    private double mProbCruz;
    private int tamPopOrig;
    private int numBits;
    private List<Cromossomo> popCromossomo;
    private Cromossomo melhorCromo;
    private List<Cromossomo> melhorPareto;
    private int[] idsVetBits;
    private int minSensAtiv;

    public Populacao(int vTamPop, int vNumBits, int[] vetIds, double mProbCruz) {
        this.mProbCruz = mProbCruz;
        this.tamPopOrig = vTamPop;
        this.numBits = vNumBits;
        this.idsVetBits = vetIds;
        this.popCromossomo = new ArrayList<>();
        this.melhorPareto = new ArrayList<>();
        this.melhorCromo = new Cromossomo(this.numBits, this.idsVetBits);
        this.minSensAtiv = 0;
    }

    public void setMelhorCromo(Cromossomo mCromo) {
        this.melhorCromo = mCromo;
    }

    public List<Cromossomo> getPopCromossomo() {
        return this.popCromossomo;
    }

    public void setPopCromossomo(List<Cromossomo> novaPopCromo) {
        this.popCromossomo = novaPopCromo;
    }

    public void ordenaF1() {
        //Ordenar as soluções presente no Pareto segundo a fitness 1
        this.popCromossomo.sort(new ComparatorFitness(TYPE_1));
        /////////////////////////////////////////////////////////////
    }

    //Cria a População dos Cromossomos
    public void startPop(double area, double raioSens, double mFatorCobMO) {
        double areaSens = Math.PI * Math.pow(raioSens, 2);
        double minSensAtivCalc = (area * mFatorCobMO * Math.PI) / (areaSens * 2);
        this.minSensAtiv = (int) Math.ceil(minSensAtivCalc);
        for (int i = 0; i < this.tamPopOrig; i++) {
            Cromossomo vCromossomo = new Cromossomo(this.numBits, this.idsVetBits);
            vCromossomo.start(this.minSensAtiv);
            this.popCromossomo.add(vCromossomo);
        }
    }

    private int tamPopAtual() {
        return this.popCromossomo.size();
    }

    public int getTamPopOrig() {
        return this.tamPopOrig;
    }

    public Cromossomo getMelhorCromo() {
        return this.melhorCromo;
    }

    public void setMelhorPareto() {
        this.melhorPareto.clear();
        for (Cromossomo aPopCromossomo : this.popCromossomo) {
            if (aPopCromossomo.getIdPareto() == 1) {
                boolean testeInserir = true;
                for (Cromossomo aMelhorPareto : this.melhorPareto) {
                    if (aPopCromossomo.getFitness(TYPE_1) == aMelhorPareto.getFitness(TYPE_1) &&
                            aPopCromossomo.getFitness(TYPE_2) == aMelhorPareto.getFitness(TYPE_2)) {
                        testeInserir = false;
                        break;
                    }
                }
                if (testeInserir) {
                    this.melhorPareto.add(aPopCromossomo);
                }
            }
        }
    }

    public List<Cromossomo> getMelhorPareto() {
        return this.melhorPareto;
    }

    private Cromossomo realizaCrossingOver(Cromossomo pPai, Cromossomo pMae) {
        Cromossomo vFilho = new Cromossomo(pPai.getTamanhoCromossomo(), this.idsVetBits);
        boolean[] vBitsPai = pPai.getVetorBits();
        boolean[] vBitsMae = pMae.getVetorBits();
        boolean[] vBitsFilho = vFilho.getVetorBits();
        int vNumeroAtivos = 0;
        for (int i = 0; i < pPai.getTamanhoCromossomo() / 2; i++) {
            vBitsFilho[i] = vBitsPai[i];
            if (vBitsFilho[i]) {
                vNumeroAtivos++;
            }
        }
        for (int i = pPai.getTamanhoCromossomo() / 2; i < pPai.getTamanhoCromossomo(); i++) {
            vBitsFilho[i] = vBitsMae[i];
            if (vBitsFilho[i]) {
                vNumeroAtivos++;
            }
        }
        vFilho.setNaoCobertura(0);
        vFilho.setNumeroAtivos(vNumeroAtivos);
        vFilho.setAvaliarFO(true);
        return vFilho;
    }

    public void realizaCasamento() {
        int vTamanhoPopulacao = this.popCromossomo.size();
        if (vTamanhoPopulacao < 2) {
            System.out.println("Populacao pequena. Quebre meu galho\n");
            System.exit(1);
        }

        //Preparando a População para Cruzamento.
        //Criando uma poupulação paralela.
        List<Cromossomo> popCromossomoAux = new ArrayList<>();
        for (Cromossomo aPopCromossomo : this.popCromossomo) {
            popCromossomoAux.add(new Cromossomo(aPopCromossomo));
        }

        //Gerando os casais:
        List<Cromossomo> filaPai = new ArrayList<>();
        List<Cromossomo> filaMae = new ArrayList<>();

        while (popCromossomoAux.size() > 1) {
            int vRandPai = (int) (popCromossomoAux.size() * (Math.random()));
            filaPai.add(new Cromossomo(popCromossomoAux.get(vRandPai)));
            popCromossomoAux.remove(vRandPai);

            int vRandMae = (int) (popCromossomoAux.size() * (Math.random()));
            filaMae.add(new Cromossomo(popCromossomoAux.get(vRandMae)));
            popCromossomoAux.remove(vRandMae);
        }

        //Verificando se cada casal irá cruzar.
        for (int i = 0; i < vTamanhoPopulacao / 2; i++) {
            double cDado = Math.random();
            Cromossomo pPai = filaPai.get(i);
            Cromossomo pMae = filaMae.get(i);
            if (cDado < this.mProbCruz) {
                /*matching criteria ... the best ones!*/
                popCromossomoAux.add(this.realizaCrossingOver(pPai, pMae));
                popCromossomoAux.add(this.realizaCrossingOver(pMae, pPai));
            } else {
                popCromossomoAux.add(new Cromossomo(pPai));
                popCromossomoAux.add(new Cromossomo(pMae));
            }
        }
        this.popCromossomo = popCromossomoAux;
    }

    public void realizaMutacao() {
        for (int i = 1; i < this.tamPopAtual(); i++) {
            this.popCromossomo.get(i).mutacaoAtivDest(this.minSensAtiv);
        }
    }

    public List<Cromossomo> copyMelhorPareto() {
        List<Cromossomo> copyMelhorPareto = new ArrayList<>();
        for (Cromossomo cromoPareto : this.melhorPareto) {
            Cromossomo cromoCopy = new Cromossomo(cromoPareto.getTamanhoCromossomo(), this.idsVetBits);
            //copiando os campos
            cromoCopy.setFitness(cromoPareto.getFitness(TYPE_1));
            cromoCopy.setFitness2(cromoPareto.getFitness(TYPE_2));
            cromoCopy.setIdPareto(cromoPareto.getIdPareto());
            cromoCopy.setNumeroAtivos(cromoPareto.getNumeroAtivos());
            cromoCopy.setPresPareto(cromoPareto.isPresPareto());
            cromoCopy.setNaoCobertura(cromoPareto.getNaoCobertura());
            cromoCopy.setFitnessMO(cromoPareto.getFitnessMO());

            //copiando o vetor de vetorBits
            cromoCopy.setVetorBits(Arrays.copyOf(cromoPareto.getVetorBits(), cromoPareto.getVetorBits().length));
            copyMelhorPareto.add(cromoCopy);
        }
        return copyMelhorPareto;
    }

    public void calcularFO_MO_1() {
        for (Cromossomo aPopCromossomo : this.popCromossomo) {
            int idPareto = aPopCromossomo.getIdPareto();
            double convIdPareto = (double) idPareto;
            double fitnessMO = 1 / convIdPareto * (1 / (Math.pow(2., (1 / (0.8 * convIdPareto)))));
            aPopCromossomo.setFitnessMO(fitnessMO);
        }
    }

    public void calcularFO_MO_3() {
        this.popCromossomo.sort(new ComparatorPareto());
        ////////////////////////////////////////////////
        int numDeParetos = this.popCromossomo.get(this.popCromossomo.size() - 1).getIdPareto();
        for (Cromossomo aPopCromossomo : this.popCromossomo) {
            int idPareto = aPopCromossomo.getIdPareto();
            aPopCromossomo.setFitnessMO((double) (numDeParetos - idPareto + 1) / numDeParetos);
        }
    }

    public void inserirPopArq(List<Cromossomo> popArq) {
        for (Cromossomo aPopArq : popArq) {
            this.popCromossomo.add(new Cromossomo(aPopArq));
        }
    }

    public void incrementarValorPareto() {
        /* Ajustando o valor dos paretos para priorizar o pareto arquivo */
        for (Cromossomo aPopCromossomo : this.popCromossomo) {
            aPopCromossomo.setIdPareto(aPopCromossomo.getIdPareto() + 1);
        }
    }

    public void ajustarValorPareto() {
        /* Ajustando o valor dos paretos para priorizar o pareto arquivo */
        for (Cromossomo aPopCromossomo : this.popCromossomo) {
            if (aPopCromossomo.getIdPareto() > 1) {
                aPopCromossomo.setIdPareto(aPopCromossomo.getIdPareto() - 1);
            }
            if (aPopCromossomo.getIdPareto() < 1) {
                System.out.println("ERRO: valor de idPareto < 1");
                System.exit(0);
            }
        }
    }

}
