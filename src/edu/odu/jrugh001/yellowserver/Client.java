package edu.odu.jrugh001.yellowserver;

import lombok.Getter;

@Getter
public class Client {
	
	private String email;
	private String username;
	
	public Client(String email, String username) {
		this.email = email;
		this.username = username;
	}
}
