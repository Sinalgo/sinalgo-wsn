package projects.wsn.simulation.algorithms.genetic;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static projects.wsn.simulation.algorithms.genetic.FitnessType.TYPE_1;
import static projects.wsn.simulation.algorithms.genetic.FitnessType.TYPE_2;

@Setter
public class Cromossomo {

    private int tamanhocromossomo;

    @Getter
    private boolean[] vetorBits;
    @Getter
    private int numeroAtivos;
    private double fitness;
    private double fitness2;
    @Getter(AccessLevel.PRIVATE)
    private double crowdingDist;
    @Getter
    private double fitnessMO;
    private double naoCobertura;
    private double mProbMutAle;
    @Getter
    private int idPareto;
    @Getter
    private boolean presPareto;
    @Getter(AccessLevel.PRIVATE)
    private int np;
    @Getter(AccessLevel.PRIVATE)
    private List<Cromossomo> sp;
    @Getter(AccessLevel.PRIVATE)
    private boolean extremoPareto;
    @Getter(AccessLevel.PRIVATE)
    private int[] vetIds;
    @Getter
    private boolean avaliarFO;

    public Cromossomo(int tamanhoCromossomo, int[] vetIds) {
        this(tamanhoCromossomo, vetIds, new boolean[tamanhoCromossomo]);
    }

    private Cromossomo(int tamanhoCromossomo, int[] vetIds, boolean[] vetorBits) {
        this.tamanhocromossomo = tamanhoCromossomo;
        this.vetorBits = vetorBits;
        this.numeroAtivos = 0;
        this.fitness = -1;
        this.fitness2 = -1;
        this.crowdingDist = -1;
        this.naoCobertura = -1;
        this.mProbMutAle = 0.2;
        this.idPareto = Integer.MAX_VALUE;
        this.presPareto = false;
        this.np = -1;
        this.sp = new ArrayList<>();
        this.extremoPareto = false;
        this.avaliarFO = true;
        this.vetIds = vetIds;
    }

    public Cromossomo(Cromossomo cromo) {
        this(cromo.getTamanhoCromossomo(), cromo.getVetIds(),
                Arrays.copyOf(cromo.getVetorBits(), cromo.getVetorBits().length));
        //copiando o vetor de vetorBits
        this.numeroAtivos = cromo.getNumeroAtivos();
        this.fitness = cromo.getFitness(TYPE_1);
        this.fitness2 = cromo.getFitness(TYPE_2);
        this.crowdingDist = cromo.getCrowdingDist();
        this.naoCobertura = cromo.getNaoCobertura();
        this.idPareto = cromo.getIdPareto();
        this.presPareto = cromo.isPresPareto();
        this.np = cromo.getNp();
        this.sp = new ArrayList<>(cromo.getSp());
        this.extremoPareto = cromo.isExtremoPareto();
        this.avaliarFO = true;
    }

    public List<Integer> getListIdsAtivo() {
        List<Integer> listIdsAtivo = new ArrayList<>();
        for (int i = 0; i < this.vetorBits.length; i++) {
            if (this.vetorBits[i]) {
                listIdsAtivo.add(this.vetIds[i]);
            }
        }
        return listIdsAtivo;
    }

    public int getNaoCobertura() {
        return (int) this.naoCobertura;
    }

    public void setFitness(double pPenalidadeAtivacao, double vCustoCaminho, int pPenalidadeNaoCobertura) {
        this.fitness = pPenalidadeAtivacao * this.numeroAtivos + pPenalidadeNaoCobertura * this.naoCobertura + vCustoCaminho;
    }

    public void setFitness2(int penNCob, double penAtiv) {
        this.fitness2 = penNCob * this.naoCobertura + penAtiv * this.numeroAtivos; //Usada!!!
    }

    public int getTamanhoCromossomo() {
        return this.tamanhocromossomo;
    }

    /*
    start chromossome by uniform distribution
     */
    public void start(int numAtiv) {
        for (int i = 0; i < this.tamanhocromossomo; i++) {
            this.vetorBits[i] = false;
        }
        for (int i = 0; i < numAtiv; i++) {
            int vRand = (int) Math.abs(Math.random() * this.tamanhocromossomo);
            if (!this.vetorBits[vRand]) {
                this.numeroAtivos++;
            }
            this.vetorBits[vRand] = true;
        }
    }

    public void mutacaoAtivDest(int minSensAtiv) {
        double vRand = Math.random();
        if (vRand < this.mProbMutAle) {
            this.calculaNumAtivos();
            if (this.numeroAtivos == minSensAtiv) {
                this.mutacaoPerm();
            } else if (this.numeroAtivos > minSensAtiv) {
                //desativa um nó.
                List<Integer> listInd = new ArrayList<>();
                for (int i = 0; i < this.tamanhocromossomo; i++) {
                    if (this.vetorBits[i]) {
                        listInd.add(i);
                    }
                }
                int vPosicaoAleatoria = this.getIntRandom(listInd.size());
                int ind = listInd.get(vPosicaoAleatoria);
                this.vetorBits[ind] = false;
            } else {
                //Ativa um nó
                List<Integer> listInd = new ArrayList<>();
                for (int i = 0; i < this.tamanhocromossomo; i++) {
                    if (!this.vetorBits[i]) {
                        listInd.add(i);
                    }
                }
                int vPosicaoAleatoria = this.getIntRandom(listInd.size());
                int ind = listInd.get(vPosicaoAleatoria);
                this.vetorBits[ind] = true;
            }
            // foi alterado
            this.avaliarFO = true;
        }
    }

    private void calculaNumAtivos() {
        int cont = 0;
        for (int i = 0; i < this.tamanhocromossomo; i++) {
            if (this.vetorBits[i]) {
                cont++;
            }
        }
        this.setNumeroAtivos(cont);
    }

    private void mutacaoPerm() {
        double vRand = Math.random();
        if (vRand < this.mProbMutAle) {
            int vPosicaoAleatoria = this.getIntRandom(this.tamanhocromossomo - 1);
            if (this.vetorBits[vPosicaoAleatoria]) {
                List<Integer> listDest = new ArrayList<>();
                for (int i = 0; i < this.tamanhocromossomo; i++) {
                    if (!this.vetorBits[i]) {
                        listDest.add(i);
                    }
                }
                if (listDest.size() > 0) {
                    int vPosicaoAleatoria2 = this.getIntRandom(listDest.size());
                    while (listDest.get(vPosicaoAleatoria2) == vPosicaoAleatoria) {
                        vPosicaoAleatoria2 = this.getIntRandom(listDest.size());
                    }
                    this.vetorBits[listDest.get(vPosicaoAleatoria2)] = true;
                }
                this.vetorBits[vPosicaoAleatoria] = false;
            } else {
                List<Integer> listAtiv = new ArrayList<>();
                for (int i = 0; i < this.tamanhocromossomo; i++) {
                    if (this.vetorBits[i]) {
                        listAtiv.add(i);
                    }
                }
                if (listAtiv.size() > 0) {
                    int vPosicaoAleatoria2 = this.getIntRandom(listAtiv.size());
                    while (listAtiv.get(vPosicaoAleatoria2) == vPosicaoAleatoria) {
                        vPosicaoAleatoria2 = this.getIntRandom(listAtiv.size());
                    }
                    this.vetorBits[listAtiv.get(vPosicaoAleatoria2)] = false;
                }
                this.vetorBits[vPosicaoAleatoria] = true;
            }
            // foi alterado
            this.avaliarFO = true;
        }

    }

    public double getFitness(FitnessType fitnessType) {
        switch (fitnessType) {
            case TYPE_1:
                return this.fitness;
            case TYPE_2:
                return this.fitness2;
            default:
                throw new RuntimeException("Invalid fitness type selected");
        }
    }

    /*
    random between 0 and pTamanhoMaximo
     */
    private int getIntRandom(int pTamanhoMaximo) {
        return (int) (pTamanhoMaximo * (Math.random()));
    }

}