package eversync.iServer;

import java.util.HashSet;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.sigtec.odi.CardinalityConstraintException;
import org.st.iserver.DigitalObject;

public class IServerManagerServicePlugin extends IServerManagerSuper implements IServerManagerInterface {
	// Logger for debugging purposes
	private static final Logger log = Logger.getLogger(IServerManagerServicePlugin.class.getName());


	@Override
	public void addAndLinkFile(String serviceName, String fileName, String fileId) {
		try {
			// For now, files from remote services are considered as copies to local files
			// with identical filenames (it's an assumption that may change in future).
			HashSet<DigitalObject> filesToLink = _iServer.getAllDigitalObjects(fileName);
			
			// Then add the new file
			DigitalObject newFile = super.addFile(fileName, fileId);
			newFile.addProperty("hostId", serviceName);
			newFile.addProperty("hostType", "ExternalService");
			
			// And finally link the existing copies all together
			for(DigitalObject file : filesToLink) {
				super.linkFiles(file, newFile);
			}
		} catch (CardinalityConstraintException e) {
			log.severe("Could not create new DigitalObject");
			e.printStackTrace();
		}
		System.out.println("TEST ==> "+_iServer.getAllIdElements());
	}

	/**
	 * Almost the same as the addAndLinkFile does but it doesn't add a file, it tries to link given file to the
	 * existing ones. 
	 */
	public void tryToLinkTo(String deviceId, String fileName, String fileUri) {
		
	}
	
	public void deleteFile(String fileName) {
		// TODO Auto-generated method stub
		
	}

	public void modifyFile(String deviceId, String fileName) {
		// TODO Auto-generated method stub
		
	}

	public JSONArray getLinkedFiles(String fileURI) {
		// TODO Auto-generated method stub
		return null;
	}

}
