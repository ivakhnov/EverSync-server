package eversync.server;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Connection {
	
	private BufferedReader _inBffReader;
	private BufferedInputStream _inBffStream;
	private DataOutputStream _out;
	
	public Connection(InputStream in, DataOutputStream out) {
		_inBffReader = new BufferedReader(new InputStreamReader(in));
		_inBffStream = new BufferedInputStream(in);
		_out = out;
	}
	
	public Message getMsg() throws IOException {
		Message msg;
		try {
			msg = new Message(_inBffReader.readLine()){};
		} catch (IOException e) {
			throw new IOException("Could not read from the input stream."); 
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException("Message could not be parsed.");
		}
		return msg;
	}
	
	public void sendMsg(Message msg) {
		try {
			_out.writeBytes(msg.toString() + '\n');
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public byte[] getByteArray(int fileSize) throws IOException {
		byte[] readByteArray = new byte[fileSize];
		String stringRepresentation = new String();
		try {
			stringRepresentation = _inBffReader.readLine();
			String[] bytesString = stringRepresentation.split(",");
			for(int i = 0 ; i < readByteArray.length ; ++i) {
				readByteArray[i] = Byte.parseByte(bytesString[i]);
			}
		} catch (IOException e) {
			throw new IOException("Could not read from the input stream.");
		}
		return readByteArray;
	}

	public void sendByteArray(byte[] file) {
		try {
			_out.write(file, 0, file.length);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void closeConn() {
		try {
			_inBffReader.close();
			_inBffStream.close();
			_out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
