package eversync.iServer;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.sigtec.odi.CardinalityConstraintException;
import org.st.iserver.DigitalObject;
import org.st.iserver.IServerCCO;
import org.st.iserver.IServerInterface;
import org.st.iserver.Individual;

import eversync.server.Server;

public class IServerManagerSuper {
	// Logger for debugging purposes
	protected static Logger log = Logger.getLogger(Server.class.getName());
	
	private static IServerInterface _iServer;
	private static Individual _creator;
	
	// Constructor
	public IServerManagerSuper() {
		_iServer = new IServerCCO();
		_creator = _iServer.createIndividual("EverSync");
	}

	protected DigitalObject createFile(String fileName, String fileId) throws CardinalityConstraintException {
		DigitalObject fileObject = _iServer.createDigitalObject(fileName, fileId, _creator);
		return fileObject;
	}
	

	
	public void linkObjects(String localId, String service, String serviceId) {
//		_iServer.getDigitalObject(id);
//		_iServer.createNavigationalLink(source, target)
		
//		DigitalObject obj = new DigitalObject(localId);
//		obj.addProperty(service, serviceId);
	}
	
	/**
	 * Returns a list of all files (DigitalObjects) stored in a particular external service.
	 * @param serviceName
	 * @return
	 */
	public ArrayList<DigitalObject> getObjectsService(String serviceName) {
		ArrayList<DigitalObject> res = new ArrayList<DigitalObject>();
		// TODO read all objects of a particular service
		try {
			DigitalObject object = _iServer.createDigitalObject("exam-examples.pdf", "id_van_note_nummer_2", _creator);
			res.add(object);
		} catch (CardinalityConstraintException e) {
			e.printStackTrace();
		}
		return res;
	}
}
