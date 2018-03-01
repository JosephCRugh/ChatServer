package edu.odu.jrugh001.yellowserver.sql;

import org.mindrot.jbcrypt.BCrypt;

import edu.odu.jrugh001.yellowserver.Server;

public class SaveToDatabase {

	public void addUser(Server server, String email, String password, String username) {
		String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
		server.getDatabase().insert("auth", "(email, password, username)", email, hashedPassword, username);
	}
}
