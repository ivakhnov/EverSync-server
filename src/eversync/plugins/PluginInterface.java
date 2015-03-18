package eversync.plugins;

import java.util.HashMap;

import eversync.server.FileEventHandler;

public interface PluginInterface {
		
	public String getPluginName();

	// Reads installation files into byte arrays and returns them 
	// all in a List so that they can be sent and installed on a client.
	public HashMap getInstallationFiles() throws Exception;
	
	// Replaces an existing file on a remote service with new version.
	public void replaceFile(String fileName, String fileUri, byte[] fileByteArray);
	
	public void init(FileEventHandler fileEventHandler);
	
	public void run();
}
