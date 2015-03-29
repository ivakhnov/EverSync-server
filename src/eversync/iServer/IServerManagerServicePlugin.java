package eversync.iServer;

import static eversync.iServer.Constants.*;

import java.util.HashSet;
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
			newFile.addProperty(HOST_ID, serviceName);
			newFile.addProperty(HOST_TYPE, SERVICE_PLUGIN);
		} catch (CardinalityConstraintException e) {
			log.severe("Could not create new DigitalObject");
			e.printStackTrace();
		}
		
		if (newFile == null)
			return; // Stop execution if there was an exception on creation
		
		// Automatically link the new file with the existing copies on third party services AND local files
		// (in order to link to local files via UI-client confirmation you have to change this for loop and/or add another one
		for(DigitalObject file : remoteFilesToLink){
			Property hostType = file.getProperty(HOST_TYPE);
			Property hostId = file.getProperty(HOST_ID);
			if (hostId.equals(serviceName) || !hostType.equals(SERVICE_PLUGIN))
				continue; // don't link to yourself
			
			super.linkFilesDirected(file, newFile);
			super.linkFilesDirected(newFile, file);
		}
		
		// TODO: Remove this test code
		System.out.println("TEST ==> "+_iServer.getAllIdElements());
		// output:
		// TEST ==> [[Individual:EverSync], [Individual:EverSync], file.txt, file2.txt, .DS_Store, everfile.txt, testfile.pdf, textfile.txt]
	}

	@Override
	public String addFile(String serviceName, String fileName, String fileId, String fileNameLabel) {		
		// Since our file linking mechanism is based on file names, 
		// we don't allow a service to add multiple files with the same file name. Those files are then
		// considered to be duplicates for one single file.
		String duplicateUri = searchFile(serviceName, fileName);
		if (duplicateUri != null && !duplicateUri.isEmpty()) {
			log.info("File named: " + fileName + " already exists in service: " + serviceName);
			return duplicateUri;
		}
		
		// Declare new file object
		DigitalObject newFile = null;
		
		try {
			// Add the new file
			newFile = super.addFile(fileName, fileId);
			if (fileNameLabel != null) {
				newFile.setLabel(fileNameLabel);
			}
			newFile.addProperty(HOST_ID, serviceName);
			newFile.addProperty(HOST_TYPE, SERVICE_PLUGIN);
		} catch (CardinalityConstraintException e) {
			log.severe("Could not create new DigitalObject");
			e.printStackTrace();
		}
		return newFile.getUri();
	}
	
	/**
	 * All the files with the same file name as the file of which the uri is given, will be linked.
	 * Note that this is the implementation for the service plugins.
	 * The files of the service plugins get a bidirectional link (i.e. actually 2 links, from-to and to-from)
	 * The 'local' files on the EverSync clients are not considered in this linking procedure.
	 * @param fileUri
	 */
	@Override
	public void searchAndLinkRelatedByUri(String fileUri) {
		DigitalObject rootFile = _iServer.getDigitalObjectUrl(fileUri);
		String rootFileHostId = rootFile.getProperty(HOST_ID).getValue();
		HashSet<DigitalObject> remoteFilesToLink = _iServer.getAllDigitalObjects(rootFile.getName());
		for(DigitalObject file : remoteFilesToLink) {
			String hostType = file.getProperty(HOST_TYPE).getValue();
			String hostId = file.getProperty(HOST_ID).getValue();
			if (hostId.equals(rootFileHostId) || !hostType.equals(SERVICE_PLUGIN))
				continue; // don't link to yourself
			
			super.linkFilesDirected(file, rootFile);
			super.linkFilesDirected(rootFile, file);
		}
	}
	
	@Override
	public JSONArray getAllLinkedFiles(String fileName) {
		String fileUri = _iServer.getDigitalObject(fileName).getUri();
		return super.getAllLinkedFilesRecursively(SERVICE_PLUGIN, fileUri);
	}
	
	@Override
	public JSONArray getLinkedFiles(String fileURI) {
		return super.getLinkedFiles(SERVICE_PLUGIN, fileURI);
	}
	
	public void deleteFile(String fileName) {
		// TODO Auto-generated method stub
		
	}

	public void modifyFile(String deviceId, String fileName) {
		// TODO Auto-generated method stub
		
	}
	
	private String searchFile(String serviceName, String fileName) {
		String resultUri = new String();
		
		HashSet<DigitalObject> remoteFilesToLink = _iServer.getAllDigitalObjects(fileName);
		for(DigitalObject file : remoteFilesToLink) {
			String fileHostType = file.getProperty(HOST_TYPE).getValue();
			String fileHostId = file.getProperty(HOST_ID).getValue();
			
			if (!fileHostType.equals(SERVICE_PLUGIN) || !fileHostId.equals(serviceName))
				continue;
			
			if (file.getName().equals(fileName)) {
				resultUri = file.getUri();
				break; // stop the loop
			}
		}
		return resultUri;
	}

}
