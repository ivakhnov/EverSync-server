package eversync.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.UUID;

import eversync.server.Message.HandshakeRequest;
import eversync.server.Message.InstallAcknowledgement;
import eversync.server.Message.InstallRequest;

public class EverSyncClientManager {

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
	 * When a new client is added to the system, an object of the EverSyncClient has to be instantiated with it.
	 * Moreover, the client receives an id, and some additional data of the plugin's installed in the EverSync (on the server)
	 * is pushed to the client.
	 */
	private void installNewClient(EverSyncClient client, Connection conn) throws Exception {
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
	 * When a new client tries to connect to the client, they both have to handshake.
	 * The client says if it's is a new client (has no id assigned yet).
	 * If so, the installNewClient method is triggered in order to install the new client on the server, and assign an id.
	 * Finally, after the handshake, the client ID is added to the set _connectedClients.
	 * The return value is the connected client object.
	 */
	public EverSyncClient handShake(Connection conn) throws Exception {
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
		// Associate the connection with the client
		client.setConn(conn);
		// Bookkeeping
		_connectedClients.add(client.getId());
		
		return client;
	}

	/**
	 * To whether whether the client with a particular ID is currentl connected to the server.
	 * @param clientID
	 * @return
	 */
	public boolean checkConnection(String clientID) {
		return _connectedClients.contains(clientID);
	}

	/**
	 * This method has to be called when a disconnection has been detected. Client disconnects,
	 * it has to be removed from the list of connected clients.
	 * @param clientID
	 */
	public void disconnected(String clientID) {
		_connectedClients.remove(clientID);
	}
}
