package servletService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;



@ServerEndpoint("/log") 
public class WebSocketLog {

	private static final Set<WebSocketLog> connections = new CopyOnWriteArraySet<>();
	public static Queue<String> log = new ConcurrentLinkedQueue<>();
	private Session session;

	
	@OnMessage
	public void onMessage(String message, Session session) throws IOException, InterruptedException {

		System.out.println("Received: " + message);
	}


	
	@OnOpen // open connection
	public void onOpen(Session session) {
		this.session = session;
		connections.add(this);
		System.out.println("Client connected");
	}

	@OnClose // close connection
	public void onClose() {
		System.out.println("Connection closed");
		connections.remove(this);
	}

	public static void sendMessage(/*String message*/) {
		
		List<String> buf = new ArrayList<>();
				
				
		for (int i = 0; i < log.size(); i++) {
			buf.add(log.poll());
		}

		for (WebSocketLog con : connections) {
			try {
				 synchronized (con) {
	                    //con.session.getBasicRemote().sendText(message);
	                    
	                    for (int i = 0; i < buf.size(); i++) {
	                    	con.session.getBasicRemote().sendText(buf.get(i));
	                    }
	                }
			} catch (IOException ex) {

			}
		}
	}
	
	@OnError
	public void onError(Session session, Throwable thr) {}

}