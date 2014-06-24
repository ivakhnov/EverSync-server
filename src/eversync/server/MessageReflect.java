package eversync.server;

import java.lang.reflect.Method;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eversync.server.Message.*;

public class MessageReflect {

	public void getLinkedItems(EverSyncClient client, String itemLocation, String itemName) throws JSONException {
		System.out.println("Test variable parsing: " + itemLocation + " " + itemName);
		NormalResponse response = new NormalResponse();
		response.setKeyValue("methodName", "showLinkedItems");

		JSONObject nestedMsg = new JSONObject();

		JSONArray list = new JSONArray();
		list.put("msg 1");
		list.put("msg 2");
		list.put("msg 3");

		nestedMsg.put("FileSystem", list);
		nestedMsg.put("evernote", list);
		nestedMsg.put("facebook", list);

		response.setKeyValue("items", nestedMsg);
		client.sendMsg(response);
	}

	/**
	 * A new file has been added to the watched directory on a client.
	 * The id of this file (path + client id) has to be stored in the iServer.
	 * @param client: Internal object which represents the involved client.
	 * @param filePath: Path to the file (starting from the root directory which is being watched).
	 * @param lastModified: UNIX timestamp when the file has been modified.
	 */
	public static void addFile(EverSyncClient client,String filePath, String lastModified) {
		System.out.println("--- SYNC ---");
		System.out.println("clientID: " + client.getId());
		System.out.println("filePath: " + filePath);
		System.out.println("lastModified: " + lastModified);
		System.out.println("--- SYNC ---");

		//Prepare and send a response message
		SyncResponse syncResp = new SyncResponse();
		client.sendMsg(syncResp);
	}

	public static void parseMessage(EverSyncClient client, Message message) {
		System.out.println("Parsing the message...");
		System.out.println(message);

		// Message dispatcher, i.e. using Java Reflection to call a method given its name (simple JSON-RPC)
		try {			
			Class cls = Class.forName("eversync.server.MessageReflect");
			Object obj = cls.newInstance();

			Message params = message.getValues("params");

			String methodName = message.getValue("methodName");
			Method method = null;

			// For safety reasons, use some kind of indirection through a switch.
			switch (methodName) {
			case "getLinkedItems": {
				String itemLocation = params.getValue("itemLocation");
				String itemName = params.getValue("itemName");
				method = cls.getDeclaredMethod("getLinkedItems", new Class[]{EverSyncClient.class, String.class, String.class});
				method.invoke(obj, client, itemLocation, itemName);
				}
				break;
			case "addFile": {
				String filePath = params.getValue("filePath");
				String lastModified = params.getValue("lastModified");
				addFile(client, filePath, lastModified);
				}
				break;
			case "deleteFile": {
				String filePath = params.getValue("filePath");
				String lastModified = params.getValue("lastModified");
				//deleteFile(client, filePath, lastModified);
				}
				break;
			case "modifyFile": {
				String filePath = params.getValue("filePath");
				String lastModified = params.getValue("lastModified");
				System.out.println("modifyFile");
				//modifyFile(client, filePath, lastModified);
				}
				break;
			default:
				throw new NoSuchMethodException();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
