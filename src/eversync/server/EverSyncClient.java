package eversync.server;

import java.io.IOException;

public class EverSyncClient {
	
	/**
	 * Private members
	 */
	private final String _ID;
	private String _OS;
	private Connection _conn;
	private String _rootPath;
	
	/**
	 * Constructor
	 * @param OS: String with the name of the operating system of the client
	 */
	public EverSyncClient(String Id) {
		_ID = Id;
	}

	/**
	 * Depending on the operating system of the client, the root path to the synchronisation folder will be different
	 */
	private void setRootPath() {
		switch (_OS) {
		case "Windows":
			_rootPath = "%userprofile%\\documents";
			break;
		case "MacIntel":
			_rootPath = "~/Documents";
			break;

		default:
			throw new RuntimeException("Operating system of the client not supported: " + _OS);
		}
	}
	
	/**
	 * Getters
	 */
	public String getRootPath() {
		return _rootPath;
	}
	
	public String getId() {
		return _ID;
	}
	
	
	/**
	 * Setters
	 */
	public void setConn(Connection conn) {
		_conn = conn;
	}

	public void setOs(String os) {
		_OS = os;
		setRootPath();
	}
	
	
	/**
	 * Methods for sending and receiving messages from the client.
	 * @throws IOException Two possible cases for exceptions: for being disconnected or if the received message could not be parsed. 
	 */
	public Message getMsg() throws IOException {
		Message msg = _conn.getMsg();
		return msg;
	}
	
	public void sendMsg(Message msg) {
		_conn.sendMsg(msg);
	}
}