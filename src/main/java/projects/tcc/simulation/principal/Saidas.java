package projects.tcc.simulation.principal;

import lombok.extern.java.Log;
import projects.tcc.simulation.data.SensorHolder;
import projects.tcc.simulation.rssf.Simulacao;
import projects.tcc.simulation.rssf.sensor.Sensor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Log
public class Saidas {

    private Simulacao mSimulacao;
    private String pasta;


    public Saidas(Simulacao mSimulacao, String pasta) {
        this.mSimulacao = mSimulacao;
        this.pasta = pasta;
        apagarArqSimulacao();
    }


    private void apagarArqSimulacao() {
        File arquivoSimulacao = new File(pasta + "simulacao");
        try {
            //Apagar um arquivo
            arquivoSimulacao.delete();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void gerarSaidaTela(int periodo) {
        log.info("Tempo = " + periodo);
        log.info("Numero de Sensores Ativos: " + SensorHolder.getActiveSensors().size());
        log.info("Energia Residual: " + mSimulacao.getEnResRede());
        log.info("Energia Consumida: " + mSimulacao.getEnConsRede());
        log.info("Cobertura Atual: " + mSimulacao.getpCobAtual());

    }

    public void geraArquivoSimulador(int estagioAtual) throws IOException {
        String pastaSaida = pasta + "simulacao";


        FileWriter fw = new FileWriter(pastaSaida, true);
        PrintWriter pw = new PrintWriter(fw, true);

        pw.println(estagioAtual);
        int pCob = (int) (mSimulacao.getpCobAtual() * 100);
        pw.println(pCob);

        for (Sensor s : SensorHolder.getAllSensorsAndSinks().values()) {
            int estadoSensor;
            if (s.isActive())
                estadoSensor = 1;
            else
                estadoSensor = 2;

            if (s.isFailed())
                estadoSensor = 3;

            double bateria = s.getBatteryEnergy();

            long pai;
            if (s.isActive() && s.getParent() != null)
                pai = s.getParent().getID();
            else
                pai = -1;

            pw.print(estadoSensor + "\t" + bateria + "\t" + pai);
            pw.println();
        }
        pw.println();

        pw.close();
        fw.close();

    }

    public void gerarArqSimulacao(int numTeste, String nomeAlg) throws IOException {

        List<Double> vpCobertura = mSimulacao.getVpCobertura();
        List<Double> vEnConsRede = mSimulacao.getvEnConsRede();
        List<Double> vEnResRede = mSimulacao.getvEnResRede();

        String nomeArq;

        //Hibrido
        nomeArq = pasta + nomeAlg + numTeste + ".out";

        FileWriter fw = new FileWriter(nomeArq);
        PrintWriter pw = new PrintWriter(fw);

        //Informando quantas linhas de dados irao ter no arquivo
        pw.print(vpCobertura.size() + "\n");

        for (int i = 0; i < vpCobertura.size(); i++) {
            pw.print(vpCobertura.get(i) + "\t" +
                    vEnConsRede.get(i) + "\t" +
                    vEnResRede.get(i) + "\n");

        }

        pw.close();


    }

    public static void geraArqSaidaTempo(String nomeArq, String pasta, double tHibrido) throws IOException {

        String arq = pasta + nomeArq;

        FileWriter fw = new FileWriter(arq);
        PrintWriter pw = new PrintWriter(fw);

        pw.print(tHibrido);

        pw.close();
    }
//	
//	public void gerarArqPeriodos (int periodo) throws IOException {
//			
//		String saidaPer = pasta + "saidaPer";
//		
//		saidaPer = saidaPer + Integer.toString(periodo) + ".out";
//		
//		geraArqSaida(saidaPer);
//		
//		
//	}
//	
//	public void geraArqSaida (String nomeArq) throws IOException {
//
//		int sink = rede.getNumSensInicial();
//		
//		FileWriter fw = new FileWriter(nomeArq);
//		PrintWriter pw = new PrintWriter(fw); 
//
//
//		pw.print(rede.getNumSensAtual()+"\n");
//
//		int sensAtivo, sensFalho; // 0 se falho ou n�o ativo.
//		for (int i=0; i<rede.getNumSensInicial(); i++)
//		{			
//			if (rede.getListSensores().get(i).estaAtivo())
//				sensAtivo = 1;
//			else 
//				sensAtivo = 0;
//
//
//			if (rede.getListSensores().get(i).estaFalha())
//				sensFalho = 1;
//			else
//				sensFalho = 0;
//
//
//			//imprimindo as coordenadas dos sensores
//			pw.print(rede.getListSensores().get(i).getPosicaoX()+ "\t" + 
//					rede.getListSensores().get(i).getPosicaoY() + "\t" +
//					rede.getListSensores().get(i).getParent()+ "\t" +
//					sensAtivo						 + "\t" +
//					sensFalho						 + "\n");
//
//		}
//
//		//acrescentando o sink no arquivo texto.
//		pw.print(rede.getListSensores().get(sink).getPosicaoX()+ "\t" + 
//				rede.getListSensores().get(sink).getPosicaoY() + "\t" +
//				rede.getListSensores().get(sink).getParent()+ "\t" +
//				1									     + "\t" +
//				0									     + "\n");
//
//		pw.close(); 
//
//	}
//	
//	

//	
//	public void geraArqSaidaMO (String nomeArq, ArrayList<Cromossomo> popCromo) throws IOException {
//
//		String arq = pasta + nomeArq;
//		
//		FileWriter fw = new FileWriter(arq);
//		PrintWriter pw = new PrintWriter(fw); 
//		
//		for (int i = 0; i < popCromo.size(); i++) {
//			
//			pw.print(popCromo.get(i).getFitness() + "\t");
//			pw.print(popCromo.get(i).getFitness2() + "\n");
//			
//		}
//		
//
//		
//		pw.close(); 
//	}
//	

//	
//	
//	// ------------------ secao multiobjetivo --------------------------------
//	public void geraArqSaida_NSGA_II (String nomeArq, ArrayList<Cromossomo> popCromo) throws IOException {
//
//		String arq = this.pasta + nomeArq;
//		
//		FileWriter fw = new FileWriter(arq);
//		PrintWriter pw = new PrintWriter(fw); 
//
//		int cont = 0;
//		for (int i = 0; i < popCromo.size(); i++) {
//
//			if(popCromo.get(i).getIdPareto() == 1) {
//				cont++;
//			}
//		}
//		
//		pw.print(cont + "\n");
//
//		for (int i = 0; i < popCromo.size(); i++) {
//
//			if(popCromo.get(i).getIdPareto() == 1) {
//
//				pw.print(popCromo.get(i).getFitness() + "\t");
//				pw.print(popCromo.get(i).getFitness2() + "\n");
//
//			}
//		}
//		pw.close(); 
//	}
//		
//	public void geraArqSaidaMO (String nomeArq, ArrayList<Cromossomo> popCromo) throws IOException {
//
//		String arq = this.pasta + nomeArq;
//		//String arq = "C:\\flavioFOen\\saidas\\" + nomeArq;
//
//		FileWriter fw = new FileWriter(arq);
//		PrintWriter pw = new PrintWriter(fw); 
//
//
//		pw.print(popCromo.size() + "\n");
//
//		for (int i = 0; i < popCromo.size(); i++) {
//
//				pw.print(popCromo.get(i).getFitness() + "\t");
//				pw.print(popCromo.get(i).getFitness2() + "\n");
//
//		}
//
//
//
//		pw.close(); 
//	}
//
//	public void geraArqSaidaMpPareto (String nomeArq, Cromossomo cromo) throws IOException {
//
//		String arq = this.pasta + nomeArq;
//		
//		FileWriter fw = new FileWriter(arq);
//		PrintWriter pw = new PrintWriter(fw); 
//
//		pw.print(cromo.getFitness() + "\t" + cromo.getFitness2());
//
//		pw.close(); 
//	}
//	
//	
//	public void geraArqSaidaPopMO (String nomeArq, ArrayList<Cromossomo> popCromo) throws IOException {
//
//		String arq = this.pasta + nomeArq;
//
//		FileWriter fw = new FileWriter(arq);
//		PrintWriter pw = new PrintWriter(fw); 
//
//
//		pw.print(popCromo.size() + "\n");
//
//		for (int i = 0; i < popCromo.size(); i++) {
//
//			pw.print(popCromo.get(i).getFitness() + "\t");
//			pw.print(popCromo.get(i).getFitness2() + "\n");
//
//			
//		}
//		pw.close(); 
//	}
//	
//	static public void enviarEmail(String emailDest, String nomeDest,
//			  String emailRemet, String nomeRemet, String assunto, String corpo)
//			  throws Exception {
//			  Properties prop = System.getProperties();
//			  prop.put("mail.smtp.host", "smtp.gmail.com");    
//		        prop.put("mail.smtp.auth", "true");    
//		        prop.put("mail.smtp.port", "465");    
//		        prop.put("mail.smtp.starttls.enable", "true");    
//		        prop.put("mail.smtp.socketFactory.port", "465");    
//		        prop.put("mail.smtp.socketFactory.fallback", "false");    
//		        prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//			  //props.put("smtp.starttls.enable", "true");
//
//			  Authenticator auth = new Authenticator() {
//			    public PasswordAuthentication getPasswordAuthentication() {
//			      return new PasswordAuthentication("fcruzeirom", "29p09f01m*");
//			    }};
//
//			  Session session = Session.getInstance(prop, auth);
//			  MimeMessage message = new MimeMessage(session);
//			  message.setFrom(new InternetAddress(emailRemet, nomeRemet));
//			  message.addRecipient(Message.RecipientType.TO,
//			    new InternetAddress(emailDest, nomeDest));
//			  message.setSubject(assunto);
//			  message.setContent(corpo, "text/plain");
//			  
//					  
//			  Transport.send(message);
//			}

}
