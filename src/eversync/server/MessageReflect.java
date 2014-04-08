package eversync.server;

import java.lang.reflect.Method;

import org.json.JSONException;

import eversync.server.Message.*;

public class MessageReflect {
	
	public void getLinkedItems(EverSyncClient client, String itemLocation, String itemName) throws JSONException {
		System.out.println("Test variable parsing: " + itemLocation + " " + itemName);
		NormalResponse response = new NormalResponse();
		response.setKeyValue("methodName", "showLinkedItems");
		response.setKeyValue("items", "{FileSystem:[{id:id_1,name:filename_1},{id:id_2,name:filename_2}],evernote:[{id:id_3,name:filename_3},{id:id_4,name:filename_4}],facebook:[{id:id_5,name:filename_5},{id:id_6,name:filename_6}]}");
		client.sendMsg(response);
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
	            case "getLinkedItems":
	            		String itemLocation = params.getValue("itemLocation");
	            		String itemName = params.getValue("itemName");
	            		method = cls.getDeclaredMethod("getLinkedItems", new Class[]{EverSyncClient.class, String.class, String.class});
	            		method.invoke(obj, client, itemLocation, itemName);
	            		break;
	            default:
	            		throw new NoSuchMethodException();
	        }
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
