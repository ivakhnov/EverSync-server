package eversync.plugins;

import java.util.HashMap;

public class PluginManager {
	
	/**
	 * A hash map where the keys are the names of the plugin's and the values are the instances of the plugin's.
	 */
	private static HashMap<String, Plugin> _plugins = new HashMap<String, Plugin>();

	/**
	 * Installation of a plugin consists of initializing it, make it run and 
	 * finally adding it to the HashMap to hold the object.
	 * @param pluginName
	 * @param plugin
	 */
	public void installPlugin(String pluginName, Plugin plugin) {
		plugin.init();
		plugin.run();
		_plugins.put(pluginName, plugin);
	}



	public void installPlugins() throws Exception {
		System.out.println("Initializing plugins ...");

		System.out.println("-- Initializing Evernote plugin ..."); // login: ivakhnovpim
		EvernotePlugin evernote = new EvernotePlugin("S=s1:U=8de6c:E=14b6ad2d7e3:C=1441321abe6:P=1cd:A=en-devtoken:V=2:H=1d6bfa88d4e429fe3918e4584561e51e");
		installPlugin("Evernote", evernote);

		System.out.println("All plugins initialized successfully!");
		
	}

}
