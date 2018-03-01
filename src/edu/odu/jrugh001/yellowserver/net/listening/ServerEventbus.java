package edu.odu.jrugh001.yellowserver.net.listening;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import edu.odu.jrugh001.yellowserver.ClientHandle;
import edu.odu.jrugh001.yellowserver.Server;
import lombok.AllArgsConstructor;

public class ServerEventbus {
	
	@AllArgsConstructor
	private class CallbackData {
		private Listener listener;
		private Method method;
	}
	
	private Map<Character, CallbackData> listeners = new HashMap<>();
	
	public void registerListener(Listener listener) {
		for (Method method : listener.getClass().getMethods()) {
			for (Opcode opcodeAnno : method.getAnnotationsByType(Opcode.class)) {
				Class<?>[] params = method.getParameterTypes();
				String error = "Listener: " + listener;
				if (params.length != 2) 
					throw new RuntimeException(error + " must have 2 parameters.");
				if (!params[0].equals(ClientHandle.class)) 
					throw new RuntimeException(error + " first parameter must be of type ClientHandle");		
				if (!params[1].equals(Server.class)) 
					throw new RuntimeException(error + " second parameter must be of type Server");		
				listeners.put(opcodeAnno.getOpcode(), new CallbackData(listener, method));
			}
		}
	}
	
	public void publish(char opcode, ClientHandle clientHandle, Server server) {
		CallbackData callbackData = listeners.get(opcode);
		if (callbackData == null) {
			System.out.println("Callback data was null");
			return;
		}
		try {
			callbackData.method.invoke(callbackData.listener, clientHandle, server);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}
