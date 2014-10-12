package eversync.server;

import java.net.*; 
import java.util.HashMap;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.logging.Logger;
import java.io.*; 

import eversync.iServer.IServerManagerEverSyncClient;
import eversync.iServer.IServerManagerInterface;
import eversync.iServer.IServerManagerServicePlugin;
import eversync.plugins.PluginManager;
import eversync.plugins.Evernote.EvernotePlugin;
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
	
	private final static FileEventHandler _fileEventHandler = new FileEventHandler();
	
	private final static PluginManager _pluginManager = new PluginManager(_fileEventHandler);
	private final static EverSyncClientManager _clientManager = new EverSyncClientManager();
	
	private final static MessageReflect _messageReflect = new MessageReflect(_fileEventHandler, _clientManager);

	// TOTO describe Callbacks
	private final static IServerManagerInterface _iServerManagerServicePlugin = new IServerManagerServicePlugin();
	private final static IServerManagerInterface _iServerManagerEverSyncClient = new IServerManagerEverSyncClient();


	/**
	 * The application main method used for initialization of the plugins and adding them to the server.
	 * It also listens on the server port and spawns handler threads.
	 */
	public static void main (String[] args) throws Exception {
		// For debugging purposes, we start each time with a new, empty database
		// Therefore, delete the existing one
		File dbFile = new File("iserver.db4o");
		dbFile.delete();

		_fileEventHandler.init(
			_pluginManager,
			_clientManager,
			_iServerManagerServicePlugin,
			_iServerManagerEverSyncClient);
		
		log.info("EverSync server started!");
		
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
			InputStream in = clientSocket.getInputStream();
			DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
			Connection connection = new Connection(in, out);

			try {
				_client = _clientManager.handShake(connection);
				_clientID = _client.getId();
			} catch (Exception e) {
				log.info("A client tried to connect without success..");
				e.printStackTrace();
			}
			log.info("Client connected: " + _clientID + " and has now " + _client.getNumberOfConnection() + " connection ("+_client.getOs()+").");
		}


		public void run() {
			// If the client has stream connection(s) then it means that this thread has been started by a creation
			// of a new stream connection. In this case, we don't go to the listening loop because additional stream 
			// connections are only used to stream files, not to listen for messages.
			if(_client.hasStreamConnections()) {
				_client.streamData();
				return;
			} else {
				// While the client is connected, listen to its messages.
				while (_clientManager.checkConnection(_clientID)) {
					try {
						Message msg = _client.getMsg();
						String msgType = msg.getMsgType();
						switch(msgType) {
	//					case "Sync Request":
	//						synchronize(_client, msg);
	//							break;
						case "Normal Request":
							_messageReflect.parseMessage(_client, msg);
							break;
						default:
							log.severe("Received unsupported message type: '"+ msg.getMsgType() +"' from client: " + _clientID);
						}
	
					} catch (Exception e) {
						// Handle the disconnection of a client.
						_clientManager.disconnected(_client);
						log.info("Client disconnected: " + _clientID);
						break;
					}
				}
			}
		}
	}
} 