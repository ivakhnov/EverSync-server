package eversync.server;

import eversync.iServer.IServerManagerInterface;
import eversync.plugins.PluginManager;

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

	public void addFile(EverSyncClient client, String fileName, String filePath, String lastModified) {
		_iServerManagerEverSyncClient.addFile(client.getId(), fileName, filePath, lastModified);
		//detectLinks
	}
	
}
