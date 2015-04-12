package eversync.plugins;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import eversync.server.EverSyncClient;
import eversync.server.FileEventHandler;
import eversync.server.Message.OpenUrlInBrowserRequest;

public class Plugin {

	protected String _pluginName;
	protected FileEventHandler _fileEventHandler;

	public String getPluginName() {
		return _pluginName;
	}
	
	public HashMap getInstallationFiles() throws Exception {
		File root = new File("src/eversync/plugins/" + _pluginName + "/client_installation_files");
		HashMap<String, byte[]> installFiles =  new HashMap<String, byte[]>();
		
		try {
			List<File> list = Arrays.asList(root.listFiles());
			
			for(int x = 0; x < list.size(); x++) {
				File file = list.get(x);
				if (file.isHidden())
					continue; // skip hidden files
				
				Path path = Paths.get(file.getAbsolutePath());
				String fileName = path.getFileName().toString();
				System.out.println("path: " + path);
				System.out.println("fileName: " + fileName);
				byte[] data = Files.readAllBytes(path);
				installFiles.put(fileName, data);
			}

		} catch (Exception e) {
			throw new Exception("Cannot find folder with installation files of: " + _pluginName); 
		}
		
		return installFiles;
	}

	protected void addAndLinkFile(String fileName, String fileId, String fileNameLabel) {
		_fileEventHandler.addAndLinkFile(this, fileName, fileId, fileNameLabel);
	}

	protected String addFile(String fileName, String fileId, String fileNameLabel) {
		return _fileEventHandler.addFile(this, fileName, fileId, fileNameLabel);
	}
	
	protected void linkFilesDirected(String parentFileUri, String childFileUri) {
		_fileEventHandler.linkFilesDirected(this, parentFileUri, childFileUri);
	}
	
	protected void openUrlInBrowser(EverSyncClient client, String url) {
		OpenUrlInBrowserRequest req = new OpenUrlInBrowserRequest(url);
		client.sendMsg(req);
	}
}
