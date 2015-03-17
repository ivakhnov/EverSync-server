package eversync.iServer;

import java.util.HashSet;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.sigtec.odi.CardinalityConstraintException;
import org.st.iserver.DigitalObject;
import org.st.iserver.util.Property;

public class IServerManagerEverSyncClient extends IServerManagerSuper implements IServerManagerInterface {
	// Logger for debugging purposes
	private static final Logger log = Logger.getLogger(IServerManagerEverSyncClient.class.getName());


	@Override
	public void addAndLinkFile(String clientId, String fileName, String filePath) {
		// Firstly unify and standardize the file path
		filePath = filePath.replaceAll("\\/", "/");
		
		// Files from different devices but with identical paths are considered as copies.
		// So they are linked automatically.
		HashSet<DigitalObject> localFilesToLink = _iServer.getAllDigitalObjectsUrl(filePath);
		
		// Files on different third party services with the same are considered as POTENTIAL copies
		// and have to be linked after user's confirmation.
		HashSet<DigitalObject> remoteFilesToLink = _iServer.getAllDigitalObjects(fileName);
		
		// Declare new file object
		DigitalObject newFile = null;
		
		try {
			// Add the new file
			newFile = super.addFile(fileName, filePath);
			newFile.addProperty("hostId", clientId);
			newFile.addProperty("hostType", "EverSyncClient");
		} catch (CardinalityConstraintException e) {
			log.severe("Could not create new DigitalObject");
			e.printStackTrace();
		}
		
		if (newFile == null)
			return; // Stop execution if there was an exception on creation
		
		// Automatically link the new file with the existing copies on other devices
		for(DigitalObject file : localFilesToLink){
			Property hostType = file.getProperty("hostType");
			if (!hostType.getValue().equals("EverSyncClient"))
				continue;
			
			super.linkFiles(file, newFile);
		}
		
		// Automatically link the new file with the existing copies on third party services
		for(DigitalObject file : remoteFilesToLink){
			Property hostType = file.getProperty("hostType");
			if (hostType.getValue().equals("EverSyncClient"))
				continue;
			
			super.linkFiles(file, newFile);
		}
		
		// TODO: Remove this test code
		System.out.println("TEST ==> "+_iServer.getAllIdElements());
		// output:
		// TEST ==> [[Individual:EverSync], [Individual:EverSync], file.txt, file2.txt, .DS_Store, everfile.txt, testfile.pdf, textfile.txt]
	}
	
	@Override
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
