package br.ufes.inf.lprm.jamco.geocoding;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import br.ufes.inf.lprm.jamco.Jamco;
import br.ufes.inf.lprm.jamco.model.Location;

public class LocalReverseGeocoding {
	
	private static final String FILENAME = "addresses.ser";
	private static ConcurrentHashMap<String, String> addresses = new ConcurrentHashMap<>();
	
	public static String getAddress (Location location) throws Exception {
		if(location == null) throw new NullPointerException("Location cannot be null.");
		
		String key = location.getLatitude() + ";" + location.getLongitude();
		return addresses.get(key);
	}
	
	public static void addAddress (Location location, String address) throws Exception {
		if(location == null) throw new NullPointerException("Location cannot be null.");
		
		String key = location.getLatitude() + ";" + location.getLongitude();
		addresses.putIfAbsent(key, address);
	}
	
	@SuppressWarnings("unchecked")
	public static void load () {
		try{
			InputStream file = new FileInputStream(FILENAME);
			InputStream buffer = new BufferedInputStream( file );
			ObjectInput input = new ObjectInputStream ( buffer );
			try{
				addresses = (ConcurrentHashMap<String, String>) input.readObject();
			}
			finally{
				input.close();
			}
		}catch (Exception e) {
			Jamco.logger.log(Level.SEVERE, "Could not load local geocodings.", e);
		}
	}
	
	public static void save () {
		try{
			OutputStream file = new FileOutputStream(FILENAME);
			OutputStream buffer = new BufferedOutputStream(file);
			ObjectOutput output = new ObjectOutputStream(buffer);
			try{
				output.writeObject(addresses);
			}finally{
				output.close();
			}
		}catch(Exception e){
			Jamco.logger.log(Level.SEVERE, "Could not save local geocodings.", e);
		}
	}
}
