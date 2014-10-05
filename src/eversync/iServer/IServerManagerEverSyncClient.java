package eversync.iServer;

import java.io.File;
import java.util.HashSet;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sigtec.odi.CardinalityConstraintException;
import org.st.iserver.DigitalObject;
import org.st.iserver.Entity;
import org.st.iserver.util.Property;

import eversync.server.Server;

public class IServerManagerEverSyncClient extends IServerManagerSuper implements IServerManagerInterface {
	// Logger for debugging purposes
	private static final Logger log = Logger.getLogger(IServerManagerEverSyncClient.class.getName());


	@Override
	public void addAndLinkFile(String clientId, String fileName, String filePath) {
		// Firstly unify standardize the file path
		filePath = filePath.replaceAll("\\/", "/");
		try {
			// Files from different devices but with identical paths are considered as copies.
			// So hey are linked by automatically.
			HashSet<DigitalObject> filesToLink = _iServer.getAllDigitalObjects(filePath);
			// Then add the new file
			DigitalObject newFile = super.addFile(fileName, filePath);
			newFile.addProperty("hostId", clientId);
			newFile.addProperty("hostType", "EverSyncClient");
			// And link it with the existing copies
			for(DigitalObject file : filesToLink){
				super.linkFiles(file, newFile);
			}
		} catch (CardinalityConstraintException e) {
			log.severe("Could not create new DigitalObject");
			e.printStackTrace();
		}
		System.out.println("TEST ==> "+_iServer.getAllIdElements());
		// output:
		// TEST ==> [[Individual:EverSync], [Individual:EverSync], file.txt, file2.txt, .DS_Store, everfile.txt, testfile.pdf, textfile.txt]
	}
	
	public JSONArray getLinkedFiles(String fileURI) {
		return super.getLinkedFiles("EverSyncClient", fileURI);
	}

	@Override
	public void deleteFile(String fileName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void modifyFile(String deviceId, String fileName) {
		// TODO Auto-generated method stub
		
	}
	
}
