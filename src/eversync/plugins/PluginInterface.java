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
	// open a web url in the web browser of the client.
	public void handleOpenOnClientRequest(EverSyncClient client, String uri);
	
	// A plugin has to have access to the base EverSync application via the file event handler.
	// This method can also be used for any pre-run initializations such as connection 
	// to the third party service.
	public void init(FileEventHandler fileEventHandler);
	
	// This method is called whenever the whole EverSync server has been initialized, the plugins 
	// have been initialized and everything is ready to run.
	// Mostly this method will be leaved empty, but this gives the possibility to execute
	// any custom code if needed.
	public void run();
	
	public void pollForChanges();
}
