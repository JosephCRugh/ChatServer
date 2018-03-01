package edu.odu.jrugh001.yellowserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;

public class ClientManager {
	
	private @Getter Map<ClientHandle, Client> clients = new HashMap<>(); 
	private @Getter List<String> loggedInEmails = new ArrayList<>();
	
	private static @Getter ClientManager instance = new ClientManager();
	
	private ClientManager() {}
	
	public void addClient(ClientHandle clientHandle, Client client) {
		clients.put(clientHandle, client);
		loggedInEmails.add(client.getEmail());
	}
	
	public Client removeClient(ClientHandle clientHandle) {
		Client client = clients.remove(clientHandle);
		loggedInEmails.remove(client.getEmail());
		return client;
	}
	
	public Client getClient(ClientHandle clientHandle) {
		return clients.get(clientHandle);
	}
}
