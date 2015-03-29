package eversync.iServer;

import static eversync.iServer.Constants.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.sigtec.odi.CardinalityConstraintException;
import org.st.iserver.DigitalObject;
import org.st.iserver.util.Property;

public class IServerManagerEverSyncClient extends IServerManagerSuper implements IServerManagerInterface {
	
	// Logger for debugging purposes
	private static final Logger log = Logger.getLogger(IServerManagerEverSyncClient.class.getName());
	
	@Override
	public void addAndLinkFile(String clientId, String fileName, String filePath, String fileNameLabel) {
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
			if (fileNameLabel != null) {
				newFile.setLabel(fileNameLabel);
			}
			newFile.addProperty(HOST_ID, clientId);
			newFile.addProperty(HOST_TYPE, EVERSYNC_CLIENT);
		} catch (CardinalityConstraintException e) {
			log.severe("Could not create new DigitalObject");
			e.printStackTrace();
		}
		
		if (newFile == null)
			return; // Stop execution if there was an exception on creation
		
		// Automatically link the new file with the existing copies on other devices
		for(DigitalObject file : localFilesToLink){
			Property hostType = file.getProperty(HOST_TYPE);
			if (!hostType.getValue().equals(EVERSYNC_CLIENT))
				continue;
			
			super.linkFilesDirected(file, newFile);
			super.linkFilesDirected(newFile, file);
		}
		
		// Automatically link the new file with the existing copies on third party services
		for(DigitalObject file : remoteFilesToLink) {
			Property hostType = file.getProperty(HOST_TYPE);
			if (hostType.getValue().equals(EVERSYNC_CLIENT))
				continue;
			
			// Get the root taxonomy items
			HashSet<DigitalObject> taxonomyRootItems = super.getRootTaxonomyItems(file);
			for (DigitalObject taxonomyRootItem : taxonomyRootItems) {
				super.linkFilesDirected(newFile, taxonomyRootItem);
			}
		}
		
		// TODO: Remove this test code
		System.out.println("TEST ==> "+_iServer.getAllIdElements());
		// output:
		// TEST ==> [[Individual:EverSync], [Individual:EverSync], file.txt, file2.txt, .DS_Store, everfile.txt, testfile.pdf, textfile.txt]
	}

	@Override
	public String addFile(String clientId, String fileName, String filePath, String fileNameLabel) {
		// Declare new file object
		DigitalObject newFile = null;
		
		try {
			// Add the new file
			newFile = super.addFile(fileName, filePath);
			if (fileNameLabel != null) {
				newFile.setLabel(fileNameLabel);
			}
			newFile.addProperty(HOST_ID, clientId);
			newFile.addProperty(HOST_TYPE, EVERSYNC_CLIENT);
		} catch (CardinalityConstraintException e) {
			log.severe("Could not create new DigitalObject");
			e.printStackTrace();
		}
		return newFile.getUri();
	}

	/**
	 * All the files with the same file name as the file of which the uri is given, will be linked.
	 * Note that this is the implementation for the EverSync clients.
	 * The files of the EverSync clients get a bidirectional link (i.e. actually 2 links, from-to and to-from).
	 * The files on the third party service will be linked as follows:
	 * 		local file -> remote file (where this is the root taxonomy file, a.k.a 
	 * 		the highest parent in case of nested self-relations because plugins can have their own hierarchy,
	 * 		for example: book -> chapter -> page -> paragraph -> ...)
	 * Linking files on EverSync clients 
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
			if(hostId.equals(rootFileHostId))
				continue;
			
			if(hostType.equals(EVERSYNC_CLIENT)) {
				super.linkFilesDirected(file, rootFile);
				super.linkFilesDirected(rootFile, file);
			}
			
			if(!hostType.equals(EVERSYNC_CLIENT)) {
				// Get the root taxonomy items
				HashSet<DigitalObject> taxonomyRootItems = super.getRootTaxonomyItems(file);
				for (DigitalObject taxonomyRootItem : taxonomyRootItems) {
					super.linkFilesDirected(rootFile, taxonomyRootItem);
				}
			}
		}
	}
	
	@Override
	public JSONArray getAllLinkedFiles(String fileURI) {
		return super.getAllLinkedFilesRecursively(EVERSYNC_CLIENT, fileURI);
	}

	@Override
	public JSONArray getLinkedFiles(String fileURI) {
		return super.getLinkedFiles(EVERSYNC_CLIENT, fileURI);
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
