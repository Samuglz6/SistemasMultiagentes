package trabajo;

import java.util.StringTokenizer;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;


public class Gestor extends Agent{
	
	private final int NOTICIAS_MIN = 5;
	private final int NOTICIAS_MAX = 20;
	private int noticias = -1;
	private String palabra;
	private String resultados;

	protected void setup()
	{
		System.out.println(getLocalName()+": a la espera de peticiones de busqueda...");
		
		MessageTemplate protocolo = MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		MessageTemplate performativa = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
		MessageTemplate plantilla = MessageTemplate.and(protocolo, performativa);

		addBehaviour(new ManejadorResponder(this,plantilla));
	}

	protected void takeDown(){
		System.out.println(getLocalName()+": liberando recursos.");
	}

	class ManejadorResponder extends AchieveREResponder
	{
		AID Bot1, Bot2, Bot3;
		MessageTemplate plantilla1, plantilla2, plantilla3;
		ACLMessage mensaje;
		
		public ManejadorResponder(Agent a, MessageTemplate template)
		{
			super(a, template);
		}

		protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException
		{
			System.out.println(getLocalName()+": se ha recibido una peticion de busqueda de parte de "+ request.getSender().getName());

			StringTokenizer token = new StringTokenizer(request.getContent());
			if(token.nextToken().equalsIgnoreCase("Buscar"))
			{
				palabra = token.nextToken();

				if(token.nextToken().equalsIgnoreCase("mostrando"))
				{
					noticias = Integer.parseInt(token.nextToken());

					if(noticias <= NOTICIAS_MAX && noticias >= NOTICIAS_MIN)
					{
						ACLMessage agree = request.createReply();
						agree.setPerformative(ACLMessage.AGREE);
						
						return agree;
					}
					else
						throw new RefuseException("Cantidad de noticias no válidas.");
				}else
					throw new NotUnderstoodException("No se ha comprendido el mensaje recibido.");
			}else
				throw new NotUnderstoodException("No se ha comprendido el mensaje recibido.");		
		}
		
		public void onStart()
		{	
			Bot1 = new AID();
			Bot1.setLocalName("BBCBot");
			
			Bot2 = new AID();
			Bot2.setLocalName("ElMundoBot");
			
			Bot3 = new AID();
			Bot3.setLocalName("LaRazonBot");

			plantilla1 = MessageTemplate.MatchSender(Bot1);
			plantilla2 = MessageTemplate.MatchSender(Bot2);
			plantilla3 = MessageTemplate.MatchSender(Bot3);
			
			resultado = "";
		}		

		public void action()
		{
			mensaje = new ACLMessage(ACLMessage.REQUEST);
			
			mensaje.setSender(getAID());
			mensaje.addReceiver(Bot1);
			send(mensaje);
			
			mensaje.setSender(getAID());
			mensaje.addReceiver(Bot2);
			send(mensaje);

			mensaje.setSender(getAID());
			mensaje.addReceiver(Bot3);
			send(mensaje);
			
			mensaje = blockingReceive(plantilla1);
			resultados += mensaje.getContent();
			
			resultados += " ";
			mensaje = blockingReceive(plantilla2);
			resultados += mensaje.getContent();
			
			resultados += " ";
			mensaje = blockingReceive(plantilla3);
			resultados += mensaje.getContent();
		}

		protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException
		{
			if(resultados.length == 0){
				throws new FailureException(getLocalName()+": se ha producido un error en la busqueda.");
			}
			else
			{
				System.out.println(getLocalName()+": enviada respuesta.");
				ACLMessage inform =  request.createReply();
				inform.setPerformative(ACLMessage.INFORM);
				inform.setContent(resultados);

				return inform;
			}
		}
	}
}