package eversync.server;

import java.net.*; 
import java.util.HashMap;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.logging.Logger;
import java.io.*; 

import eversync.plugins.EvernotePlugin;
import eversync.plugins.PluginManager;
import eversync.server.EverSyncClient;
import eversync.server.MessageReflect;
import eversync.server.Message.*;


public class Server  {
	// Logger for debugging purposes
	private static Logger log = Logger.getLogger(Server.class.getName());

	/**
	 * The port on which the server will listen
	 */
	private static final int _serverPort = 8080;

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
	 * Objects to control the plugins of EverSync.
	 * The constructor of the plugin manager will not initialize the plugin objects. This 
	 * has to be done through the installPlugins() method.
	 */
	private static PluginManager _pluginManager = new PluginManager();
	
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
	private static EverSyncClient handShake(Connection conn) throws Exception {
		// Prepare the handshake request
		HandshakeRequest handshakeRequest = new HandshakeRequest();

		// Send the handshake request and read the response
		conn.sendMsg(handshakeRequest);
		Message res = conn.getMsg();

		String clientId;
		EverSyncClient client;

		// Two possible response types are possible.
		// Or it is the very first handshake with this client ever, 
		// then it has to be registered in the server before any information exchange can happen.
		// Or it has already being registered, so then the response type is just a normal handshake.
		String resType = res.getMsgType();
		if (resType.equals("Handshake Response")) {
			clientId = res.getValue("clientId");
			client = _installedClients.get(clientId);
		} else if (resType.equals("Initial Handshake Response")) {
			// Generate a new unique id for the client
			do {
				clientId = UUID.randomUUID().toString();
			} while (_installedClients.containsKey(clientId));

			client = new EverSyncClient(clientId);
			installNewClient(client, conn);
		} else {
			throw new NoSuchElementException();
		}

		client.setConn(conn);
		return client;
	}

	/**
	 * When a new client is added to the system, an object of the EverSyncClient has to be instantiated with it.
	 * Moreover, the client receives an id, and some additional data of the plugin's installed in the EverSync (on the server)
	 * is pushed to the client.
	 * @param client
	 * @throws Exception
	 */
	private static void installNewClient(EverSyncClient client, Connection conn) throws Exception {
		// Prepare and send the installation request
		InstallRequest installRequest = new InstallRequest();

		conn.sendMsg(installRequest);

		// Receive the response with the information about the client.
		Message res = conn.getMsg();
		// Get the key values from the message
		String os = res.getValue("OS");
		// Send the values to the client object.
		client.setOs(os);

		// Prepare and send the acknowledgement message with the assigned id of the client.
		InstallAcknowledgement installAcknowledgement = 
				new InstallAcknowledgement(client.getId(), client.getRootPath());

		conn.sendMsg(installAcknowledgement);

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
		
		// Install all plugins in the system.
		_pluginManager.installPlugins();

		ServerSocket serverSocket = new ServerSocket(_serverPort);
		try {
			while(true) {
				// Start new thread per connection
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
			DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
			Connection connection = new Connection(in, out);

			try {
				_client = handShake(connection);
				_clientID = _client.getId();
				_connectedClients.add(_clientID);
			} catch (Exception e) {
				e.printStackTrace();
			}
			log.info("Client connected: " + _clientID);
		}


		public void run() {
			// While the client is connected, listen to its messages
			while (_connectedClients.contains(_clientID)) {
				try {
					Message msg = _client.getMsg();
					String msgType = msg.getMsgType();
					switch(msgType) {
//						case "Sync Request":
//						synchronize(_client, msg);
//							break;
					case "Normal Request":
						MessageReflect.parseMessage(_client, msg);
						break;
					default:
						log.severe("Received unsupported message type: '"+ msg.getMsgType() +"' from client: " + _clientID);
					}

//					if (msgType.equals("Normal Request")) {
//						MessageReflect.parseMessage(_client, msg);
//					} else {
//						log.severe("Received unsupported message type: '"+ msg.getMsgType() +"' from client: " + _clientID);
//					}
				} catch (Exception e) {
					// Handle the disconnection of a client.
					_connectedClients.remove(_clientID);
					log.info("Client disconnected: " + _clientID);
					break;
				}
			}
		}
	}
} 