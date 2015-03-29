package eversync.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import eversync.plugins.Evernote.EvernotePlugin;
import eversync.plugins.Flickr.FlickrPlugin;
import eversync.server.FileEventHandler;

public class PluginManager {
	// Logger for debugging purposes
	private static Logger log = Logger.getLogger(PluginManager.class.getName());
	
	/**
	 * A hash map where the keys are the names of the plugin's and the values are the instances of the plugin's.
	 */
	private static HashMap<String, PluginInterface> _plugins = new HashMap<String, PluginInterface>();
	
	// TODO description of why
	private final FileEventHandler _fileEventHandler;

	public PluginManager(FileEventHandler fileEventhandler) {
		_fileEventHandler = fileEventhandler;
	}

	/**
	 * Installation of a plugin consists of initializing it, make it run and 
	 * finally adding it to the HashMap to hold the object.
	 * @param pluginName
	 * @param pluginInterface
	 */
	private void installPlugin(String pluginName, PluginInterface pluginInterface) {
		pluginInterface.init(_fileEventHandler);
		pluginInterface.run();
		_plugins.put(pluginName, pluginInterface);
	}

	/**
	 * Install all the plugins on the server.
	 * @throws Exception
	 */
	public void installPlugins() throws Exception {
		log.info("Initializing plugins ...");

		log.info("-- Initializing Flickr plugin ...");
		FlickrPlugin flickr = new FlickrPlugin(
				"ddbf3a3f6229d256481d4aeabaa99a63", 
				"ca346c4fa57f2106",
				"72157650764299927-66c79182ad3b0d5e",
				"8a38b15c114110ce");
		installPlugin("Flickr", flickr);
		
		log.info("-- Initializing Evernote plugin ..."); // login: ivakhnovpim
		EvernotePlugin evernote = new EvernotePlugin("S=s1:U=8de6c:E=1539a0de540:C=14c425cb838:P=1cd:A=en-devtoken:V=2:H=0087ea0c97f17d7b445052d6b6c84ee9");
		installPlugin("Evernote", evernote);
//		
//		log.info("-- Initializing Facebook plugin ...");
//		FacebookPlugin facebook = new FacebookPlugin("testToken");
//		installPlugin("Facebook", facebook);

		log.info("All plugins initialized successfully!");
	}

	/**
	 * Return a list of all the installed plugins
	 * @return
	 */
	public static List<PluginInterface> getAllPlugins() {
		return new ArrayList<PluginInterface>(_plugins.values());
	}
	
	/**
	 * Returns PluginInterface by plugin name
	 * @param pluginName
	 * @return
	 */
	public PluginInterface get(String pluginName) {
		return _plugins.get(pluginName);
	}
}
