package eversync.iServer;

import static eversync.iServer.Constants.*;

import java.util.HashSet;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sigtec.odi.CardinalityConstraintException;
import org.st.iserver.DigitalObject;
import org.st.iserver.Entity;
import org.st.iserver.IServerCCO;
import org.st.iserver.IServerInterface;
import org.st.iserver.Individual;
import org.st.iserver.util.Property;

import eversync.plugins.Plugin;

public class IServerManagerSuper {

	// Logger for debugging purposes
	private static final Logger log = Logger.getLogger(IServerManagerSuper.class.getName());

	// Singletons
	protected static final IServerInterface _iServer = new IServerCCO();
	private static final Individual _creator = _iServer.createIndividual("EverSync");

	/**
	 * Save a file
	 * @param fileName
	 * @param fileId
	 * @return
	 * @throws CardinalityConstraintException
	 */
	protected DigitalObject addFile(String fileName, String fileId) throws CardinalityConstraintException {
		DigitalObject fileObject = _iServer.createDigitalObject(fileName, fileId, _creator);
		return fileObject;
	}

	protected void linkFilesDirected(DigitalObject source, DigitalObject target) {
		try {
			_iServer.createNavigationalLink("NavigationalLink", source, target, _creator);
		} catch (CardinalityConstraintException e) {
			log.severe("Could not create a link between two files!");
			e.printStackTrace();
		}
	}
	
	public void linkFilesDirected(String parentFileUri, String childFileUri) {
		DigitalObject parentFile = _iServer.getDigitalObjectUrl(parentFileUri);
		DigitalObject childFile = _iServer.getDigitalObjectUrl(childFileUri);
		this.linkFilesDirected(parentFile, childFile);
	}
	
	/**
	 * Converting a DigitalObject to a JSONObject
	 * @param obj
	 * @return Json of the form:
	 * {
	 * 		"name": ##name##,
	 * 		"uri": ##uri##,
	 * 		"XpropertyX" : ##valueForPropertyX##,
	 * 		...
	 * }
	 */
	protected JSONObject digitalObjectToJson(DigitalObject obj) {
		JSONObject result = new JSONObject();
		
		String fileName = obj.getName();
		String fileUri =  obj.getUri();
		String fileLabel = obj.getLabel();
		
		try {
			result.put(FILE_NAME, fileName);
			result.put(FILE_URI, fileUri);
			result.put(FILE_LABEL, fileLabel);
			
			HashSet<Property> propertiesSet = obj.getProperties();
			for(Property prop : propertiesSet) {
				String propKey = prop.getKey();
				String propValue = prop.getValue();
				
				result.put(propKey, propValue);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	private void getLinkedFilesRecursion(Entity startFile, HashSet<Entity> allLinkedFiles){
		allLinkedFiles.add(startFile);
		HashSet<Entity> linkedFiles = startFile.getEntitiesDirectlyLinkedToMe();
		
		if (linkedFiles.isEmpty()) return;
		for (Entity f : linkedFiles) {
			HashSet<Entity> directlyLinked = f.getEntitiesDirectlyLinkedToMe();
			directlyLinked.removeAll(linkedFiles);
			if (!directlyLinked.isEmpty() && !allLinkedFiles.contains(f)) {
				getLinkedFilesRecursion(f, allLinkedFiles);
			}
		}
		return;
	}
	
	/**
	 * Collect all files that are stored on a given host (local client or some service) and that are linked to the file with the given URI.
	 * @param hostType: depends on where the file is store (EverSyncClient or some third party service). 
	 * @param fileURI: can be a file path for the items on a client device and an ID for items from services.
	 * @return
	 */
	protected JSONArray getAllLinkedFilesRecursively(String hostType, String fileURI) {
		HashSet<Entity> allLinkedFiles = new HashSet<Entity>();
		JSONArray results = new JSONArray();
		
		// Just a random file with the given URI (there are possibly multiple files with the same file
		// path which is used as a URI for local files)
		DigitalObject aFile =  _iServer.getDigitalObjectUrl(fileURI);
		getLinkedFilesRecursion(aFile, allLinkedFiles);
		
		for(Entity file : allLinkedFiles) {
			if(file.getProperty(HOST_TYPE).getValue().equals(hostType)) {
				JSONObject propertiesJson = digitalObjectToJson((DigitalObject)file);
				results.put(propertiesJson);
			}
		}
		return results;
	}
	
	protected JSONArray getLinkedFiles(String hostType, String fileURI) {
		JSONArray results = new JSONArray();
		
		DigitalObject aFile =  _iServer.getDigitalObjectUrl(fileURI);
		HashSet<Entity> linkedFiles = aFile.getMyChildren();
		
		for(Entity file : linkedFiles) {
			if(file.getProperty(HOST_TYPE).getValue().equals(hostType)) {
				JSONObject propertiesJson = digitalObjectToJson((DigitalObject)file);
				results.put(propertiesJson);
			}
		}
		return results;
	}
	
	protected HashSet<DigitalObject> getRootTaxonomyItems(String hostId, DigitalObject startingFile) {
		HashSet<DigitalObject> rootTaxonomyItems = new HashSet<DigitalObject>();
		getParentOrSelfRecursion(hostId, startingFile, rootTaxonomyItems);
		return rootTaxonomyItems;
	}
	
	private void getParentOrSelfRecursion(String hostId, Entity object, HashSet<DigitalObject> currentRootItems) {
		HashSet<Entity> sources = object.getMyParents();
		if (sources.size() > 0) {
			for (Entity entity : sources){
				String entityHostId = entity.getProperty(HOST_ID).getValue();
				if (entityHostId.equals(hostId)) {
					getParentOrSelfRecursion(hostId, entity, currentRootItems);
				}
			}
		} else {
			// A HashSet refuses adding a duplicate entry, so let's rely on that
			currentRootItems.add((DigitalObject)object);
			return;
		}
	}
}
