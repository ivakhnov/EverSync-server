package eversync.plugins.Facebook;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Version;

import eversync.plugins.Plugin;
import eversync.plugins.PluginInterface;
import eversync.server.FileEventHandler;

public class FacebookPlugin extends Plugin implements PluginInterface {
	
	private FacebookClient _fbClient;
	
	/**
	 * Constructor
	 * 
	 */
	public FacebookPlugin(String token) throws Exception {
		super._pluginName = "Facebook";
		
		_fbClient = new DefaultFacebookClient(token, Version.UNVERSIONED);
	}
	
	public void pollForChanges() {
		// TODO
	}
	
	/**
	 * Collects all files and notes from the service and adds them to the IServer.
	 */
	private void getAllPhotos() {
		// TODO
		// get all notes in list
		// iterate over photos, take one per one
		// get all files (resources is the name?) of that photo
		// add it to the IServer, link them together and search for local files with relevant names
		// since each file will be linked with a relevant local photo, eventually all the remote files on different services
		// will also get linked via indirect links
		// direct links => to local files ==> indirect among remote files
	}

	/**
	 * Intialize. During this step, we
	 * authenticate with the Facebook web service. All of the code could be boilerplate from Facebook tutorials
	 */
	@Override
	public void init(FileEventHandler fileEventHandler) {
		super._fileEventHandler = fileEventHandler;
		// Some additional functionality if needed.
	}

	@Override
	public void run() {
		try {
			getAllPhotos();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}