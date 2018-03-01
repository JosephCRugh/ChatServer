package edu.odu.jrugh001.yellowserver;

import java.util.Scanner;

import edu.odu.jrugh001.yellowserver.sql.Database;
import edu.odu.jrugh001.yellowserver.sql.SaveToDatabase;
import lombok.Getter;

@Getter
public class Server {
	
	private static ServerNetwork serverNetwork;
	private Database database;
	private SaveToDatabase saveToDatabase;
	
	public Server() {
		database = new Database();
		if (!database.initialize()) {
			shutdown();
		}
		saveToDatabase = new SaveToDatabase();
	}
	
	public static void main(String args[]) {
		
		serverNetwork = new ServerNetwork();
		
		Server server = new Server();
		serverNetwork.openServer(server);
		
		Scanner scanner = new Scanner(System.in);
		String input;
		while (true) {
			input = scanner.nextLine();
			if (input.equalsIgnoreCase("stop")) break;
		}
		scanner.close();
		server.cleanup();
		
		shutdown();
	}
	
	private void cleanup() {
		database.close();
	}
	
	public static void shutdown() {
		serverNetwork.close();
		System.exit(0);
	}
}
