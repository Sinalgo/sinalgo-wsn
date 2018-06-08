package projects.tcc.simulation.algorithms.genetic;

import java.util.ArrayList;

public class Cromossomo {

    private int mTamanhoCromossomo;
    private int mBits[];
    private int mNumeroAtivos;
    private double mFitness;
    private double mFitness2;
    private double crowdingDist;
    private double mFitnessMO;
    private double mNaoCobertura;
    private double mProbMutAle;
    private double mProbMutBB;
    private int idPareto;
    private boolean presPareto;
    private int np;
    private ArrayList<Cromossomo> sp;
    private boolean extremoPareto;
    private int[] vetIds;
    private boolean avaliarFO;


    public Cromossomo(int pTamanhoCromossomo, int[] vetIds) {
        mTamanhoCromossomo = pTamanhoCromossomo;
        mBits = new int[mTamanhoCromossomo];
        mNumeroAtivos = 0;
        mFitness = -1;
        mFitness2 = -1;
        crowdingDist = -1;
        mNaoCobertura = -1;
        mProbMutAle = 0.2;
        mProbMutBB = 0.025;//0.025;
        idPareto = Integer.MAX_VALUE;
        presPareto = false;
        np = -1;
        sp = new ArrayList<>();
        extremoPareto = false;
        avaliarFO = true;
        this.vetIds = vetIds;
    }

    public Cromossomo(Cromossomo cromo) {
        this(cromo.getTamanhoCromossomo(), cromo.getVetIds());
        //copiando o vetor de bits
        int[] vetBitsCromo = cromo.getVetorBits();

        for (int k = 0; k < vetBitsCromo.length; k++) {
            mBits[k] = vetBitsCromo[k];
        }
        mNumeroAtivos = cromo.getNumeroAtivos();
        mFitness = cromo.getFitness();
        mFitness2 = cromo.getFitness2();
        crowdingDist = cromo.getCrowdingDist();
        mNaoCobertura = cromo.getNaoCobertura();
        idPareto = cromo.getIdPareto();
        presPareto = cromo.getPresPareto();
        np = cromo.getNp();

        //copiar a lista sp
        sp = new ArrayList<>();
        for (int i = 0; i < cromo.getSp().size(); i++) {
            sp.add(cromo.getSp().get(i));
        }
        extremoPareto = cromo.getExtremoPareto();
        avaliarFO = true;

    }


    public ArrayList<Integer> getListIdsAtivo() {

        ArrayList<Integer> listIdsAtivo = new ArrayList<>();

        for (int i = 0; i < mBits.length; i++) {
            if (mBits[i] == 1) {
                listIdsAtivo.add(vetIds[i]);
            }
        }

        return listIdsAtivo;
    }


    public int[] getVetIds() {
        return vetIds;
    }

    public boolean isAvaliarFO() {
        return avaliarFO;
    }

    public void setAvaliarFO(boolean avaliarFO) {
        this.avaliarFO = avaliarFO;
    }

    public boolean getExtremoPareto() {
        return extremoPareto;
    }

    public void setExtremoPareto(boolean extremoPareto) {
        this.extremoPareto = extremoPareto;
    }

    public boolean getPresPareto() {
        return presPareto;
    }

    public void setPresPareto(boolean estado) {
        this.presPareto = estado;
    }

    public int getIdPareto() {
        return idPareto;
    }

    public void setIdPareto(int idPareto) {
        this.idPareto = idPareto;
    }

    public int getNumeroAtivos() {
        return mNumeroAtivos;
    }

    public int[] getVetorBits() {
        return mBits;
    }

    public boolean[] getVetorBoolean() {
        boolean[] vetSensAtiv = new boolean[mBits.length];

        for (int i = 0; i < mBits.length; i++) {
            if (mBits[i] == 1)
                vetSensAtiv[i] = true;
            else
                vetSensAtiv[i] = false;
        }
        return vetSensAtiv;
    }

    public void setVetorBits(int[] Bits) {
        this.mBits = Bits;
    }

    public int getNaoCobertura() {
        return (int) mNaoCobertura;
    }

    public double getFitness() {
        return mFitness;
    }

    public double getFitness2() {
        return mFitness2;
    }

    public void setNaoCobertura(int pNaoCobertura) {
        mNaoCobertura = pNaoCobertura;
    }

    public void setFitness() {
        mFitness = mNumeroAtivos + mNaoCobertura;
    }

    public void setFitness(double pPenalidadeAtivacao, double vCustoCaminho, int pPenalidadeNaoCobertura) {
//		System.out.println("pPenalidadeAtivacao*mNumeroAtivos = " + pPenalidadeAtivacao*mNumeroAtivos);
//		System.out.println("vCustoCaminho = " + vCustoCaminho);
//		System.out.println("pPenalidadeNaoCobertura*mNaoCobertura = " + pPenalidadeNaoCobertura*mNaoCobertura);
        mFitness = pPenalidadeAtivacao * mNumeroAtivos + pPenalidadeNaoCobertura * mNaoCobertura + vCustoCaminho;
    }

    public void setFitness(double pPenalidadeAtivacao, double vCustoCaminho, int pPenalidadeNaoCobertura, double enRes) {
        mFitness = (pPenalidadeAtivacao * mNumeroAtivos + vCustoCaminho) / enRes + pPenalidadeNaoCobertura * mNaoCobertura;
    }

    public void setFitness(double pFitness) {
        mFitness = pFitness;
    }

    public void setFitness2(double raioSens, int penNCob, int redCob) {
//		System.out.println("nSensAtivos*(Math.PI*(Math.pow(raioSens, 2))) = " + nSensAtivos*(Math.PI*(Math.pow(raioSens, 2))));
//		System.out.println("penNCob*mNaoCobertura = " + penNCob*mNaoCobertura);
//		System.out.println("mNaoCobertura*redCob = " + (mNaoCobertura*redCob)/1000);
//		mFitness2 = mNumeroAtivos*(Math.PI*(Math.pow(raioSens, 2))) + penNCob*mNaoCobertura + redCob;
        mFitness2 = penNCob * mNaoCobertura + ((mNaoCobertura + 10) * redCob) / 100 + 100 * mNumeroAtivos; //Usada!!!
        //mFitness2 = mFitness2/5000000000.; // ajuste...
    }

    public void setFitness2(double raioSens, int penNCob, double penAtiv) {
//		System.out.println("nSensAtivos*(Math.PI*(Math.pow(raioSens, 2))) = " + nSensAtivos*(Math.PI*(Math.pow(raioSens, 2))));
//		System.out.println("penNCob*mNaoCobertura = " + penNCob*mNaoCobertura);
//		System.out.println("mNaoCobertura*redCob = " + (mNaoCobertura*redCob)/1000);
//		mFitness2 = mNumeroAtivos*(Math.PI*(Math.pow(raioSens, 2))) + penNCob*mNaoCobertura + redCob;
        //System.out.println("penNCob*mNaoCobertura = " + penNCob*mNaoCobertura + "  -  penAtiv*mNumeroAtivos = " + penAtiv*mNumeroAtivos);
        mFitness2 = penNCob * mNaoCobertura + penAtiv * mNumeroAtivos; //Usada!!!
        //mFitness2 = mFitness2/5000000000.; // ajuste...
    }

    public void setFitness2(double pFitness2) {
        mFitness2 = pFitness2;
    }

    public int getTamanhoCromossomo() {
        return mTamanhoCromossomo;
    }

    public void setNumeroAtivos(int pNumeroAtivos) {
        mNumeroAtivos = pNumeroAtivos;
    }

    public double getCrowdingDist() {
        return crowdingDist;
    }

    public void setCrowdingDist(double crowdingDist) {
        this.crowdingDist = crowdingDist;
    }

    public int getNp() {
        return np;
    }

    public void setNp(int np) {
        this.np = np;
    }

    public ArrayList<Cromossomo> getSp() {
        return sp;
    }

    public void setSp(ArrayList<Cromossomo> sp) {
        this.sp = sp;
    }

    /*
    start chromossome by uniform distribution
     */
    public void start(int numAtiv) {
        for (int i = 0; i < mTamanhoCromossomo; i++) {
            mBits[i] = 0;

        }

        for (int i = 0; i < numAtiv; i++) {
            int vRand = (int) Math.abs(Math.random() * mTamanhoCromossomo);

            if (mBits[vRand] == 0) {
                mNumeroAtivos++;
            }
            mBits[vRand] = 1;

        }
    }


    public void mutaBit(int numMut) {

        for (int i = 0; i < numMut; i++) {

            int vRand = (int) Math.abs(Math.random() * mTamanhoCromossomo);

            if (mBits[vRand] == 1) {

                mBits[vRand] = 0;
                mNumeroAtivos--;

            } else {
                mBits[vRand] = 1;
                mNumeroAtivos++;
            }
        }
    }


    /*
    starts chromossome by a distribution that considers the size of area and the medium sensing range
     */
    public void start(int pTamanhoArea, double pRaioSensoriamento, int pTotalSensores) {
        double vNumeroMinimoAtivos = pTamanhoArea / (3.14 * pRaioSensoriamento * pRaioSensoriamento);
        vNumeroMinimoAtivos = vNumeroMinimoAtivos / pTotalSensores;
        double vProbabilidadeEstarAtivo = vNumeroMinimoAtivos;

        for (int i = 0; i < mTamanhoCromossomo; i++) {
            double vRand = Math.random();

            if (vRand < vProbabilidadeEstarAtivo) {
                mBits[i] = 1;
                mNumeroAtivos++;
            } else
                mBits[i] = 0;

        }

    }


    /*
    prints a chromossome - just for debug purposes
     */
    public void print() {
        int cContador;
        for (cContador = 0; cContador < mTamanhoCromossomo; cContador++)
            System.out.print(mBits[cContador]);

        System.out.println("\t" + mFitness);
    }

    public void printCompleto() {
        int cContador;
        for (cContador = 0; cContador < mTamanhoCromossomo; cContador++)
            System.out.print(mBits[cContador]);

        System.out.print("\t" + mNumeroAtivos + "\t" + mNaoCobertura + "\t" + mFitness);

        System.out.println();
    }


    /*
    random mutation
    if 0->1
    if 1->0
     */
    public void mutacaoAleatoria() {

        double vRand = Math.random();

        if (vRand < mProbMutAle) {


            int vPosicaoAleatoria = getIntRandom(mTamanhoCromossomo);


            if (mBits[vPosicaoAleatoria] == 1) {
                mBits[vPosicaoAleatoria] = 0;
                mNumeroAtivos--;
            } else {
                mBits[vPosicaoAleatoria] = 1;
                mNumeroAtivos++;
            }
            // foi alterado
            avaliarFO = true;

        }

    }

    public void mutacaoBB() {

        for (int i = 0; i < mTamanhoCromossomo; i++) {

            double vRand = Math.random();

            if (vRand < mProbMutBB) {

                if (mBits[i] == 1) {
                    mBits[i] = 0;
                    mNumeroAtivos--;
                } else {
                    mBits[i] = 1;
                    mNumeroAtivos++;
                }
                // foi alterado
                avaliarFO = true;

            }

        }

    }

    public void mutacaoAtivDest(int minSensAtiv) {

        double vRand = Math.random();

        if (vRand < mProbMutAle) {

            calculaNumAtivos();


            if (mNumeroAtivos == minSensAtiv) {
                mutacaoPerm();
            } else if (mNumeroAtivos > minSensAtiv) {
                //desativa um n�.
                ArrayList<Integer> listInd = new ArrayList<>();
                for (int i = 0; i < mTamanhoCromossomo; i++) {
                    if (mBits[i] == 1) {
                        listInd.add(i);
                    }
                }

                int vPosicaoAleatoria = getIntRandom(listInd.size());
                int ind = listInd.get(vPosicaoAleatoria);

                mBits[ind] = 0;

            } else {
                //Ativa um n�
                ArrayList<Integer> listInd = new ArrayList<>();
                for (int i = 0; i < mTamanhoCromossomo; i++) {
                    if (mBits[i] == 0) {
                        listInd.add(i);
                    }
                }

                int vPosicaoAleatoria = getIntRandom(listInd.size());
                int ind = listInd.get(vPosicaoAleatoria);

                mBits[ind] = 1;
            }

            // foi alterado
            avaliarFO = true;

        }

    }

    public void calculaNumAtivos() {
        int cont = 0;
        for (int i = 0; i < mTamanhoCromossomo; i++) {
            if (mBits[i] == 1) {
                cont++;
            }
        }
        setNumeroAtivos(cont);
    }

    public void mutacaoPerm() {

        double vRand = Math.random();

        if (vRand < mProbMutAle) {


            int vPosicaoAleatoria = getIntRandom(mTamanhoCromossomo - 1);


            if (mBits[vPosicaoAleatoria] == 1) {
                ArrayList<Integer> listDest = new ArrayList<>();

                for (int i = 0; i < mTamanhoCromossomo; i++) {
                    if (mBits[i] == 0)
                        listDest.add(i);
                }

                if (listDest.size() > 0) {
                    int vPosicaoAleatoria2 = getIntRandom(listDest.size());
                    while (listDest.get(vPosicaoAleatoria2) == vPosicaoAleatoria) {
                        vPosicaoAleatoria2 = getIntRandom(listDest.size());
                    }
                    mBits[listDest.get(vPosicaoAleatoria2)] = 1;
                }

                mBits[vPosicaoAleatoria] = 0;

            } else {
                ArrayList<Integer> listAtiv = new ArrayList<>();

                for (int i = 0; i < mTamanhoCromossomo; i++) {
                    if (mBits[i] == 1)
                        listAtiv.add(i);
                }

                if (listAtiv.size() > 0) {

                    int vPosicaoAleatoria2 = getIntRandom(listAtiv.size());
                    while (listAtiv.get(vPosicaoAleatoria2) == vPosicaoAleatoria) {
                        vPosicaoAleatoria2 = getIntRandom(listAtiv.size());
                    }
                    mBits[listAtiv.get(vPosicaoAleatoria2)] = 0;
                }
                mBits[vPosicaoAleatoria] = 1;


            }
            // foi alterado
            avaliarFO = true;

        }

    }

	/*
	random between 0 and pTamanhoMaximo
	 */

    int getIntRandom(int pTamanhoMaximo) {
        int vRand = (int) (pTamanhoMaximo * (Math.random()));
        return vRand;
    }


    void ativaPosicao(int pPosicao) {
        if (mBits == null)
            return;

        if (mBits[pPosicao] == 0) {
            mBits[pPosicao] = 1;
            mNumeroAtivos++;
        }
    }


    void desativaPosicao(int pPosicao) {
        if (mBits == null)
            return;

        if (mBits[pPosicao] == 1) {
            mBits[pPosicao] = 0;
            mNumeroAtivos--;
        }
    }

	/*
  used for sorting! Do not put your hands here!
	 */
	/*	public int compareTo(Object arg0) {

		Cromossomo vCromossomo = (Cromossomo) arg0;

		if(this.getFitness() < vCromossomo.getFitness())
			return -1;

		if(this.getFitness() == vCromossomo.getFitness())
			return 0;

		return 1; 
	}*/


    public double getFitnessMO() {
        return mFitnessMO;
    }

    public void setFitnessMO(double fitnessMO) {
        this.mFitnessMO = fitnessMO;
    }

}
