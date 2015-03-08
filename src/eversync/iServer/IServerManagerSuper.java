package eversync.iServer;

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

public class IServerManagerSuper {
	// Logger for debugging purposes
	private static final Logger log = Logger.getLogger(IServerManagerSuper.class.getName());

	// Singletons
	protected static final IServerInterface _iServer = new IServerCCO();
	private static final Individual _creator = _iServer.createIndividual("EverSync");

	protected DigitalObject addFile(String fileName, String fileId) throws CardinalityConstraintException {
//		DigitalObject potentialLinkedFile = _iServer.getDigitalObject(fileName);
		DigitalObject fileObject = _iServer.createDigitalObject(fileName, fileId, _creator);
//		if (potentialLinkedFile != null) {
//			linkFiles(potentialLinkedFile, fileObject);
//		}
		return fileObject;
	}

	protected void linkFiles(DigitalObject source, DigitalObject target) {
		try {
			_iServer.createNavigationalLink("NavigationalLink", source, target, _creator);
		} catch (CardinalityConstraintException e) {
			log.severe("Could not create a link between two files!");
			e.printStackTrace();
		}
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
		
		try {
			result.put("name", fileName);
			result.put("uri", fileUri);
			
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
	protected JSONArray getLinkedFiles(String hostType, String fileURI) {
		HashSet<Entity> allLinkedFiles = new HashSet<Entity>();
		JSONArray results = new JSONArray();
		
		// Just a random file with the given URI (there are possibly multiple files with the same file
		// path which is used as a URI for local files)
		DigitalObject aFile =  _iServer.getDigitalObjectUrl(fileURI);
		getLinkedFilesRecursion(aFile, allLinkedFiles);
		
		for(Entity file : allLinkedFiles) {
			if(file.getProperty("hostType").getValue().equals(hostType)) {
				JSONObject propertiesJson = digitalObjectToJson((DigitalObject)file);
				results.put(propertiesJson);
			}
		}
		return results;
	}
	
//	/**
//	 * Returns a list of all files (DigitalObjects) stored in a particular external service.
//	 * @param serviceName
//	 * @return
//	 */
//	public ArrayList<DigitalObject> getObjectsService(String serviceName) {
//		ArrayList<DigitalObject> res = new ArrayList<DigitalObject>();
//		// TODO read all objects of a particular service
//		try {
//			DigitalObject object = _iServer.createDigitalObject("exam-examples.pdf", "id_van_note_nummer_2", _creator);
//			res.add(object);
//		} catch (CardinalityConstraintException e) {
//			e.printStackTrace();
//		}
//		return res;
//	}
}
