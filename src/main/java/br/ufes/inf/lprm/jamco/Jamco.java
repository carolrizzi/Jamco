package br.ufes.inf.lprm.jamco;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.ResourceType;
import org.drools.conf.EventProcessingOption;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;

import br.ufes.inf.lprm.jamco.geocoding.LocalReverseGeocoding;
import br.ufes.inf.lprm.jamco.http.JamcoHttpServer;
import br.ufes.inf.lprm.scene.SituationKnowledgeBaseFactory;
import br.ufes.inf.lprm.scene.SituationKnowledgeBuilderFactory;

public class Jamco {

	public static Logger logger;
	private static JamcoHttpServer httpServer = null;
	private static StatefulKnowledgeSession ksession = null;
	private static Engine engine = null;
	
    public static final void main(String[] args) {
		Jamco.startServer();
        
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while(true){
        	String in;
			try {
				in = br.readLine();
            	if(in.toLowerCase().equals("exit") || in.toLowerCase().equals("stop")){
            		Jamco.stopServer();
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, "Could not read command to stop the system. Stopping anyway.", e);
				Jamco.stopServer();
			}
        }
    }

    public static void startServer (){
    	int PORT = 8000;
    	double DEFAULT_MIN_SPEED = 20.0; // km/h
    	try {
	    	logger = Logger.getLogger(Jamco.class.getName());
	    	
	    	Properties prop = new Properties();
			LogManager.getLogManager().readConfiguration(new FileInputStream("jamco.properties"));
			
			prop.load(new FileInputStream("jamco.properties"));
			
			DEFAULT_MIN_SPEED = Double.parseDouble(getProperty(prop, "default_min_speed", "" + DEFAULT_MIN_SPEED));
			PORT = Integer.parseInt(getProperty(prop, "port", "" + PORT));
		} catch (Exception e) {
			logger.setLevel(Level.INFO);
			logger.log(Level.SEVERE, "Could not open jamco.properties. Assigning default values to properties and logger.", e);
		}
    	
    	try{
    		LocalReverseGeocoding.load();
    	}catch (Exception e) {
    		logger.log(Level.WARNING, "Could not load local geocoding data.", e);
		}
    	
    	try {
			ksession = readKnowledgeBase();
			ksession.setGlobal("DEFAULT_MIN_SPEED", DEFAULT_MIN_SPEED);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Could not start knoledge base. Stopping the system.", e);
			System.exit(1);
		}
//		KnowledgeRuntimeLogger klogger = KnowledgeRuntimeLoggerFactory.newFileLogger(ksession, "test");
    	try{
	        engine = new Engine(ksession);
	        engine.start();
    	}catch (Exception e) {
        	logger.log(Level.SEVERE, "Could not start Drools Rule Engine. Stopping the system.", e);
			System.exit(1);
		}
        
        try{
        	Jamco.httpServer = new JamcoHttpServer(PORT, ksession);
        	httpServer.start();
        }catch (Exception e) {
        	logger.log(Level.SEVERE, "Could not start Http server. Stopping the system.", e);
			System.exit(1);
		}
        
        System.out.println("Jamco is up and running on port " + PORT + ". Type 'stop' to stop.");
    }
    
    private static String getProperty (Properties prop, String property, String def) {
    	try{
			//property = "port";
			String strPort = prop.getProperty(property);
			if(strPort != null && !strPort.equals(""))
				return strPort;
		}catch (Exception e) {
			logger.log(Level.WARNING, "Could not read property '" + property + "'. Assigning defaul value to " + property + ": " + def, e);
		}
    	return def;
    }
    
    public static void stopServer () {
    	try{
    		LocalReverseGeocoding.save();
    	}catch (Exception e) {
    		logger.log(Level.WARNING, "Could not save local geocoding data.", e);
		}
    	
    	try{
    		httpServer.stopServer();
    	}catch (Exception e) {
			logger.log(Level.WARNING, "Could not stop Http Server. Stopping the system anyway.", e);
		}
		
    	try{
    		engine.stopEngine();
    	}catch (Exception e) {
			logger.log(Level.WARNING, "Could not stop the Drools Rule Engine. Stopping the system anyway.", e);
		}
//        klogger.close();
    	logger.log(Level.INFO, "Jamco has been shut down.");
		System.exit(0);
    }
    
    private static StatefulKnowledgeSession readKnowledgeBase() throws Exception {
		KnowledgeBuilder kbuilder = SituationKnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource("Rule.drl"), ResourceType.DRL);
        KnowledgeBuilderErrors errors = kbuilder.getErrors();

        if (errors.size() > 0) {
            for (KnowledgeBuilderError error: errors) {
                System.err.println(error);
            }
            throw new IllegalArgumentException("Could not parse knowledge.");
        }
        
        KnowledgeBaseConfiguration config = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
		config.setOption( EventProcessingOption.STREAM );
        KnowledgeBase kbase = SituationKnowledgeBaseFactory.newKnowledgeBase(kbuilder, config);
        
        return kbase.newStatefulKnowledgeSession();
    }
}

class Engine extends Thread {
	
	private StatefulKnowledgeSession ksession;
	
	public Engine(StatefulKnowledgeSession ksession) {
		this.ksession = ksession;
	}
	
	private void fireEngine () {
		try{
			Jamco.logger.log(Level.INFO, "Starting Rule Engine");
			ksession.fireUntilHalt();
		}catch (Exception e) {
			Jamco.logger.log(Level.WARNING, "Rule Engine Exception. Restarting engine thread.", e);
			this.fireEngine();
		}
	}
	
	public void run () {
		fireEngine();
	}
	
	public void stopEngine() {
		Jamco.logger.log(Level.INFO, "Stopping Rule Engine");
		ksession.halt();
	}
	
}
