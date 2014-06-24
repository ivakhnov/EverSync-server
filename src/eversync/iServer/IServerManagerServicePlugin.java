package eversync.iServer;

import org.sigtec.odi.CardinalityConstraintException;
import org.st.iserver.DigitalObject;

public class IServerManagerServicePlugin extends IServerManagerSuper implements IServerManagerInterface {
	
	@Override
	public void addFile(String serviceName, String fileName, String fileId) {
		try {
			DigitalObject fileObject = super.createFile(serviceName, fileName, fileId);
			fileObject.addProperty("hostType", "ExternalService");
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

}
