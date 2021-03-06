package eversync.server;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eversync.iServer.IServerManagerInterface;
import eversync.plugins.Plugin;
import eversync.plugins.PluginInterface;
import eversync.plugins.PluginManager;
import eversync.server.Message.DownloadPreparation;
import eversync.server.Message.SyncResponse;
import eversync.server.Message.UploadRequest;
import eversync.server.Message.AddToLinkQueueRequest;
import eversync.server.Message.RemoveFromLinkQueueRequest;
import static eversync.iServer.Constants.*;

/**
 * Handles the events by triggering the callbacks in case of a file change on one of the clients
 * or external services. File changes may include file creation, deletion or file update.
 * 
 * @author Evgeni Ivakhnov
 *
 */
public class FileEventHandler {
	
	private static PluginManager _pluginManager;
	private static EverSyncClientManager _clientManager;
	
	private static IServerManagerInterface _iServerManagerServicePlugin;
	private static IServerManagerInterface _iServerManagerEverSyncClient;
	
	public void init(PluginManager pluginManager,
			EverSyncClientManager clientManager,
			IServerManagerInterface iServerManagerServicePlugin,
			IServerManagerInterface iServerManagerEverSyncClient) {
		
		_pluginManager = pluginManager;
		_clientManager = clientManager;
		_iServerManagerServicePlugin = iServerManagerServicePlugin;
		_iServerManagerEverSyncClient = iServerManagerEverSyncClient;
	}
	
	public JSONObject getLinkedFiles(EverSyncClient client, String fileName, String fullPath) throws JSONException {
		String[] path = fullPath.split(":");
		String filePath = path[path.length - 1];
		
		filePath = filePath.substring(filePath.indexOf(':') + 1, filePath.length());
		JSONObject results = new JSONObject();
		// Collect local linked assets
		JSONArray clientFiles = _iServerManagerEverSyncClient.getLinkedFiles(filePath, true);
		if (clientFiles.length() > 0) {
			results.put("MyDevices", clientFiles);
		}
		
		// Collect remote linked assets (on services)
		JSONArray remoteFiles = _iServerManagerServicePlugin.getLinkedFiles(filePath, false);
		for(int x = 0; x < remoteFiles.length(); x++) {
			JSONObject fileObj = remoteFiles.getJSONObject(x);
			String pluginName = fileObj.getString(HOST_ID);
			JSONArray pluginRes = null;
			try {
				pluginRes = results.getJSONArray(pluginName);
			} catch (Exception e) {
				pluginRes = new JSONArray();
			}
			pluginRes.put(fileObj);
			results.put(pluginName, pluginRes);
		}
		
		return results;
	}
	
	public JSONObject getLocalFilesByName(EverSyncClient client, String fileName) throws JSONException {
		JSONObject results = new JSONObject();
		// Collect local linked assets
		JSONArray clientFiles = _iServerManagerEverSyncClient.getFilesByName(fileName);
		if (clientFiles.length() > 0) {
			results.put("MyDevices", clientFiles);
		}
		return results;
	}

	public void addFile(EverSyncClient client, String fileName, String filePath) {
		System.out.println("SYNC addFile from ClientId: " + client.getId() + " filePath: " + filePath);
		System.out.println("");
		
		_iServerManagerEverSyncClient.addAndLinkFile(client.getId(), fileName, filePath, null);
		
		// TODO: search for files with same name on the external services and link them
		List<PluginInterface> plugins =  _pluginManager.getAllPlugins();
		for (PluginInterface plugin : plugins) {
			//_iServerManagerServicePlugin.tryToLinkTo(plugin.getPluginName(), fileName, filePath);
		}
		
		//Prepare and send a response message
		SyncResponse syncResp = new SyncResponse();
		client.sendMsg(syncResp);
	}
	
	public void addAndLinkFile(Plugin plugin, String fileName, String fileId, String fileNameLabel) {
		System.out.println("SYNC addAndLinkFile from Service: " + plugin.getPluginName() + " filePath: " + fileId);
		_iServerManagerServicePlugin.addAndLinkFile(plugin.getPluginName(), fileName, fileId, fileNameLabel);
	}
	
	public String addFile(Plugin plugin, String fileName, String fileId, String fileNameLabel) {
		System.out.println("SYNC addFile from Service: " + plugin.getPluginName() + " filePath: " + fileId);
		return _iServerManagerServicePlugin.addFile(plugin.getPluginName(), fileName, fileId, fileNameLabel);
	}
	
	public void linkFilesDirected(Plugin plugin, String parentFileUri, String childFileUri) {
		System.out.println("SYNC Service: " + plugin.getPluginName() + " links file: " + parentFileUri + " to file: " +childFileUri);
		_iServerManagerServicePlugin.linkFilesDirected(parentFileUri, childFileUri);
	}
	
	public void requestClientsToLink(Plugin plugin, String fileName, String fileLabel) {
		AddToLinkQueueRequest msg = new AddToLinkQueueRequest(plugin, fileName, fileLabel);
		_clientManager.broadcastQueued(msg);
	}
	
	public void linkTwoFiles(EverSyncClient client, String localFileName, PluginInterface plugin, String remoteFileName) {
		RemoveFromLinkQueueRequest msg = new RemoveFromLinkQueueRequest(plugin, remoteFileName);
		
		_iServerManagerEverSyncClient.linkFilesDirectedByName(localFileName, remoteFileName, plugin.getPluginName());
		
		JSONArray clientFiles = _iServerManagerEverSyncClient.getFilesByName(localFileName);
		for (int i = 0; i < clientFiles.length(); i++) {
			try {
				JSONObject file = clientFiles.getJSONObject(i);
				String fileClientId = file.getString(HOST_ID);
				EverSyncClient clnt = _clientManager.getClient(fileClientId);
				clnt.sendMsg(msg);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	public void modifyFile(EverSyncClient client, String fileName, String filePath) throws Exception {
		System.out.println("server: File modification registered: "+filePath);
		
		// Collect all the local (on the clients) linked entities to be updated
		JSONArray clientFiles = _iServerManagerEverSyncClient.getLinkedFiles(filePath, true);
		// All the remotely linked files (on the third party services)
		JSONArray remoteFiles = _iServerManagerServicePlugin.getLinkedFiles(filePath, false);
		
		if (clientFiles.length() == 0 && remoteFiles.length() == 0)
			return;
		
		UploadRequest uploadReq = new UploadRequest(filePath);
		client.sendMsg(uploadReq);
		
		while(!client.isFilePulled(filePath))
		{
			Thread.sleep(1000);
		}
		
		byte[] fileByteArray = client.getPulledFileContent(filePath);
		
		// Then notify all the registered client to update the file (the ones that are offline will get changes
		// pushed when then become available
		for (int x = 0; x < clientFiles.length(); x++) {
			JSONObject file = clientFiles.getJSONObject(x);
			String receiverId = file.getString(HOST_ID);
			String localFilePath = file.getString(FILE_URI);
			String name = file.getString(FILE_NAME);
			
			// Skip the "uploader"
			if (receiverId == client.getId()) {
				System.out.println("Skip one receiver");
				continue;
			} else {
				System.out.println("Send to one receiver");
				// Download request is needed to ask a client to be prepared to download a file from the server
				DownloadPreparation downloadPrep = new DownloadPreparation(fileByteArray.length, localFilePath, name);
				
				EverSyncClient clientToUpdate = _clientManager.getClient(receiverId);
				clientToUpdate.sendFile(downloadPrep, fileByteArray);
			}
		}
		
		// Update those files as well
		for (int i = 0; i < remoteFiles.length(); i++) {
			JSONObject remoteFile = (JSONObject) remoteFiles.get(i);
			// remoteFile looks as follows:
			//{	
			//	"hostType":"EverSyncClient",
			//	"name":"wallpaper-2388706.jpg",
			//	"hostId":"1d79d0a5-5b7a-4d28-a7a3-3234a83cf660",
			//	"uri":"untitled_folder/wallpaper-2388706.jpg"
			//}
			PluginInterface plugin = _pluginManager.get(remoteFile.getString(HOST_ID));
			plugin.replaceFile(remoteFile.getString(FILE_NAME), remoteFile.getString(FILE_URI), fileByteArray);
		}
	}
}
