package eversync.server;

import java.net.*; 
import java.util.HashMap;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.io.*; 

import org.json.JSONObject;

import eversync.server.EverSyncClient;
import eversync.server.EvernoteClient;
import eversync.server.MessageReflect;


public class Server  {
	
	
	/**
	 * The port on which the server will listen
	 */
	private static final int serverPort = 8080;
	
	/**
	 * A set of currently connected clients
	 */
	private static HashSet<String> _connectedClients = new HashSet<String>();
	
	/**
	 * A hash map where the keys are the id's of the clients and values instances of the clients.
	 * Used to store previously installed clients.
	 * Once a client has been installed, it won't be deleted from this hashmap, unless the client explicitly says to do so.
	 * Therefore, an instance of a client object is created only once.
	 */
	private static HashMap<String, EverSyncClient> _installedClients = new HashMap<String, EverSyncClient>();
	
	/**
	 * A hash map where the keys are the names of the plugin's and the values are the instances of the plugin's.
	 */
	private static HashMap<String, Plugin> _plugins = new HashMap<String, Plugin>();
	
	
	/**
	 * When a new client tries to connect to the client, they both have to handshake.
	 * The client says if it's is a new client (has no id assigned yet).
	 * If so, the installNewClient method is triggered in order to install the new client on the server, and assign an id.
	 * Finally, after the handshake, the client ID is added to the set _connectedClients.
	 * @param in
	 * @param out
	 * @return
	 * @throws Exception
	 */
	private static EverSyncClient handShake(BufferedReader in, PrintWriter out) throws Exception {
		// Prepare the handshake request
		JSONObject req = new JSONObject();
		req.put("type", "Handshake Request");
		String handshakeRequest = req.toString();
		
		// Send the handshake request and read the response
		out.println(handshakeRequest);
		JSONObject res = new JSONObject(in.readLine());

		String clientId = null;
		EverSyncClient client;
		
		// Two possible response types are possible.
		// Or it is the very first handshake with this client ever, 
		// then it has to be registered in the server before any information exchange can happen.
		// Or it has already being registered, so then the response type is just a normal handshake.
		String resType = (String) res.get("type");
		if (resType.equals("Handshake Response")) {
			clientId = (String) res.get("clientId");
			client = _installedClients.get(clientId);
		} else if (resType.equals("Initial Handshake Response")) {
			// Generate a new unique id for the client
			do {
				clientId = UUID.randomUUID().toString();
			} while (_installedClients.containsKey(clientId));
			
			client = new EverSyncClient(clientId);
			installNewClient(client, in, out);
		} else {
			throw new NoSuchElementException();
		}
		
		client.setIn(in);
		client.setOut(out);
		_connectedClients.add(clientId);
		return client;
	}
	
	/**
	 * When a new client is added to the system, an object of the EverSyncClient has to be instantiated with it.
	 * Moreover, the client receives an id, and some additional data of the plugin's installed in the EverSync (on the server)
	 * is pushed to the client.
	 * @param client
	 * @throws Exception
	 */
	private static void installNewClient(EverSyncClient client, BufferedReader in, PrintWriter out) throws Exception {
		// Prepare and send the installation request
		JSONObject req = new JSONObject();
		req.put("type", "Client Installation Request");
		String installRequest = req.toString();
		out.println(installRequest);
		
		// Receive the response with the information about the client.
		JSONObject res = new JSONObject(in.readLine());
		// Get the key values from the message
		String os = (String) res.get("OS");
		// Send the values to the client object.
		client.setOs(os);
				
		// Prepare and send the acknowledgement message with the assigned id of the client.
		JSONObject ack = new JSONObject();
		ack.put("type", "Client Installation Acknowledgement");
		ack.put("clientId", client.getId());
		ack.put("rootPath", client.getRootPath());
		String installAcknowledgement = ack.toString();
		out.println(installAcknowledgement);
		
		// TO-DO
		// HERE COMES THE PLUGIN INSTALLATION TO THE CLIENT
		
		// Finally add the client to the hashmap of the installed clients.
		_installedClients.put(client.getId(), client);
	}

	
	/**
	 * The application main method used for initialization of the plugins and adding them to the server.
	 * It also listens on the server port and spawns handler threads.
	 */
	public static void main (String[] args) throws Exception {
		System.out.println("EverSync server started!");
		System.out.println("Initializing plugins ...");
		
		System.out.println("-- Initializing Evernote plugin ...");
		EvernoteClient evernote = new EvernoteClient("S=s1:U=8de6c:E=14b6ad2d7e3:C=1441321abe6:P=1cd:A=en-devtoken:V=2:H=1d6bfa88d4e429fe3918e4584561e51e");
		_plugins.put("Evernote", evernote);
		
		System.out.println("All plugins initialized successfully!");
		
		ServerSocket serverSocket = new ServerSocket(serverPort);
		try {
			while(true) {
				new ConnectionHandler(serverSocket.accept()).start();
			}
		} finally {
			serverSocket.close();
		}
		
	}
	
	private static class ConnectionHandler extends Thread {
		private String _clientID;
		private EverSyncClient _client;
		
		/**
		 * Initialize a new connection between the client and trigger the handshake mechanism between the server and the client
		 * @param clientSocket
		 * @throws IOException
		 */
		public ConnectionHandler(Socket clientSocket) throws IOException {			
			// Create character streams for the socket.
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            try {
            	_client = handShake(in, out);
            	_clientID = _client.getId();
            } catch (Exception e) {
            	e.printStackTrace();
            }
			
			System.out.println("Connected to the client: " + _clientID);
		}
		
		
		public void run() {
			// HIER MOET IK TELKENS ALS HET SCHRIJVEN OF LEZEN NIET LUKT, INTERPRETEREN ALS EEN DISCONNECTIE!!!
			//_client.sendMsg("Hallo!!");
		}
	}
} 