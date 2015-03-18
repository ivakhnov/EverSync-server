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
	private Queue<Message> _messageQueue;

	/**
	 * Constructor
	 * @param OS: String with the name of the operating system of the client
	 */
	public EverSyncClient(String Id) {
		_ID = Id;
		_streamConns = new LinkedList<Connection>();
		_streamData = new LinkedList<byte[]>();
		_messageQueue = new LinkedList<Message>();
	}

	/**
	 * The path to the folder on the client which will be synchronized.
	 */
	private void setRootPath() {
		_rootPath = "~/EverSync_folder";
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
		_conn.closeConn();
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
	
	public String getOs() {
		return _OS;
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
		} else {
			_messageQueue.add(downloadReq);
		}
	}

	public void streamData() {
		Connection streamConn = _streamConns.poll();
		byte[] file = _streamData.poll();
		streamConn.sendByteArray(file);
		streamConn.closeConn();
	}
	
	/**
	 * The client is connected, so check if it has missed something important.
	 * Check the queue of messages he didn't receive.
	 */
	public void redeemMessages() {
		while (_messageQueue.peek() != null) {
			Message msg = _messageQueue.poll();
			sendMsg(msg);
		}
	}
	
	// For debugging purposes
	public int getNumberOfConnection() {
		int res = 0;
		if(_conn != null) { res++; };
		res += _streamConns.size();
		return res;
	}
}