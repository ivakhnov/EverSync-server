package eversync.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import eversync.server.Message;

public class Connection {
	
	private BufferedReader _in;
	private DataOutputStream _out;
	
	public Connection(BufferedReader in, DataOutputStream out) {
		_in = in;
		_out = out;
	}
	
	public Message getMsg() throws IOException {
		Message msg;
		try {
			msg = new Message(_in.readLine()){};
		} catch (IOException e) {
			throw new IOException("Could not read from the input stream."); 
		} catch (Exception e) {
			throw new IOException("Message could not be parsed.");
		}
		return msg;
	}
	
	public void sendMsg(Message msg) {
		try {
			_out.writeBytes(msg.toString() + '\n');
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
