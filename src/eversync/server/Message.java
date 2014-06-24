package eversync.server;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class Message {
	
	private JSONObject _MSG;
	
	public Message() {
		JSONObject json = new JSONObject();
		_MSG = json;
	}
	
	public Message(JSONObject json) {
		_MSG = json;
	}
	
	public Message(String receivedMsg) throws Exception {
		try {
			JSONObject json = new JSONObject(receivedMsg);
			_MSG = json;
		} catch (JSONException e) {
			throw new Exception("Could not create a new message object from the received message from the client."); 
		}
	}

	public String toString() {
		return _MSG.toString();
	}
	
	public void setKeyValue(String key, String value) {
		try {
			_MSG.put(key, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void setKeyValue(String key, JSONObject value) {
		try {
			_MSG.put(key, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * To read one single value corresponding to a key in the message.
	 */
	public String getValue(String key) throws Exception {
		String value;
		try {
			value = (String) _MSG.getString(key);
		} catch (JSONException e) {
			throw new Exception("Such key does not exist: " + key);
		}
		return value;
	}
	
	/**
	 * To read nested messages.
	 */
	public Message getValues(String key) throws Exception {
		JSONObject value = _MSG.getJSONObject(key);
		Message msg = new Message(value){};
		return msg;		
	}
	
	public String getMsgType() throws Exception {
		return this.getValue("msgType");
	}

	/**
	 * Subclasses of the Message class. 
	 * @author Evgeni
	 *
	 */
	public static class HandshakeRequest extends Message {
		public HandshakeRequest() {
			super.setKeyValue("msgType", "Handshake Request");
		}
	}
	
	public static class InstallRequest extends Message {
		public InstallRequest() {
			super.setKeyValue("msgType", "Client Installation Request");
		}
	}
	
	public static class InstallAcknowledgement extends Message {
		public InstallAcknowledgement(String clientId, String rootPath) {
			super.setKeyValue("msgType", "Client Installation Acknowledgement");
			super.setKeyValue("clientId", clientId);
			super.setKeyValue("rootPath", rootPath);
		}
	}
	
	public static class NormalResponse extends Message {
		public NormalResponse() {
			super.setKeyValue("msgType", "Normal Response");
		}
	}
	
	public static class SyncResponse extends Message {
		public SyncResponse() {
			super.setKeyValue("msgType", "Sync Response");
		}
	}
}
