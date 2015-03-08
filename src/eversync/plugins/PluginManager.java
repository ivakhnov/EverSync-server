package eversync.plugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import eversync.iServer.IServerManagerInterface;
import eversync.iServer.IServerManagerServicePlugin;
import eversync.plugins.Evernote.EvernotePlugin;
import eversync.plugins.Facebook.FacebookPlugin;
import eversync.plugins.Flickr.FlickrPlugin;
import eversync.server.FileEventHandler;
import eversync.server.Server;

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

//		log.info("-- Initializing Evernote plugin ..."); // login: ivakhnovpim
//		EvernotePlugin evernote = new EvernotePlugin("S=s1:U=8de6c:E=14b6ad2d7e3:C=1441321abe6:P=1cd:A=en-devtoken:V=2:H=1d6bfa88d4e429fe3918e4584561e51e");
//		installPlugin("Evernote", evernote);
//		
//		log.info("-- Initializing Facebook plugin ...");
//		FacebookPlugin facebook = new FacebookPlugin("testToken");
//		installPlugin("Facebook", facebook);
		
		log.info("-- Initializing Flickr plugin ...");
		FlickrPlugin flickr = new FlickrPlugin(
				"ddbf3a3f6229d256481d4aeabaa99a63", 
				"ca346c4fa57f2106",
				"72157650764299927-66c79182ad3b0d5e",
				"8a38b15c114110ce");
		installPlugin("Flickr", flickr);

		log.info("All plugins initialized successfully!");
	}

	/**
	 * Return a list of all the installed plugins
	 * @return
	 */
	public static List<PluginInterface> getAllPlugins() {
		return new ArrayList<PluginInterface>(_plugins.values());
	}
}
