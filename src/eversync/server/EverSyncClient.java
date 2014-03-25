package eversync.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class EverSyncClient {
	
	/**
	 * Private members
	 */
	private final String _ID;
	private String _OS;
	private BufferedReader _in;
	private PrintWriter _out;
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
	public void setIn(BufferedReader in) {
		_in = in;
	}
	
	public void setOut(PrintWriter out) {
		_out = out;
	}
	
	public String getMsg() throws IOException {
		return _in.readLine();
	}
	
	public void sendMsg(String msg) {
		_out.println(msg);
	}

	public void setOs(String os) {
		_OS = os;
		setRootPath();
	}
}