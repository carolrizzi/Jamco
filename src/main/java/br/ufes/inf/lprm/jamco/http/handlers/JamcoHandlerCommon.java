package br.ufes.inf.lprm.jamco.http.handlers;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;

import org.drools.runtime.rule.FactHandle;

import com.sun.net.httpserver.HttpExchange;

public class JamcoHandlerCommon {

	protected static final int OK = 200;
	protected static final int INTERNAL_SERVER_ERROR = 500;
	protected static final int BAD_REQUEST = 400;
	protected static final int NOT_FOUND = 404;
	
	protected static ConcurrentHashMap<String, FactHandle> vehicles = new ConcurrentHashMap<>();
	
	protected void returnResponse (HttpExchange exchange, int code) {
		OutputStream os = null;
		try {
			exchange.sendResponseHeaders(code, 0);
            os = exchange.getResponseBody();
		} catch (IOException e) {
			System.err.println("[SEVERE] Could not return response. Reason: IOException");
		} finally {
			try {
				os.close();
			} catch (IOException e) {}
		}
	}
	
	protected void returnResponse (HttpExchange exchange, String response) {
		returnResponse(exchange, OK, response);
	}
	
	protected void returnResponse (HttpExchange exchange, int code, String response) {
		OutputStream os = null;
		try {
			exchange.sendResponseHeaders(code, response.length());
            os = exchange.getResponseBody();
            os.write(response.getBytes());
		} catch (IOException e) {
			System.err.println("[SEVERE] Could not return response. Reason: IOException");
		} finally {
			try {
				os.close();
			} catch (IOException e) {}
		}
	}
}
