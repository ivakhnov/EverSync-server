package eversync.iServer;

public interface IServerManagerInterface {

	public void addFile(String deviceId, String fileName, String fileURI);
	
	public void deleteFile(String fileName);

	public void getFiles();
}
