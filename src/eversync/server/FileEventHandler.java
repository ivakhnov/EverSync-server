package eversync.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.st.iserver.DigitalObject;
import org.st.iserver.Entity;

import eversync.iServer.IServerManagerInterface;
import eversync.plugins.PluginManager;
import eversync.server.Message.*;

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
		// Collect all the linked entities
		JSONArray clientFiles = _iServerManagerEverSyncClient.getLinkedFiles(filePath);
		results.put("MyDevices", clientFiles);
		/*
		 * for (each plugin) {
		 * 		string pluginname = plugin.getname
		 * 		iservermanagerserviceplugin.getLinkedfiles
		 * 		
		 * }
		 */
		
		return results;
	}

	public void addFile(EverSyncClient client, String fileName, String filePath) {
		System.out.println("--- SYNC addFile ---");
		System.out.println("clientID: " + client.getId());
		System.out.println("filePath: " + filePath);
		System.out.println("--- SYNC addFile ---");
		System.out.println("");
		
		_iServerManagerEverSyncClient.addAndLinkFile(client.getId(), fileName, filePath);
		
		//Prepare and send a response message
		SyncResponse syncResp = new SyncResponse();
		client.sendMsg(syncResp);
	}
	// TODO implement the same method for plugins
	
	public void modifyFile(EverSyncClient client, String fileName, String filePath) throws Exception {
		System.out.println("server: File modification registered: "+filePath);
		UploadRequest uploadReq = new UploadRequest(filePath);
		
		client.sendMsg(uploadReq);
		Message res = client.getMsg();
		int fileSize = Integer.parseInt(res.getValue("fileSize"));
		
		byte[] fileByteArray = client.getFile(fileSize);
		
		// Collect all the linked entities to be updated
		JSONArray clientFiles = _iServerManagerEverSyncClient.getLinkedFiles(filePath);
		for (int x = 0; x < clientFiles.length(); x++) {
			JSONObject file = clientFiles.getJSONObject(x);
			String receiverId = file.getString("hostId");
			String localFilePath = file.getString("uri");
			String name = file.getString("name");
			
			// Skip the "uploader"
			if (receiverId == client.getId()) {
				System.out.println("Skip one receiver");
				continue;
			} else {
				System.out.println("Send to one receiver");
				// Download request is needed to ask a client to be prepared to download a file from the server
				DownloadPreparation downloadPrep = new DownloadPreparation(fileSize, localFilePath, name);
				
				EverSyncClient clientToUpdate = _clientManager.getClient(receiverId);
				clientToUpdate.sendFile(downloadPrep, fileByteArray);
			}
		}
	}
}
