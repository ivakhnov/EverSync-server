package eversync.server;

import java.lang.reflect.Method;

import org.json.JSONException;
import org.json.JSONObject;

import static eversync.iServer.Constants.*;
import eversync.plugins.PluginInterface;
import eversync.plugins.PluginManager;
import eversync.server.Message.DownloadPreparation;
import eversync.server.Message.NormalMessage;
import eversync.server.Message.OpenFileRequest;
import eversync.server.Message.UploadRequest;

public class MessageReflect {

	private static FileEventHandler _fileEventHandler;
	private static EverSyncClientManager _clientManager;
	private static PluginManager _pluginManager;

	public MessageReflect(FileEventHandler fileEventHandler, EverSyncClientManager clientManager, PluginManager pluginManager) {
		_fileEventHandler = fileEventHandler;
		_clientManager = clientManager;
		_pluginManager = pluginManager;
	}

	private void getLinkedFiles(EverSyncClient client, String fileName, String filePath) throws JSONException {
		NormalMessage response = new NormalMessage();
		response.setKeyValue("methodName", "showLinkedItems");

//		JSONObject nestedMsg = new JSONObject();
//
//		JSONArray list = new JSONArray();
//		list.put("msg 1");
//		list.put("msg 2");
//		list.put("msg 3");
//
//		nestedMsg.put("FileSystem", list);
//		nestedMsg.put("evernote", list);
//		nestedMsg.put("facebook", list);
		
		JSONObject linkedFiles = _fileEventHandler.getLinkedFiles(client, fileName, filePath);

		response.setKeyValue("items", linkedFiles);
		client.sendMsg(response);
	}
	
	private void getLocalFilesByName(EverSyncClient client, String fileName) throws JSONException {
		NormalMessage response = new NormalMessage();
		response.setKeyValue("methodName", "showLinkedItems");
		JSONObject localFiles = _fileEventHandler.getLocalFilesByName(client, fileName);

		response.setKeyValue("items", localFiles);
		client.sendMsg(response);
	}

	private void openRemotely(String hostId, String filePath) {
		EverSyncClient client = _clientManager.getClient(hostId);
		OpenFileRequest req = new OpenFileRequest(filePath);
		client.sendMsg(req);
	}
	
	private void copyFromRemoteAndOpen(EverSyncClient receiverClient, String hostId, String fileUri, String fileName) throws NumberFormatException, Exception {
		UploadRequest uploadReq = new UploadRequest(fileUri);
		EverSyncClient hostClient = _clientManager.getClient(hostId);
		hostClient.sendMsg(uploadReq);
		
		while(!hostClient.isFilePulled(fileUri))
		{
			Thread.sleep(1000);
		}
		
		byte[] fileByteArray = hostClient.getPulledFileContent(fileUri);
		
		// Download request is needed to ask a client to be prepared to download a file from the server
		DownloadPreparation downloadPrep = new DownloadPreparation(fileByteArray.length, fileName, fileUri);
		receiverClient.sendFile(downloadPrep, fileByteArray);
	};
	
	private void askPluginToOpen(EverSyncClient client, String hostType, String hostId, String uri) {
		if (hostType.equals(EVERSYNC_CLIENT))
			return;
		
		PluginInterface plugin = _pluginManager.get(hostId);
		plugin.handleOpenOnClientRequest(client, uri);
	}
	
	private void linkTwoFiles(EverSyncClient client, String localFileName, String hostId, String remoteFileName) {
		PluginInterface plugin = _pluginManager.get(hostId);
		_fileEventHandler.linkTwoFiles(client, localFileName, plugin, remoteFileName);
	}

	public void parseMessage(EverSyncClient client, Message message) {
		System.out.println("Parsing the message...");
		System.out.println(message);

		// Message dispatcher, i.e. using Java Reflection to call a method given its name (simple JSON-RPC)
		try {

			Message params = message.getValues("params");

			String methodName = message.getValue("methodName");
			Method method = null;

			// For safety reasons, use some kind of indirection through a switch.
			switch (methodName) {
			case "getLinkedFiles": {
				String fileName = params.getValue("fileName");
				String filePath = params.getValue("filePath");
				getLinkedFiles(client, fileName, filePath);
				}
				break;
			case "getLocalFilesByName": {
				String fileName = params.getValue("fileName");
				getLocalFilesByName(client, fileName);
				}
				break;
			case "addFile": {
				String fileName = params.getValue("fileName");
				String filePath = params.getValue("filePath");
				String lastModified = params.getValue("lastModified");
				_fileEventHandler.addFile(client, fileName, filePath);
				}
				break;
			case "deleteFile": {
				String fileName = params.getValue("fileName");
				String filePath = params.getValue("filePath");
				String lastModified = params.getValue("lastModified");
				//deleteFile(client, filePath, lastModified);
				}
				break;
			case "modifyFile": {
				String fileName = params.getValue("fileName");
				String filePath = params.getValue("filePath");
				_fileEventHandler.modifyFile(client, fileName, filePath);
				}
				break;
			case "openRemotely": {
				String hostId = params.getValue("hostId");
				String filePath = params.getValue("filePath");
				openRemotely(hostId, filePath);
				}
				break;
			case "copyFromRemoteAndOpen": {
				String hostId = params.getValue("hostId");
				String fileUri = params.getValue("fileUri");
				String fileName = params.getValue("fileName");
				copyFromRemoteAndOpen(client, hostId, fileUri, fileName);
				}
				break;
			case "askPluginToOpen": {
				String hostType = params.getValue("hostType");
				String hostId = params.getValue("hostId");
				String uri = params.getValue("uri");
				askPluginToOpen(client, hostType, hostId, uri);
				}
				break;
			case "linkTwoFiles": {
				String localFileName = params.getValue("localFileName");
				String remoteFileName = params.getValue("remoteFileName");
				String hostId = params.getValue("hostId");
				linkTwoFiles(client, localFileName, hostId, remoteFileName);
				}
				break;
			default:
				throw new NoSuchMethodException();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
