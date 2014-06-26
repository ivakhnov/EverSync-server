package eversync.iServer;

public interface IServerManagerInterface {

	public void addFile(String deviceId, String fileName, String fileURI, String lastModified);
	
	public void deleteFile(String fileName);

	public void getFiles();
	
	public void updateFile(String deviceId, String fileName);
}
