package eversync.plugins;

import java.util.List;

import com.evernote.auth.EvernoteAuth;
import com.evernote.auth.EvernoteService;
import com.evernote.clients.ClientFactory;
import com.evernote.clients.NoteStoreClient;
import com.evernote.clients.UserStoreClient;
import com.evernote.edam.error.EDAMErrorCode;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.notestore.NoteMetadata;
import com.evernote.edam.notestore.NotesMetadataList;
import com.evernote.edam.notestore.NotesMetadataResultSpec;
import com.evernote.edam.notestore.SyncChunk;
import com.evernote.edam.notestore.SyncChunkFilter;
import com.evernote.edam.notestore.SyncState;
import com.evernote.edam.type.Data;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteSortOrder;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.Resource;
import com.evernote.edam.type.ResourceAttributes;
import com.evernote.edam.type.Tag;
import com.evernote.thrift.TException;
import com.evernote.thrift.transport.TTransportException;

import eversync.iServer.IServerManagerInterface;
import eversync.server.FileEventHandler;

public class EvernotePlugin implements Plugin {
	
	private String _pluginName;
	private String _extensionName;
	private String _iconName;
	
	private FileEventHandler _fileEventHandler;
	
	private UserStoreClient userStore;
	private NoteStoreClient noteStore;
	
	
	/**
	 * Constructor
	 * 
	 * Intialize UserStore and NoteStore clients. During this step, we
	 * authenticate with the Evernote web service. All of this code is boilerplate
	 * - you can copy it straight into your application.
	 */
	public EvernotePlugin(String token) throws Exception {
		_pluginName = "Evernote";
		_extensionName = "extensieNaam";
		_iconName = "icoonNaam";
		
		// Set up the UserStore client and check that we can speak to the server
		EvernoteAuth evernoteAuth = new EvernoteAuth(EvernoteService.SANDBOX, token);
		ClientFactory factory = new ClientFactory(evernoteAuth);
		userStore = factory.createUserStoreClient();
	
		boolean versionOk = userStore.checkVersion("Evernote EverSync (Java)",
				com.evernote.edam.userstore.Constants.EDAM_VERSION_MAJOR,
				com.evernote.edam.userstore.Constants.EDAM_VERSION_MINOR);
		if (!versionOk) {
			System.err.println("Incompatible Evernote client protocol version");
			System.exit(1);
		}
	
		// Set up the NoteStore client
		noteStore = factory.createNoteStoreClient();
	}
	
	public void pollForChanges() throws EDAMUserException, EDAMSystemException, TException {
		int latestUpdateCount = 0; // Persist this value
				 
		// Each time you want to check for new and updated notes...
		SyncState currentState = noteStore.getSyncState();
		int currentUpdateCount = currentState.getUpdateCount();
		
		System.out.println("getUpdateCount: " + currentUpdateCount);
		System.out.println("getSyncState: " + noteStore.getSyncState());
		
		if (currentUpdateCount > latestUpdateCount) {
		 
//		  // Something in the account has changed, so search for notes
//		  NotesMetadataList newNotes = noteStore.findNotesMetadata( ... );
//		  
//		  // Do something with the notes you found...
//		  for (NoteMetadata note : newNotes.getNotes()) {
//		    // ...
//		  }
		  
		  // Keep track of the new high-water mark
		  latestUpdateCount = currentUpdateCount;
		}
	}
	
	/**
	 * Retrieve and display a list of the user's notes.
	 */
	public void listNotes() throws Exception {
		// List the notes in the user's account
		System.out.println("Listing notes:");
	
		// First, get a list of all notebooks
		List<Notebook> notebooks = noteStore.listNotebooks();
	
		for (Notebook notebook : notebooks) {
			System.out.println("Notebook: " + notebook.getName());
	
			// Next, search for the first 100 notes in this notebook, ordering by update date
			NoteFilter filter = new NoteFilter();
			filter.setNotebookGuid(notebook.getGuid());
			filter.setOrder(NoteSortOrder.UPDATED.getValue());
			filter.setAscending(true);
			
//			filter.setWords("updated:20140209T010000 resource:*");
			
			NotesMetadataResultSpec resultSpec = new NotesMetadataResultSpec();
			resultSpec.setIncludeTitle(true);
			resultSpec.setIncludeCreated(true);
			resultSpec.setIncludeUpdated(true);
			resultSpec.setIncludeLargestResourceSize(true);
	
			NotesMetadataList noteList = noteStore.findNotesMetadata(filter, 0, 100, resultSpec);
			for (NoteMetadata noteData : noteList.getNotes()) {
				Note note = noteStore.getNote(noteData.getGuid(), false, true, false, false);
				System.out.println(" * " + note);
			}
		}
		System.out.println();
		System.out.println();
	}

	@Override
	public String getPluginName() {
		return _pluginName;
	}

	@Override
	public String getExtensionName() {
		return _extensionName;
	}

	@Override
	public String getIconName() {
		return _iconName;
	}

	/**
	 * Intialize UserStore and NoteStore clients. During this step, we
	 * authenticate with the Evernote web service. All of this code is boilerplate.
	 */
	@Override
	public void init(FileEventHandler fileEventHandler) {
		_fileEventHandler = fileEventHandler;
	}

	@Override
	public void run() {
		// Debug code to be deleted from here
//		try {
//			pollForChanges();
//		} catch (EDAMUserException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (EDAMSystemException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (TException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	
}
