package eversync.plugins;

import eversync.iServer.IServerManagerInterface;
import eversync.server.FileEventHandler;

public interface Plugin {
		
	public String getPluginName();
	
	public String getExtensionName();
	
	public String getIconName();
	
	public void init(FileEventHandler fileEventHandler);
	
	public void run();
}
