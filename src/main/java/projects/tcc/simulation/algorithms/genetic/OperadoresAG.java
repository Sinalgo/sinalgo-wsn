package projects.tcc.simulation.algorithms.genetic;

public class OperadoresAG {

    public OperadoresAG() {

    }

	/*public Populacao selecaoTorneio (Populacao popCromo, int vTamPop, int vNumBits){

		Populacao popSel = new Populacao(vTamPop, vNumBits);
		ArrayList<Cromossomo> listCromoSel = popCromo.getPopCromossomo();
		int vRand;

		for (int i = 0; i < vTamPop; i++){		
			//Selecionando os Cromossomos para Torneio
			vRand = (int) (Math.round((listCromoSel.size()-1)*(Math.random())));
			Cromossomo indv1 = listCromoSel.get(vRand);

			vRand = (int) (Math.round((listCromoSel.size()-1)*(Math.random())));
			Cromossomo indv2 = listCromoSel.get(vRand);

			if (indv1.getIdPareto() == indv2.getIdPareto()){
				//comparar pelo Crowding-Distance
				if(indv1.getCrowdingDist() > indv2.getCrowdingDist()){
					popSel.addIndiv(new Cromossomo (indv1));
				}
				else{
					popSel.addIndiv(new Cromossomo (indv2));
				}			
			}
			else{
				//comparar pelo Pareto
				if(indv1.getIdPareto() < indv2.getIdPareto()){
					popSel.addIndiv(new Cromossomo (indv1));
				}
				else{
					popSel.addIndiv(new Cromossomo (indv2));
				}
			}
		}	
		return popSel;
	}*/


    public void cruzamento(Populacao popCromo) {
        //popCromo.realizaCasamentoComMelhores();
        popCromo.realizaCasamento();

    }

    public void mutacao(Populacao popCromo) {
        popCromo.realizaMutacao();
    }

}
