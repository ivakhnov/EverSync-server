package eversync.server;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

public class EverSyncClient {
	// Logger for debugging purposes
	private static Logger log = Logger.getLogger(Server.class.getName());
	
	/**
	 * Private members
	 */
	private final String _ID;
	private String _OS;
	private Connection _conn = null;
	private String _rootPath;
	private Queue<Connection> _streamConns;
	private Queue<byte[]> _streamData;

	/**
	 * Constructor
	 * @param OS: String with the name of the operating system of the client
	 */
	public EverSyncClient(String Id) {
		_ID = Id;
		_streamConns = new LinkedList<Connection>();
		_streamData = new LinkedList<byte[]>();
	}

	/**
	 * Depending on the operating system of the client, the root path to the synchronisation folder will be different
	 */
	private void setRootPath() {
		switch (_OS) {
		case "Win32":
			_rootPath = "~\\documents";
			break;
		case "Mac":
			//_rootPath = "~/Documents";
			_rootPath = "~/EverSync_folder";
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
	
	public Boolean isConnected() {
		return (_conn != null);
	}

	public Boolean hasStreamConnections() {
		return (_streamConns.size() != 0);
	}

	/**
	 * Setters
	 */
	public void setConn(Connection conn) {
		_conn = conn;
	}

	public void resetConn() {
		_conn = null;
	}

	/**
	 * Besides the regular connection with the server, a client can create multiple temporary connections to stream files.
	 * @param conn
	 */
	public void addStreamConnection(Connection conn) {
		_streamConns.add(conn);
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

	/**
	 * Reads a file from the actual client as a byteArray (i.e. upload to server)
	 * @param fileSize
	 * @return
	 * @throws IOException 
	 */
	public byte[] getFile(int fileSize) throws IOException {
		return _conn.getByteArray(fileSize);
	}

	public void sendFile(Message downloadReq, byte[] file) {
		_streamData.add(file);

		if (isConnected()) {
			sendMsg(downloadReq);
		}
	}

	public void streamData() {
		Connection streamConn = _streamConns.poll();
		byte[] file = _streamData.poll();
		streamConn.sendByteArray(file);
	}
}