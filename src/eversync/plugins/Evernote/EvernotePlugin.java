package eversync.plugins.Evernote;

import java.util.List;

import com.evernote.auth.EvernoteAuth;
import com.evernote.auth.EvernoteService;
import com.evernote.clients.ClientFactory;
import com.evernote.clients.NoteStoreClient;
import com.evernote.clients.UserStoreClient;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteMetadata;
import com.evernote.edam.notestore.NotesMetadataList;
import com.evernote.edam.notestore.NotesMetadataResultSpec;
import com.evernote.edam.notestore.SyncState;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteSortOrder;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.Resource;
import com.evernote.thrift.TException;

import eversync.plugins.Plugin;
import eversync.plugins.PluginInterface;
import eversync.server.FileEventHandler;

public class EvernotePlugin extends Plugin implements PluginInterface {
	
	private static final String NOTE_LABEL = "EvernoteNote";
	
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
		super._pluginName = "Evernote";
		
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
				System.out.println("noteGuid : " + noteData.getGuid());
				System.out.println("fileName : " + noteData.getTitle());
				Note note = noteStore.getNote(noteData.getGuid(), false, true, false, false);
				List<Resource> resources = note.getResources();
				if (resources != null && resources.size() > 0) 
				{
					System.out.println("Aantal resources : " + note.getResources().size());
					for (Resource resource : resources) {
						byte[] fileContent = resource.getData().getBody();
						String fileType = resource.getMime();
						String fileName = resource.getAttributes().getFileName();
						
						System.out.println("fileContent : " + fileContent);
						System.out.println("fileType : " + fileType);
						System.out.println("fileName : " + fileName);
					};
					System.out.println(" * " + note);
				}
			}
		}
		System.out.println();
		System.out.println();
	}
	
	/**
	 * Collects all the notebooks of a user and adds them to the iServer. 
	 * No links between notes and files (local or remote) are created here.
	 * This method is used to construct the first hop of the links: 
	 * local file => notebook => remote file inside notebook => potential any other items related to the file
	 * @throws EDAMUserException
	 * @throws EDAMSystemException
	 * @throws TException
	 */
	private void getAllNotes() throws EDAMUserException, EDAMSystemException, TException {
		List<Notebook> notebooks = noteStore.listNotebooks();
		for (Notebook notebook : notebooks) {
			super.addFile(notebook.getGuid(), notebook.getGuid(), notebook.getName());
		}
	}
	
	/**
	 * Collects all files and notes from the Evernote service and adds them to the IServer.
	 * This method is used to construct the two hops: 
	 * local file => note => remote file inside the note
	 * @throws TException 
	 * @throws EDAMSystemException 
	 * @throws EDAMUserException 
	 * @throws EDAMNotFoundException 
	 */
	private void getAllFiles() throws EDAMUserException, EDAMSystemException, TException, EDAMNotFoundException {
		// First, get a list of all notebooks
		List<Notebook> notebooks = noteStore.listNotebooks();

		for (Notebook notebook : notebooks) {
			// Next, search for the first 100 notes in this notebook, ordering by update date
			NoteFilter filter = new NoteFilter();
			filter.setNotebookGuid(notebook.getGuid());
			filter.setOrder(NoteSortOrder.UPDATED.getValue());
			filter.setAscending(true);
			
			NotesMetadataResultSpec resultSpec = new NotesMetadataResultSpec();
			resultSpec.setIncludeTitle(true);
			resultSpec.setIncludeNotebookGuid(true);
			
			NotesMetadataList notesList = noteStore.findNotesMetadata(filter, 0, 100, resultSpec);
			for (NoteMetadata noteData : notesList.getNotes()) {
				
				// Add the note to the iServer
				String noteId = String.join(".", noteData.getGuid(), NOTE_LABEL);
				super.addFile(noteId, noteId, noteData.getTitle());
				
				Note note = noteStore.getNote(noteData.getGuid(), false, true, false, false);
				List<Resource> resources = note.getResources();
				if (resources != null && resources.size() > 0) 
				{
					for (Resource resource : resources) {
						String fileId = resource.getGuid();
						String fileName = resource.getAttributes().getFileName();
						
						String fileUri = super.addFile(fileName, fileId, fileName);
						super.linkFilesDirected(noteId, fileUri);
						super.searchAndLinkRelated(fileUri);
					};
				}
			}
		}
	}

	/**
	 * Intialize UserStore and NoteStore clients. During this step, we
	 * authenticate with the Evernote web service. All of this code is boilerplate.
	 */
	@Override
	public void init(FileEventHandler fileEventHandler) {
		super._fileEventHandler = fileEventHandler;
		try {
			// Extract all notes and files and relate them
			getAllFiles();
		} catch (EDAMUserException | EDAMSystemException | TException | EDAMNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			//listNotes();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	@Override
	public void replaceFile(String fileName, String fileUri, byte[] fileByteArray) {
		// TODO Auto-generated method stub
		
	}

	
}
