package eversync.plugins;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import eversync.server.FileEventHandler;

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
				File f = list.get(x);
				Path path = Paths.get(f.getAbsolutePath());
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

	protected void addFile(String fileName, String fileId) {
		_fileEventHandler.addFile(this, fileName, fileId);
	}
}
