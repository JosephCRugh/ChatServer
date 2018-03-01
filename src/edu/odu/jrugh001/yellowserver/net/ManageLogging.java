package edu.odu.jrugh001.yellowserver.net;

import java.sql.SQLException;

import org.mindrot.jbcrypt.BCrypt;

import edu.odu.jrugh001.yellowserver.Client;
import edu.odu.jrugh001.yellowserver.ClientHandle;
import edu.odu.jrugh001.yellowserver.ClientManager;
import edu.odu.jrugh001.yellowserver.Server;
import edu.odu.jrugh001.yellowserver.net.listening.Listener;
import edu.odu.jrugh001.yellowserver.net.listening.Opcode;
import edu.odu.jrugh001.yellowserver.net.listening.Opcodes;

public class ManageLogging implements Listener {
	
	@Opcode(getOpcode = Opcodes.LOGIN_REQUEST)
	public void onClientLogin(ClientHandle clientHandle, Server server) {
		
		String email = clientHandle.readString();
		String password = clientHandle.readString();
		
		// Do not even send the client a response
		if (email == null || password == null) return;
		
		ClientManager clientManager = ClientManager.getInstance();
		
		// User already logged in
		if (clientManager.getLoggedInEmails().contains(email)) {
			clientHandle.write(Opcodes.LOGIN_REQUEST_RESPONSE, outStream -> outStream.writeByte(0x2));
			return;
		}
		
		boolean[] passwordFound = { false };
		String[] username = { "" };
		server.getDatabase().find("auth", "email", email, result -> {
			try {
				if (result.next()) {
					String hashedPassword = result.getString("password");
					
					// Password found
					if (BCrypt.checkpw(password, hashedPassword)) {
						username[0] = result.getString("username");
						passwordFound[0] = true;
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
		
		clientHandle.write(Opcodes.LOGIN_REQUEST_RESPONSE, outStream -> {
			if (passwordFound[0]) {
				outStream.writeByte(0x1);
				// Writing out the client's username
				outStream.writeUTF(username[0]);	
				
				// Providing them with the username's of everyone currently online
				String[] usernames = new String[clientManager.getClients().size()];
				int[] count = { 0 }; 
				clientManager.getClients().values().forEach(client -> {
					usernames[count[0]++] = client.getUsername(); 
				});
				
				outStream.writeObject(usernames);
			} else {
				outStream.writeByte(0x0);	
			}
		});
		
		// Do not log the user in if the password cannot be matched to the email
		if (!passwordFound[0]) return;
		
		System.out.println(username[0] + " Has logged in.");
		
		clientManager.getClients().keySet().forEach(informClientHandle ->
			informClientHandle.write(Opcodes.INDICATE_LOGIN, outStream -> outStream.writeUTF(username[0])));
		
		clientManager.addClient(clientHandle, new Client(email, username[0]));
	}
	
	@Opcode(getOpcode = Opcodes.LOGOUT_REQUEST)
	public void onClientLoggout(ClientHandle clientHandle, Server server) {
		
		Client client = ClientManager.getInstance().getClient(clientHandle);
		if (client == null) return;
		
		System.out.println(client.getUsername() + " is logging out.");
		ClientManager clientManager = ClientManager.getInstance();
		clientManager.removeClient(clientHandle);
		
		// Informing the other clients that the client has logged out
		String clientUsername = client.getUsername();
		clientManager.getClients().keySet().forEach(informClientHandle -> {
			informClientHandle.write(Opcodes.LOGOUT_REQUEST, outStream -> outStream.writeUTF(clientUsername));
		});
	}
}
