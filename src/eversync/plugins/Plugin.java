package eversync.plugins;

public interface Plugin {
		
	public String getPluginName();
	
	public String getExtensionName();
	
	public String getIconName();
	
	public void init();
	
	public void run();
}
