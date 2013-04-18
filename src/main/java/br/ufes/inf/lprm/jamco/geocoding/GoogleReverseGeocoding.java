package br.ufes.inf.lprm.jamco.geocoding;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import br.ufes.inf.lprm.jamco.Jamco;
import br.ufes.inf.lprm.jamco.model.Location;

public class GoogleReverseGeocoding {
	
	private static final String REQUEST_PREFIX = "http://maps.google.com/maps/api/geocode/json?latlng=";
	private static final String REQUEST_POSFIX = "&sensor=false&language=pt-BR";
	
	public static String getAddress (Location location) throws Exception {
		if(location == null) throw new NullPointerException("Location cannot be null.");
		
		URL url = new URL(REQUEST_PREFIX + location.getLatitude() + "," + location.getLongitude() + REQUEST_POSFIX);
		
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<String> future = executor.submit(new Task(url));
		
		try {
			return future.get(10, TimeUnit.SECONDS); //TODO: should be a configuration
		} catch (Exception e) {
			throw new Exception(e);
		}finally{
			executor.shutdownNow();
		}
	}
}

class Task implements Callable<String> {
    private HttpURLConnection conn;
	private URL url;

    public Task(URL url) {
		this.url= url;
	}

	@Override
    public String call() {
		BufferedReader br = null;
		StringBuilder sb = null;
		String line;
		String parsedQuery;
    	while(true){
			try {
				conn = (HttpURLConnection) url.openConnection();
				conn.connect();
				br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				sb = new StringBuilder();
				
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				
				parsedQuery = parseQuery(sb.toString());
				
				try {
					br.close();
					conn.disconnect();
				} catch (Exception e) {}
				
				return parsedQuery;
			} catch (Exception e) {
				Jamco.logger.log(Level.WARNING, "Could not get reverse geocoding information.", e);
				conn.disconnect();
				try{Thread.sleep(100);}catch(Exception e1){}
			}
		}
    }
	
	private String parseQuery (String str) throws Exception {
		JSONObject json = null;
		String status = null;
		try{
			json = (JSONObject) JSONSerializer.toJSON(str);
			status = json.getString("status");
		}catch (Exception e) {
			throw new Exception("Error while parsing reverse geocoding response. Response: " + str, e);
		}
		
		if(!status.equals("OK"))
			throw new Exception("Reverse Geocoding response is not ready. Status: " + status);
		
		try{
			JSONArray results = json.getJSONArray("results");
			return results.getJSONObject(0).getString("formatted_address");
		}catch (Exception e) {
			throw new Exception("Invalid reverse geocoding response or address was not found.", e);
		}
	}
}