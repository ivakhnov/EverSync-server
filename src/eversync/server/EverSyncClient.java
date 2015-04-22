package eversync.server;

import java.io.IOException;
import java.util.Hashtable;
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
	private Queue<byte[]> _pushDataToClient;
	private Queue<Message> _messageQueue;
	private Hashtable<String, byte[]> _pulledDataFromClient = new Hashtable<String, byte[]>();

	/**
	 * Constructor
	 * @param OS: String with the name of the operating system of the client
	 */
	public EverSyncClient(String Id) {
		_ID = Id;
		_streamConns = new LinkedList<Connection>();
		_pushDataToClient = new LinkedList<byte[]>();
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

	public void sendFile(Message downloadReq, byte[] file) {
		_pushDataToClient.add(file);

		if (isConnected()) {
			sendMsg(downloadReq);
		} else {
			_messageQueue.add(downloadReq);
		}
	}

	public void parseMessage(Message msg) throws Exception {
		switch(msg.getMsg()) {
		case "Download Acknowledgement":
			pushDataToClient();;
			break;
		case "File Upload Preparation":
			int fileSize = Integer.parseInt(msg.getValue("fileSize"));
			String filePath = msg.getValue("filePath");
			pullDataFromClient(fileSize, filePath);
			break;
		default :
			log.severe("Received unsupported message: '"+ msg.getMsg());
		}
	}

	/**
	 * For each file that has to be sent/broadcasted by a client with other clients, and for each
	 * file that has to be downloaded by a client from somewhere else, the client opens a new socket connection.
	 * All the additional open connections of a client are added to the stream connections queue. Every file
	 * that has to be pushed to a client is also kept in a queue, namely the stream data queue. 
	 */
	private void pushDataToClient() {
		Connection streamConn = _streamConns.poll();
		byte[] file = _pushDataToClient.poll();
		streamConn.sendByteArray(file);
		streamConn.closeConn();
	}
	
	private void pullDataFromClient(int fileSize, String filePath) throws IOException {
		Connection streamConn = _streamConns.poll();
		byte[] fileByteArray = streamConn.getByteArray(fileSize);
		_pulledDataFromClient.put(filePath, fileByteArray);
		streamConn.closeConn();
	}
	
	/**
	 * Method intended to check whether a file is already pulled from client
	 * which would mean that it's ready from broadcasting
	 * @param filePath
	 */
	public boolean isFilePulled(String filePath) {
		return _pulledDataFromClient.containsKey(filePath);
	}
	
	public byte[] getPulledFileContent(String filePath) {
		byte[] file = _pulledDataFromClient.get(filePath);
		_pulledDataFromClient.remove(filePath);
		return file;
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