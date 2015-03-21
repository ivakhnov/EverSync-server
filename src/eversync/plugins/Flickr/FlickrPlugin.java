package eversync.plugins.Flickr;

import java.util.Date;
import java.util.Set;
import java.util.logging.Logger;

import org.scribe.model.Token;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.auth.AuthInterface;
import com.flickr4java.flickr.auth.Permission;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;

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
		super._pluginName = "Flickr";
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
	 * @throws FlickrException 
	 */
	private void getAllPhotos() throws FlickrException {
		String userId = _auth.getUser().getId();
		String safeSearch = null;
		Date minUploadDate = null;
		Date maxUploadDate = null;
		Date minTakenDate = null;
		Date maxTakenDate = null;
		String contentType = "7"; // Content Type setting: 7 for photos, screenshots, and 'other' (= all).
		String privacyFilter = null;
		Set<String> extras = null;
		int perPage = 500; // The max value allowed by Flickr API
		int page = 1; // No pagination implemented yet in this plugin
		
		PhotoList <Photo> photos = _flickr.getPeopleInterface().getPhotos(userId, safeSearch, minUploadDate, maxUploadDate, minTakenDate, 
				maxTakenDate, contentType, privacyFilter, extras, perPage, page);
		
		for (Photo photo : photos) {
			String fileName = photo.getTitle();
			String fileId = photo.getId();
			//fileId += "." + "FlickrComment";
			super.addFile(fileName, fileId);
		}
	}
	
	@Override
	public void replaceFile(String fileName, String fileUri, byte[] fileByteArray) {
		Boolean async = true;
		try {
			//_flickr.
			_flickr.getUploader().replace(fileByteArray, fileUri, async);
		} catch (FlickrException e) {
			// TODO Auto-generated catch block
			log.severe("Unable to replace the photo with id: " + fileUri);
			e.printStackTrace();
		}
	}
	
	private void warnAppRegistration() {
		AuthInterface authInterface = _flickr.getAuthInterface();
		
		Token token = authInterface.getRequestToken();
		//log.info("token: " + token);
		
		String url = authInterface.getAuthorizationUrl(token, Permission.DELETE);
		log.severe("Follow this URL to authorise yourself on Flickr");
		log.severe(url);
		log.severe("Insert the token it gives you into configuration of this app!");
	}

	/**
	 * Intialize with the file event handler
	 */
	@Override
	public void init(FileEventHandler fileEventHandler) {
		super._fileEventHandler = fileEventHandler;
		// Some additional functionality if needed.
		try {
			getAllPhotos();
		} catch (FlickrException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
