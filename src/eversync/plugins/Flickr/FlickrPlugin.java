package eversync.plugins.Flickr;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
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
import com.flickr4java.flickr.photos.PhotoUrl;
import com.flickr4java.flickr.photos.PhotosInterface;
import com.flickr4java.flickr.photos.comments.Comment;
import com.flickr4java.flickr.photos.comments.CommentsInterface;
import com.restfb.experimental.api.impl.CommentsImpl;
import com.restfb.types.Comment.Comments;

import eversync.plugins.Plugin;
import eversync.plugins.PluginInterface;
import eversync.server.EverSyncClient;
import eversync.server.FileEventHandler;

public class FlickrPlugin extends Plugin implements PluginInterface {

	private static Logger log = Logger.getLogger(FlickrPlugin.class.getName());
	private Flickr _flickr = null;
	private Auth _auth = null;
	
	private final String COMMENT_LABEL = "FlickrComment";
	
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
			collectComments(photo);
		}
	}
	
	private void collectComments(Photo photo) {
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		String photoName = photo.getTitle();
		String photoId = photo.getId();
		//fileId += "." + "FlickrComment";
		
		CommentsInterface ci = _flickr.getCommentsInterface();
		List<Comment> comments;
		try {
			comments = ci.getList(photoId);
			Iterator<Comment> commentsIterator = comments.iterator();
			
			while (commentsIterator.hasNext()) {
				Comment comment = (Comment) commentsIterator.next();
				String id = constructId(photoId, comment.getId());
				String label = String.join(" - ", comment.getAuthorName(), df.format(comment.getDateCreate()));
				super.addAndLinkFile(photoName, id, label);
			}
		} catch (FlickrException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
	/**
	 * Every plugin has to implement how it will open certain recource on a client.
	 * Flickr plugin opens the comments for a photo by opening that photo in the default webbrowser.
	 * id: id of the comment which was initially inserted in the iServer
	 */
	public void handleOpenOnClientRequest(EverSyncClient client, String id) {
		Photo photo;
		try {
			String photoId = getActualPhotoId(id);
			photo = _flickr.getPhotosInterface().getPhoto(photoId);
			super.openUrlInBrowser(client, photo.getUrl());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String constructId(String photoId, String commentId) {
		return String.join(".", photoId, commentId, COMMENT_LABEL);
	}
	
	private String getActualCommentId(String id) throws Exception {
		Exception ex = new Exception("Unknown id: " + id);
		String[] idElements = id.split("\\.");
		if (idElements.length != 3)
			throw ex;
		
		String label = idElements[idElements.length - 1];
		if (!label.equals(COMMENT_LABEL))
			throw ex;
		
		String commentId = idElements[idElements.length - 2];
		return commentId;
	}
	
	private String getActualPhotoId(String id) throws Exception {
		Exception ex = new Exception("Unknown id: " + id);
		String[] idElements = id.split("\\.");
		if (idElements.length != 3)
			throw ex;
		
		String label = idElements[idElements.length - 1];
		if (!label.equals(COMMENT_LABEL))
			throw ex;
		
		String photoId = idElements[0];
		return photoId;
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
