package eversync.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eversync.server.Message.*;

public class MessageReflect {

	private static FileEventHandler _fileEventHandler;

	public MessageReflect(FileEventHandler fileEventHandler) {
		_fileEventHandler = fileEventHandler;
	}

	private void getLinkedFiles(EverSyncClient client, String fileName, String filePath) throws JSONException {
		System.out.println("Test variable parsing: " + filePath + " " + fileName);
		NormalMessage response = new NormalMessage();
		response.setKeyValue("methodName", "showLinkedItems");

		JSONObject nestedMsg = new JSONObject();

		JSONArray list = new JSONArray();
		list.put("msg 1");
		list.put("msg 2");
		list.put("msg 3");

		nestedMsg.put("FileSystem", list);
		nestedMsg.put("evernote", list);
		nestedMsg.put("facebook", list);
		
		_fileEventHandler.getLinkedFiles(client, fileName, filePath);

		response.setKeyValue("items", nestedMsg);
		client.sendMsg(response);
	}

	public void parseMessage(EverSyncClient client, Message message) {
		System.out.println("Parsing the message...");
		System.out.println(message);

		// Message dispatcher, i.e. using Java Reflection to call a method given its name (simple JSON-RPC)
		try {

			Message params = message.getValues("params");

			String methodName = message.getValue("methodName");
			Method method = null;

			// For safety reasons, use some kind of indirection through a switch.
			switch (methodName) {
			case "getLinkedFiles": {
				String fileName = params.getValue("fileName");
				String filePath = params.getValue("filePath");
				getLinkedFiles(client, fileName, filePath);
				}
				break;
			case "addFile": {
				String fileName = params.getValue("fileName");
				String filePath = params.getValue("filePath");
				String lastModified = params.getValue("lastModified");
				_fileEventHandler.addFile(client, fileName, filePath);
				}
				break;
			case "deleteFile": {
				String fileName = params.getValue("fileName");
				String filePath = params.getValue("filePath");
				String lastModified = params.getValue("lastModified");
				//deleteFile(client, filePath, lastModified);
				}
				break;
			case "modifyFile": {
				System.out.println("modifyFile");
				String fileName = params.getValue("fileName");
				String filePath = params.getValue("filePath");
				String lastModified = params.getValue("lastModified");
				_fileEventHandler.modifyFile(client, fileName, filePath);
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
