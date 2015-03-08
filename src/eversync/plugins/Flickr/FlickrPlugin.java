package eversync.plugins.Flickr;

import java.util.logging.Logger;

import org.scribe.model.Token;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.auth.AuthInterface;
import com.flickr4java.flickr.auth.Permission;

import eversync.plugins.Plugin;
import eversync.plugins.PluginInterface;
import eversync.server.FileEventHandler;

public class FlickrPlugin extends Plugin implements PluginInterface {

	private static Logger log = Logger.getLogger(FlickrPlugin.class.getName());
	private Flickr _flickr = null;
	private Auth _auth = null;
	
	/**
	 * Constructor.
	 * During this step, we authenticate with the FLickr web service.
	 */
	public FlickrPlugin(String apiKey, String sharedSecret, String token, String tokenSecret) throws Exception {
		super._pluginName = "FLickr";
		this._flickr = new Flickr(apiKey, sharedSecret, new REST());
		
		try {
			AuthInterface authInterface = _flickr.getAuthInterface();
			this._auth = authInterface.checkToken(token, tokenSecret);
			this._flickr.setAuth(_auth);
		} catch (Exception e) {
			warnAppRegistration();
		}
	}
	
	public void pollForChanges() {
		// TODO
	}
	
	/**
	 * Collects all files and notes from the service and adds them to the IServer.
	 */
	private void getAllPhotos() {
		
	}
	
	private void warnAppRegistration() {
		AuthInterface authInterface = _flickr.getAuthInterface();
		
		Token token = authInterface.getRequestToken();
		//log.info("token: " + token);
		
		String url = authInterface.getAuthorizationUrl(token, Permission.DELETE);
		log.warning("Follow this URL to authorise yourself on Flickr");
		log.warning(url);
		log.warning("Insert the token it gives you into configuration of this app!");
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
