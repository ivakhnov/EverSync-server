package eversync.server;

import java.lang.reflect.Method;

import org.json.JSONArray;
import org.json.JSONObject;

public class MessageReflect {
	
	public void testMethod(int x, int y) {		
		System.out.println("Test variable parsing: " + x + " " + y);
	}
	
	public static void parseMessage(JSONObject message) {
		System.out.println("Parsing the message...");
		System.out.println(message);
        
		// Message dispatcher, i.e. using Java Reflection to call a method given its name (simple JSON-RPC)
		try {			
			Class cls = Class.forName("eversync.server.MessageReflect");
			Object obj = cls.newInstance();
			
			JSONArray params = (JSONArray) message.get("params");
			
			String methodName = (String) message.get("methodName");
			Method method = null;
			
			// For safety reasons, use some kind of indirection through a switch.
			switch (methodName) {
	            case "testMethod": 
	            		int x = params.getInt(0);
	            		int y = params.getInt(1);
	            		method = cls.getDeclaredMethod("testMethod", new Class[]{Integer.TYPE, Integer.TYPE});
	            		method.invoke(obj, x, y);
	                    break;
	            default:
	            		throw new NoSuchMethodException();
	        }
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
