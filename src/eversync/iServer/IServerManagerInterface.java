package eversync.iServer;

import org.json.JSONArray;

public interface IServerManagerInterface {

	public void addAndLinkFile(String deviceId, String fileName, String fileURI);

	public JSONArray getLinkedFiles(String fileURI);

	public void deleteFile(String fileName);

	public void modifyFile(String deviceId, String fileName);
}
