package eversync.iServer;

import org.sigtec.odi.CardinalityConstraintException;
import org.st.iserver.DigitalObject;

public class IServerManagerEverSyncClient extends IServerManagerSuper implements IServerManagerInterface {

	@Override
	public void addFile(String clientId, String fileName, String filePath) {
		try {
			DigitalObject fileObject = super.addFile(fileName, filePath);
			fileObject.addProperty("hostId", clientId);
			fileObject.addProperty("hostType", "EverSyncClient");
		} catch (CardinalityConstraintException e) {
			super.log.severe("Could not create new DigitalObject");
			e.printStackTrace();
		}
		System.out.println("TEST ==> "+_iServer.getAllIdElements());
		// output:
		// TEST ==> [[Individual:EverSync], [Individual:EverSync], file.txt, file2.txt, .DS_Store, everfile.txt, testfile.pdf, textfile.txt]
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
