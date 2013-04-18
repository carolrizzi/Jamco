package br.ufes.inf.lprm.jamco.http.handlers;

import java.net.URI;
import java.util.HashMap;
import java.util.logging.Level;

import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;

import br.ufes.inf.lprm.jamco.Jamco;
import br.ufes.inf.lprm.jamco.geocoding.GoogleReverseGeocoding;
import br.ufes.inf.lprm.jamco.geocoding.LocalReverseGeocoding;
import br.ufes.inf.lprm.jamco.model.Location;
import br.ufes.inf.lprm.jamco.model.Bus;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class JamcoUpdate extends JamcoHandlerCommon implements HttpHandler{

	private StatefulKnowledgeSession ksession;
	
	public JamcoUpdate(StatefulKnowledgeSession ksession) {
		this.ksession = ksession;
	}
	
	@Override
	public void handle(HttpExchange exchange) {
		try{
			URI uri = exchange.getRequestURI();
	        String query = uri.getQuery();
	        
	        QueryParser parser = null;
	        try{
	        	 parser = new QueryParser(query);
	        }catch (Exception e) {
				Jamco.logger.log(Level.SEVERE, "Could not update vehicle.", e);
				returnResponse(exchange, BAD_REQUEST);
				return;
			}
			String id = parser.getId();
			
			FactHandle fh = vehicles.get(id);
			if(fh != null){
				Bus vehicle = (Bus) ksession.getObject(fh);
				if(vehicle != null){
					parser.updateVehicle(vehicle);
					ksession.update(fh, vehicle);
		            returnResponse(exchange, OK);
		            return;
				}
			}
        	Jamco.logger.log(Level.SEVERE, "Could not update vehicle. Reason: vehicle does not exist. Id: " + id);
        	returnResponse(exchange, BAD_REQUEST);
		}catch (Exception e) {
			Jamco.logger.log(Level.SEVERE, "Could not update vehicle.", e);
			returnResponse(exchange, INTERNAL_SERVER_ERROR);
		}
	}

	class QueryParser {

		private String id;
		private double speed;
		private Location location;		
		
		public QueryParser(String query) throws Exception {
			HashMap<String, String> attributes = new HashMap<String, String>();
			String [] strAttributes = null;
			try{
				strAttributes = query.split("&");
	        	if(strAttributes.length < 4)
	        		throw new Exception("Malformed URL: Missing Parameter.");
	
	        	for(int i = 0; i < strAttributes.length; i++){
	        		String [] key_value = strAttributes[i].split("=");
	        		attributes.put(key_value[0], key_value[1]);
	        	}
	        	
	    	}catch (Exception e) {
				throw new Exception("Malformed URL: Unkown Reason. Query: " + query, e);
			}
			
	    	try{
				speed = Double.parseDouble(attributes.get("speed"));
				id = attributes.get("id");
				location = new Location(Double.parseDouble(attributes.get("lat")), Double.parseDouble(attributes.get("lng")));
	    	}catch (Exception e) {
	    		throw new Exception ("Malformed URL: Missing Parameter. Query: " + query, e);
			}
		}
		
		public String getId() {
			return id;
		}
		
		public void updateVehicle (Bus vehicle) throws Exception {
			//if(speed <= 0 && vehicle.getSpeed() == 0) return;
			vehicle.setSpeed(speed);
			String address = LocalReverseGeocoding.getAddress(location);
			if(address == null){
				address = GoogleReverseGeocoding.getAddress(location);
				LocalReverseGeocoding.addAddress(location, address);
			}else{
			}
			vehicle.setAddress(address);
		}	

	}
}
