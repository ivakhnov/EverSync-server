package eversync.iServer;

import org.sigtec.odi.CardinalityConstraintException;
import org.st.iserver.DigitalObject;

public class IServerManagerEverSyncClient extends IServerManagerSuper implements IServerManagerInterface {

	@Override
	public void addFile(String clientId, String fileName, String filePath, String lastModified) {
		try {
			DigitalObject fileObject = super.createFile(fileName, filePath);
			fileObject.addProperty("hostId", clientId);
			fileObject.addProperty("hostType", "EverSyncClient");
		} catch (CardinalityConstraintException e) {
			super.log.severe("Could not create new DigitalObject");
			e.printStackTrace();
		}
	}

	@Override
	public void getFiles() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteFile(String fileName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateFile(String deviceId, String fileName) {
		// TODO Auto-generated method stub
		
	}
	
}
