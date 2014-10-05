package eversync.iServer;

import java.util.logging.Logger;

import org.json.JSONArray;
import org.sigtec.odi.CardinalityConstraintException;
import org.st.iserver.DigitalObject;

import eversync.server.Server;

public class IServerManagerServicePlugin extends IServerManagerSuper implements IServerManagerInterface {
	// Logger for debugging purposes
	private static final Logger log = Logger.getLogger(IServerManagerServicePlugin.class.getName());


	@Override
	public void addAndLinkFile(String serviceName, String fileName, String fileId) {
		try {
			DigitalObject fileObject = super.addFile(fileName, fileId);
			fileObject.addProperty("hostId", serviceName);
			fileObject.addProperty("hostType", "ExternalService");
		} catch (CardinalityConstraintException e) {
			log.severe("Could not create new DigitalObject");
			e.printStackTrace();
		}
	}

	@Override
	public void deleteFile(String fileName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void modifyFile(String deviceId, String fileName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JSONArray getLinkedFiles(String fileURI) {
		// TODO Auto-generated method stub
		return null;
	}

}
