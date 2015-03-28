package eversync.iServer;

import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.sigtec.odi.CardinalityConstraintException;
import org.st.iserver.DigitalObject;
import org.st.iserver.util.Property;

public class IServerManagerServicePlugin extends IServerManagerSuper implements IServerManagerInterface {
	// Logger for debugging purposes
	private static final Logger log = Logger.getLogger(IServerManagerServicePlugin.class.getName());


	@Override
	public void addAndLinkFile(String serviceName, String fileName, String fileId, String fileNameLabel) {
		// Files on different third party services with the same are considered as POTENTIAL copies
		// and have to be linked after user's confirmation.
		HashSet<DigitalObject> remoteFilesToLink = _iServer.getAllDigitalObjects(fileName);
		
		// Declare new file object
		DigitalObject newFile = null;
		
		try {
			// Add the new file
			newFile = super.addFile(fileName, fileId);
			if (fileNameLabel != null) {
				newFile.setLabel(fileNameLabel);
			}
			newFile.addProperty("hostId", serviceName);
			newFile.addProperty("hostType", "ExternalService");
		} catch (CardinalityConstraintException e) {
			log.severe("Could not create new DigitalObject");
			e.printStackTrace();
		}
		
		if (newFile == null)
			return; // Stop execution if there was an exception on creation
		
		// Automatically link the new file with the existing copies on third party services AND local files
		// (in order to link to local files via UI-client confirmation you have to change this for loop and/or add another one
		for(DigitalObject file : remoteFilesToLink){
			Property hostType = file.getProperty("hostType");
			Property hostId = file.getProperty("hostId");
			if (hostId.getValue().equals(serviceName))
				continue; // don't link to yourself
			
			super.linkFiles(file, newFile);
		}
		
		// TODO: Remove this test code
		System.out.println("TEST ==> "+_iServer.getAllIdElements());
		// output:
		// TEST ==> [[Individual:EverSync], [Individual:EverSync], file.txt, file2.txt, .DS_Store, everfile.txt, testfile.pdf, textfile.txt]
	}

	@Override
	public void addFile(String serviceName, String fileName, String fileId, String fileNameLabel) {
		// Declare new file object
		DigitalObject newFile = null;
		
		try {
			// Add the new file
			newFile = super.addFile(fileName, fileId);
			if (fileNameLabel != null) {
				newFile.setLabel(fileNameLabel);
			}
			newFile.addProperty("hostId", serviceName);
			newFile.addProperty("hostType", "ExternalService");
		} catch (CardinalityConstraintException e) {
			log.severe("Could not create new DigitalObject");
			e.printStackTrace();
		}
	}
	
	@Override
	public JSONArray getAllLinkedFiles(String fileName) {
		String fileUri = _iServer.getDigitalObject(fileName).getUri();
		return super.getAllLinkedFilesRecursively("ExternalService", fileUri);
	}
	
	@Override
	public JSONArray getLinkedFiles(String fileURI) {
		return super.getLinkedFiles("ExternalService", fileURI);
	}
	
	public void deleteFile(String fileName) {
		// TODO Auto-generated method stub
		
	}

	public void modifyFile(String deviceId, String fileName) {
		// TODO Auto-generated method stub
		
	}

}
