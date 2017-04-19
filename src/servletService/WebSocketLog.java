package servletService;

import java.io.IOException;
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
	}

	
	//broadcase all the log message
	public static void sendMessage(String message) {

		for (WebSocketLog con : connections) {
			try {
				 synchronized (con) {
	                    con.session.getBasicRemote().sendText(message);
	                }
			} catch (IOException ex) {

			}
		}
	}
	
	@OnError
	public void onError(Session session, Throwable thr) {}

}