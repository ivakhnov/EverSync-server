package eversync.plugins;

import java.util.HashMap;

import eversync.server.EverSyncClient;
import eversync.server.FileEventHandler;

public interface PluginInterface {
		
	public String getPluginName();

	// Reads installation files into byte arrays and returns them 
	// all in a List so that they can be sent and installed on a client.
	public HashMap getInstallationFiles() throws Exception;
	
	// Replaces an existing file on a remote service with new version.
	public void replaceFile(String fileName, String fileUri, byte[] fileByteArray);
	
	// A client can ask a client to open a file.
	// Depending on the plugin implementation, plugins can then copy the actual file
	// to the file and open it with the OS default associated program, or they can 
	// open a web url in the webbrowser of the client.
	public void handleOpenOnClientRequest(EverSyncClient client, String uri);
	
	public void init(FileEventHandler fileEventHandler);
	
	public void run();
	
	public void pollForChanges();
}
