package eversync.server;

import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eversync.plugins.PluginInterface;

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
	
	public void setKeyValue(String key, JSONArray value) {
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
	public static class EmptyMessage extends Message {
		public EmptyMessage() {}
	}

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
		public InstallAcknowledgement(String clientId, String rootPath, List<PluginInterface> plugins) {
			super.setKeyValue("msgType", "Client Installation Acknowledgement");
			super.setKeyValue("clientId", clientId);
			super.setKeyValue("rootPath", rootPath);
			
			JSONObject installFiles = new JSONObject(); 
			try {
				for (PluginInterface plugin : plugins) {
					installFiles.put(plugin.getPluginName(), plugin.getInstallationFiles());
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			super.setKeyValue("installationFiles", installFiles);
		}
	}
	
	public static class NormalMessage extends Message {
		public NormalMessage() {
			super.setKeyValue("msgType", "Normal Message");
		}
	}
	
	public static class SyncResponse extends Message {
		public SyncResponse() {
			super.setKeyValue("msgType", "Sync Response");
		}
	}

	public static class UploadRequest extends Message {
		public UploadRequest(String filePath) {
			super.setKeyValue("msgType", "Normal Message");
			super.setKeyValue("methodName", "uploadFile");
			super.setKeyValue("filePath", filePath);
		}
	}

	public static class DownloadPreparation extends Message {
		public DownloadPreparation(int fileSize, String fileName, String filePath) {
			super.setKeyValue("msgType", "Download Preparation");
			super.setKeyValue("filePath", filePath);
			super.setKeyValue("fileName", fileName);
			super.setKeyValue("fileSize", Integer.toString(fileSize));
		}
	}
	
	public static class OpenFileRequest extends Message {
		public OpenFileRequest(String filePath) {
			super.setKeyValue("msgType", "Normal Message");
			super.setKeyValue("methodName", "openFile");
			super.setKeyValue("filePath", filePath);
		}
	}
	
	public static class InstalledClientsNotification extends Message {
		public InstalledClientsNotification(Set<String> clientsSet) {
			super.setKeyValue("msgType", "Normal Message");
			super.setKeyValue("methodName", "setInstalledClients");
			JSONArray clientsArray = new JSONArray();
			for (String clientId : clientsSet) {
				clientsArray.put(clientId);
			}
			super.setKeyValue("clients", clientsArray);
		}
	}
	
	public static class ConnectedClientsNotification extends Message {
		public ConnectedClientsNotification(Set<String> clientsSet) {
			super.setKeyValue("msgType", "Normal Message");
			super.setKeyValue("methodName", "setConnectedClients");
			JSONArray clientsArray = new JSONArray();
			for (String clientId : clientsSet) {
				clientsArray.put(clientId);
			}
			super.setKeyValue("clients", clientsArray);
		}
	}
	
	public static class OpenUrlInBrowserRequest extends Message {
		public OpenUrlInBrowserRequest(String url) {
			super.setKeyValue("msgType", "Normal Message");
			super.setKeyValue("methodName", "openUrlInBrowser");
			super.setKeyValue("url", url);
		}
	}
}
