package eversync.plugins.Facebook;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.evernote.edam.type.User;
import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.DefaultJsonMapper;
import com.restfb.FacebookClient;
import com.restfb.JsonMapper;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.json.JsonArray;
import com.restfb.json.JsonObject;
import com.restfb.types.Album;
import com.restfb.types.Comment;
import com.restfb.types.Photo;

import eversync.plugins.Plugin;
import eversync.plugins.PluginInterface;
import eversync.server.EverSyncClient;
import eversync.server.FileEventHandler;

public class FacebookPlugin extends Plugin implements PluginInterface {
	
	private FacebookClient _fbClient;
	private JsonMapper _jsonMapper = new DefaultJsonMapper();
	
	private String ALBUM_LABEL = "FacebookAlbum";
	private long _lastSyncTime = System.currentTimeMillis() / 1000L;
	private final DateFormat _df = new SimpleDateFormat("dd/MM/yyyy");
	
	/**
	 * Constructor
	 * 
	 */
	public FacebookPlugin(String token) throws Exception {
		super._pluginName = "Facebook";
		
		_fbClient = new DefaultFacebookClient(token, Version.UNVERSIONED);
	}
	
	public void pollForChanges() {
		// Get all new photos
		JsonObject newPhotosJson = _fbClient.fetchObject("photos", JsonObject.class,
				Parameter.with("ids", "10206497753019618"),
				Parameter.with("since", _lastSyncTime));
		
		String photosDataString = newPhotosJson.getJsonObject("10206497753019618").getString("data");
		List<Photo> newPhotos = _jsonMapper.toJavaList(photosDataString, Photo.class);
		if (!newPhotos.isEmpty()) {
			for(Photo photo : newPhotos) {
				String subString = photo.getLink().substring(photo.getLink().lastIndexOf("&set=a.") + 7);
				String albumId = subString.substring(0, subString.indexOf("."));
				Album album = _fbClient.fetchObject(albumId, Album.class);
				
				albumId = String.join(".", albumId, ALBUM_LABEL);
				super.addFile(photo.getId(), photo.getId(), album.getName());
				super.requestClientsToLink(photo.getId(), "photo in album: " + album.getName());
			}
		}
		
		// Get all new comments
		JsonObject newCommentsJson = _fbClient.fetchObject("photos", JsonObject.class,
				Parameter.with("ids", "10206497753019618"),
				Parameter.with("fields", "comments"),
				Parameter.with("since", _lastSyncTime));
		
		JsonArray resultsArray = newCommentsJson.getJsonObject("10206497753019618").getJsonArray("data");
		if (resultsArray.length() > 0) {
			String newCommentsDataString = resultsArray.getJsonObject(0).getJsonObject("comments").getString("data");
			List<Comment> newComments = _jsonMapper.toJavaList(newCommentsDataString, Comment.class);
			if (!newComments.isEmpty()) {
				for(Comment comment : newComments) {
					String photoId = comment.getId().substring(0, comment.getId().indexOf("_"));
					String label = String.join(" - ", comment.getFrom().getName(), _df.format(comment.getCreatedTime()));
					super.addAndLinkFile(photoId, comment.getId(), label);
				}
			}
		}
		
		// Update the sync time
		_lastSyncTime = System.currentTimeMillis() / 1000L;
	}
	
	public void handleOpenOnClientRequest(EverSyncClient client, String id) {
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
		
		// Set up the last sync timestamp for debugging purposes
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		try {
			Date d = sdf.parse("01/01/2015");
			_lastSyncTime = d.getTime() / 1000;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	@Override
	public void replaceFile(String fileName, String fileUri, byte[] fileByteArray) {
		// TODO Auto-generated method stub
		
	}

	
}