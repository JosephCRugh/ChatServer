package edu.odu.jrugh001.yellowserver;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import edu.odu.jrugh001.yellowserver.net.ManageChatMessages;
import edu.odu.jrugh001.yellowserver.net.ManageLogging;
import edu.odu.jrugh001.yellowserver.net.listening.ServerEventbus;
import lombok.Setter;

public class ServerNetwork implements Runnable {

	private final int PORT = 3407;
	
	private ServerSocket serverSocket;
	private ServerEventbus eventBus = new ServerEventbus();
	
	private @Setter volatile boolean running = false;
	private Server server;
	
	public void openServer(Server server) {
		this.server = server;
		
		try {
			serverSocket = new ServerSocket(PORT);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		new Thread(this, "Start").start();
	}

	@Override
	public void run() {
		System.out.println("Server opened on port: " + PORT);
		System.out.println("Please use the stop command to stop the server.");
		running = true;
		registerListeners();
		listenForConnections();
	}
	
	private void registerListeners() {
		eventBus.registerListener(new ManageLogging());
		eventBus.registerListener(new ManageChatMessages());
	}

	private void listenForConnections() {
		new Thread(() -> {
			while (running) {
				try {
	
					receivePackets(serverSocket.accept());
					
				} catch (IOException e) {
					
					if (e instanceof SocketException && !running) {
						break;
					}
					
					e.printStackTrace();
					Server.shutdown();
				}
			}
		}, "ConnectionListener").start();
	}
	
	private void receivePackets(Socket clientSocket) {
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				ClientHandle clientHandle = null;
				try (
						ObjectOutputStream outStream = new ObjectOutputStream(clientSocket.getOutputStream());
						ObjectInputStream inStream = new ObjectInputStream(clientSocket.getInputStream());
						) {
					
					clientHandle = new ClientHandle(clientSocket, outStream, inStream);
					
					while (running) {
						eventBus.publish(inStream.readChar(), clientHandle, server);
					}
				} catch (IOException e) {
					
					if (e instanceof EOFException || e instanceof SocketException) {
						if (clientHandle != null && running) {
							// The client has logged out....
						}
					} else {
						e.printStackTrace();
					}
					
				} finally {
					if (clientSocket != null) {
						try {
							clientSocket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
			
		}, clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort()).start();
	}
	
	public void close() {
		running = false;
		System.out.println("Closing down server...");
		try {
			if (serverSocket != null) {
				serverSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		// Double ensuring that that the server is closed
		close();
	}
}
