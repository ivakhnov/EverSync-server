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
	protected static final Logger log = Logger.getLogger(Server.class.getName());

	// Singletons
	protected static final IServerInterface _iServer = new IServerCCO();
	private static final Individual _creator = _iServer.createIndividual("EverSync");

	protected DigitalObject addFile(String fileName, String fileId) throws CardinalityConstraintException {
//		DigitalObject potentialLinkedFile = _iServer.getDigitalObject(fileName);
		DigitalObject fileObject = _iServer.createDigitalObject(fileName, fileId, _creator);
//		if (potentialLinkedFile != null) {
//			linkFiles(potentialLinkedFile, fileObject);
//		}
		return fileObject;
	}

	private void linkFiles(DigitalObject source, DigitalObject target) {
		try {
			_iServer.createNavigationalLink(source, target);
		} catch (CardinalityConstraintException e) {
			log.severe("Could not create a link between two files!");
			e.printStackTrace();
		}
	}

	public void getLinkedFiles() {
		
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
