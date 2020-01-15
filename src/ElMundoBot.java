package trabajo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.*;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ElMundoBot extends Agent
{
	private String url;
	private ArrayList<String> resultados;
	private String palabra;
	private int numero;

	protected void setup()
	{
		url = "http://ariadna.elmundo.es/buscador/archivo.html?q=";
	 	resultados = new ArrayList<String>();

		addBehaviour(new ComportamientoRecepcion());
	}

	protected void takeDown()
	{
		System.out.println(getLocalName()+":  liberando recursos.");
	}

	class ComportamientoRecepcion extends Behaviour
	{
		private AID emisor;
		private ACLMessage mensaje;
		private MessageTemplate plantilla;

		public void onStart()
		{
			emisor = new AID();
			emisor.setLocalName("Gestor");

			mensaje = new ACLMessage(ACLMessage.REQUEST);
			plantilla = MessageTemplate.MatchSender(emisor);
		}

		public void action()
		{
			mensaje = blockingReceive(plantilla);

			StringTokenizer token = new StringTokenizer(mensaje.getContent());
			url += token.nextToken();
			url += "&fd=365&td=0&n=50&w=60&s=0&fecha_busq_avanzada=1"
			numero = Integer.parseInt(token.nextToken());
		}

		public boolean done()
		{
			return true;
		}

		public int onEnd
		{
			addBehaviour(new ComportamientoBusqueda());
			return 1;
		}
	}

	class ComportamientoBusqueda extends Behaviour
	{
		private ArrayList<String> datos;
		private Pattern p;
		private Matcher m;
		
		public void onStart()
		{
			datos = new ArrayList<String>();
			datos.add("ul.lista_resultados > li:not(.accesos_directos) > h3 > a");
			datos.add("div.num_resultados > ul > li > a");
			datos.add("Siguiente");
			
			p = Pattern.compile([a-zA-Z][a-zA-Z][a-zA-Z]+);
		}

		public void action()
		{
			m = p.matcher(palabra);
			if(m)
			{
				extraer(url, datos);
			}
			else
			{
				resultados = "";
			}
		}

		public boolean done()
		{
			return true;
		}
		public int onEnd(){
			addBehaviour(new ComportamientoEnvio());
			return 1;
		}
	}
	class ComportamientoEnvio extends Behaviour
	{
		String contenido
		AID gestor;
		MessageTemplate plantilla;
		ACLMessage mensaje;

		public void onStart()
		{
			contenido = "";
			gestor = new AID();
			gestor.setLocalName("Gestor");
			plantilla = MessageTemplate.MatchSender(gestor);
		}

		public void action()
		{
			for(int i = 0; i < resultados.size() || i == numero-1; i++){
				contenido += resultados.get(i) + " ";
			}

			mensaje = new ACLMessage();
			mensaje.setSender(getAID());
			mensaje.addReceiver(plantilla);
			mensaje.setContent(contenido);
			send(mensaje);
		}

		public boolean done()
		{
			return true;
		}
	}
	/*Aqui van los metodos correspondientes al bot encargados de hacer el web scrapping*/

	public void extraer(String url, ArrayList<String> datos) throws IOException
	{
		Document doc = Jsoup.connect(url).get();

		Elements result = doc.select(datos.get(0));

		for(Element element : result)
		{
		    resultados.add(element.attr("href"));
		}


		Elements next = doc.select(datos.get(1));

		for(Element element : next)
		{
			if(url.contains("elmundo") && element.text().contains(datos.get(2)))
			{
				url = "https:" + element.attr("href");
				extraer(url, datos);
				break;
			}

		    if(url.contains("bbc") && element.hasClass(datos.get(2)))
		    {
		        url = url.split("[?]q=")[0] + element.attr("href");
		        extraer(url, datos);
		        break;
		    }

		    if(url.contains("eldiario") && element.text().contains(datos.get(2)))
		    {
		    	url = url.split("/buscador")[0] + element.attr("href");
		    	extraer(url, datos);
		    	break;
		    }
		}
	}

	public ArrayList<String> getResultados()
	{
		return this.resultados;
	}

    	public String getUrl() {
        	return url;
    	}
}
