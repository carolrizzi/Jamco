package br.ufes.inf.lprm.jamco.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;

import org.drools.runtime.StatefulKnowledgeSession;

import br.ufes.inf.lprm.jamco.http.handlers.JamcoDisconnect;
import br.ufes.inf.lprm.jamco.http.handlers.JamcoGetId;
import br.ufes.inf.lprm.jamco.http.handlers.JamcoUpdate;

import com.sun.net.httpserver.HttpServer;

public class JamcoHttpServer extends Thread {
	public static AtomicLong AMOUNT = new AtomicLong(0);
	private HttpServer server = null;
	
	public JamcoHttpServer(int port, StatefulKnowledgeSession ksession) throws IOException {
		server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/Jamco/getid", new JamcoGetId(ksession));
        server.createContext("/Jamco/disconnect", new JamcoDisconnect(ksession));
        server.createContext("/Jamco/update", new JamcoUpdate(ksession));
        server.setExecutor(null);
	}
	
	public void run () {
		server.start();
	}
	
	public void stopServer () {
		server.stop(NORM_PRIORITY);
	}
}

