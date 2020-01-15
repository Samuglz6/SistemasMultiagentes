package trabajo;

import java.util.StringTokenizer;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.domain.FIPANames;


public class Initiator extends Agent{

	private String resultados;

	protected void setup(){

		resultados = "";
		
		Object[] args = getArguments();
		AID receptor = new AID();
		receptor.setLocalName("Gestor");

		System.out.println(args.length);

		if(args != null && args.length == 2)
		{
			System.out.println(getLocalName()+": realizando la peticion de busqueda.");
			ACLMessage mensaje = new ACLMessage(ACLMessage.REQUEST);
			mensaje.addReceiver(receptor);
			mensaje.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
			mensaje.setContent("Buscar "+args[0]+" mostrando "+args[1]+" noticias");

			addBehaviour(new ManejadorInitiator(this,mensaje));
		}
		else
		{
			System.out.println(getLocalName()+": se debe introducir una palabra y un número para la busqueda de noticias.");
			doDelete();
		}
		
		resultado = resultados.replaceAll("\\s+","\n");
		System.out.println(resultados);
	}

	protected void takeDown(){
		System.out.println(getLocalName()+": liberando recursos.");
	}

	class ManejadorInitiator extends AchieveREInitiator
	{
		public ManejadorInitiator(Agent a, ACLMessage msg)
		{
			super(a,msg);
		}

		protected void handleAgree(ACLMessage agree)
		{
			System.out.println(getLocalName()+": "+agree.getSender().getName()+" ha enviado la solicitud de busqueda.");
		}

		protected void handleRefuse(ACLMessage refuse)
		{
			System.out.println(getLocalName()+": "+refuse.getSender().getName()+" ha rechazado la solicitud de busqueda.");
		}

		protected void handleInform(ACLMessage inform)
		{
			System.out.println(getLocalName()+": "+inform.getSender().getName()+" han recibido las noticias solicitadas.");
			resultados = inform.getContent(); 
		}

		protected void handleFailure(ACLMessage failure)
		{
			System.out.println(getLocalName()+": error al recibir las noticias solicitadas por parte de "+failure.getSender().getName()+".");
		}
	}
}
