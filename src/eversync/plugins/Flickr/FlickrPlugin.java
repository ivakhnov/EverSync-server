package eversync.plugins.Flickr;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.REST;

import eversync.plugins.Plugin;
import eversync.plugins.PluginInterface;
import eversync.server.FileEventHandler;

public class FlickrPlugin extends Plugin implements PluginInterface {

	private Flickr _flickr = null;
	
	/**
	 * Constructor.
	 * During this step, we
	 * authenticate with the FLickr web service. All of the code is boilerplate.
	 */
	public FlickrPlugin(String apiKey, String sharedSecret) throws Exception {
		super._pluginName = "FLickr";
		this._flickr = new Flickr(apiKey, sharedSecret, new REST());
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
	 * Intialize with the file event handler
	 */
	@Override
	public void init(FileEventHandler fileEventHandler) {
		super._fileEventHandler = fileEventHandler;
		// Some additional functionality if needed.
		getAllPhotos();
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
