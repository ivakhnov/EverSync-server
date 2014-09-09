package eversync.iServer;

import org.sigtec.odi.CardinalityConstraintException;
import org.st.iserver.DigitalObject;

public class IServerManagerServicePlugin extends IServerManagerSuper implements IServerManagerInterface {
	
	@Override
	public void addFile(String serviceName, String fileName, String fileId) {
		try {
			DigitalObject fileObject = super.addFile(fileName, fileId);
			fileObject.addProperty("hostId", serviceName);
			fileObject.addProperty("hostType", "ExternalService");
		} catch (CardinalityConstraintException e) {
			super.log.severe("Could not create new DigitalObject");
			e.printStackTrace();
		}
	}

	@Override
	public void getLinkedFiles() {
		// TODO Auto-generated method stub
		
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
