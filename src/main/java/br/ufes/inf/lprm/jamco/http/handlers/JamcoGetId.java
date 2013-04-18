package br.ufes.inf.lprm.jamco.http.handlers;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;

import br.ufes.inf.lprm.jamco.Jamco;
import br.ufes.inf.lprm.jamco.geocoding.GoogleReverseGeocoding;
import br.ufes.inf.lprm.jamco.geocoding.LocalReverseGeocoding;
import br.ufes.inf.lprm.jamco.http.JamcoHttpServer;
import br.ufes.inf.lprm.jamco.model.Location;
import br.ufes.inf.lprm.jamco.model.Bus;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class JamcoGetId extends JamcoHandlerCommon implements HttpHandler {

	private StatefulKnowledgeSession ksession;
	
	public JamcoGetId (StatefulKnowledgeSession ksession) {
		this.ksession = ksession;
	}
	
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		try {
            URI uri = exchange.getRequestURI();
	        String query = uri.getQuery();
	        
	        FactHandle fh = null;
	        
			while(true){
				String id = UUID.randomUUID().toString();
				
				Bus vehicle = null;
				try{
					vehicle = createVehicle(id, query);
				} catch (Exception e) {
					Jamco.logger.log(Level.SEVERE, "Could not insert new vehicle.", e);
					returnResponse(exchange, BAD_REQUEST);
					return;
				}
		        
		        if(fh == null) fh = ksession.insert(vehicle);
		        else ksession.update(fh, vehicle);
		        
		        FactHandle existingFh = vehicles.putIfAbsent(id, fh);
		        if(existingFh == null){
		        	long amount = JamcoHttpServer.AMOUNT.incrementAndGet();
		        	Jamco.logger.log(Level.INFO, "Vehicle inserted. Id: " + id + ". Vehicles amount: " + amount);
		            returnResponse(exchange, id);
		            break;
		        }
			}
		} catch (Exception e) {
			Jamco.logger.log(Level.SEVERE, "Could not insert new vehicle.", e);
			returnResponse(exchange, INTERNAL_SERVER_ERROR);
		}

	}
	
	private Bus createVehicle (String id, String query) throws Exception {
		String [] strAttributes = null;
		HashMap<String, String> attributes = new HashMap<String, String>();;
		try{
			strAttributes = query.split("&");
        	if(strAttributes.length < 3)
        		throw new Exception("Malformed URL: Missing Parameter.");

        	for(int i = 0; i < strAttributes.length; i++){
        		String [] key_value = strAttributes[i].split("=");
        		attributes.put(key_value[0], key_value[1]);
        	}
        	
    	}catch (Exception e) {
    		throw new Exception("Malformed URL: Unkown Reason. Query: " + query, e);
		}
    	
		double speed = -1;
		Location location = null;
    	try{
			speed = Double.parseDouble(attributes.get("speed"));
        	location = new Location(Double.parseDouble(attributes.get("lat")), Double.parseDouble(attributes.get("lng")));
    	}catch (Exception e) {
    		throw new Exception ("Malformed URL: Missing Parameter. Query: " + query, e);
		}
    	
    	try{
    		String address = LocalReverseGeocoding.getAddress(location);
    		if(address != null){
    			return new Bus (speed, address, id);
    		}
    	}catch (Exception e) {
    		throw new Exception ("Error while performing local geocoding query.", e);
		}
    	
    	try{
    		String address = GoogleReverseGeocoding.getAddress(location);
    		LocalReverseGeocoding.addAddress(location, address);
	    	return new Bus(speed, address, id);
    	}catch (Exception e) {
    		throw new Exception ("Error while requesting current vehicle address.", e);
		}
    }

}
