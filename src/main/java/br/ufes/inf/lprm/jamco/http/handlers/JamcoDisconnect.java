package br.ufes.inf.lprm.jamco.http.handlers;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;

import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;

import br.ufes.inf.lprm.jamco.Jamco;
import br.ufes.inf.lprm.jamco.http.JamcoHttpServer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class JamcoDisconnect extends JamcoHandlerCommon implements HttpHandler {

	private StatefulKnowledgeSession ksession;
	
	public JamcoDisconnect(StatefulKnowledgeSession ksession) {
		this.ksession = ksession;
	}
	
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		try{
			URI uri = exchange.getRequestURI();
	        String query = uri.getQuery();
	        
	        String id = null;
	        try{
	        	id = getId(query);
	        }catch (Exception e) {
				Jamco.logger.log(Level.SEVERE, "Could not remove vehicle.", e);
				returnResponse(exchange, BAD_REQUEST);
				return;
			}
	        
			FactHandle fh = vehicles.get(id);
			if(fh != null) ksession.retract(fh);
			else Jamco.logger.log(Level.WARNING, "Could not retract vehicle. Reason: vehicle does not exist. Id: " + id);
			
			long amount = JamcoHttpServer.AMOUNT.decrementAndGet();
			Jamco.logger.log(Level.INFO, "Vehicle disconnected. Id: " + id + ". Vehicles amount: " + amount);
			returnResponse(exchange, OK);
		}catch (Exception e) {
			Jamco.logger.log(Level.SEVERE, "Could not remove vehicle.", e);
			returnResponse(exchange, INTERNAL_SERVER_ERROR);
		}
	}

	private String getId (String query) throws Exception {
		if(query == null || query == "")
			throw new Exception("Malformed URL: Missing Parameter.");
			
		String[] parameters =  query.split("&");
		for(int i = 0; i < parameters.length; i++){
			if(parameters[i].startsWith("id=")){
				return parameters[i].substring(3);
			}
		}
		
		throw new Exception("Malformed URL: Missing Parameter.");
	}
	
}
