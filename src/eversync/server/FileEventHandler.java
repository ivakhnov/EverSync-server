package eversync.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.st.iserver.DigitalObject;

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
	
	public void getLinkedFiles(EverSyncClient client, String fileName, String filePath) {
		_iServerManagerEverSyncClient.getLinkedFiles();
	}

	public void addFile(EverSyncClient client, String fileName, String filePath) {
		System.out.println("--- SYNC addFile ---");
		System.out.println("clientID: " + client.getId());
		System.out.println("filePath: " + filePath);
		System.out.println("--- SYNC addFile ---");
		System.out.println("");
		
		_iServerManagerEverSyncClient.addFile(client.getId(), fileName, filePath);
		// TODO
		// detect existing files with the same name
		// normally each client and/or service has at most one file with the same name and path
		// link the new file with the existing files (i.e. on each client or service)
		
		//Prepare and send a response message
		SyncResponse syncResp = new SyncResponse();
		client.sendMsg(syncResp);
	}
	// TODO implement the same method for plugins
	
	public void modifyFile(EverSyncClient client, String fileName, String filePath) throws Exception {
		UploadRequest uploadReq = new UploadRequest(filePath);
		
		client.sendMsg(uploadReq);
		Message res = client.getMsg();
		int fileSize = Integer.parseInt(res.getValue("fileSize"));
		
		byte[] fileByteArray = client.getFile(fileSize);
		
		// TODO read a list of linked files which have to be updated 
		// => ONLY with property "hostType" = "EverSyncClient"
//		for (DigitalObject fileObject : linkedFiles) {
//			String clientId = fileObject.getProperty("hostId").getValue();
//			String localFilePath = fileObject.getUri();
//			DownloadRequest downloadReq = new DownloadRequest(localFilePath, fileSize);

		String clientId = client.getId();
		String localFilePath = filePath.substring(0, filePath.length()-4) + "TEST.txt";
		// Download request is needed to ask a client to be prepared to download a file from the server
		DownloadRequest downloadReq = new DownloadRequest(localFilePath, fileSize);

		EverSyncClient clientToUpdate = _clientManager.getClient(clientId);
		clientToUpdate.sendFile(downloadReq, fileByteArray);
	}
}
