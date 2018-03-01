package edu.odu.jrugh001.yellowserver.net;

import edu.odu.jrugh001.yellowserver.ClientHandle;
import edu.odu.jrugh001.yellowserver.ClientManager;
import edu.odu.jrugh001.yellowserver.Server;
import edu.odu.jrugh001.yellowserver.net.listening.Listener;
import edu.odu.jrugh001.yellowserver.net.listening.Opcode;
import edu.odu.jrugh001.yellowserver.net.listening.Opcodes;

public class ManageChatMessages implements Listener {

	/**
     * Incoming chat message from the client.
	 */
	@Opcode(getOpcode = Opcodes.CHAT_BOX_MESSAGE)
	public void onChatMessage(ClientHandle clientHandle, Server server) {
		String message = clientHandle.readString();
		String username = ClientManager.getInstance().getClient(clientHandle).getUsername();
		ClientManager.getInstance().getClients().keySet().forEach(handle ->
				handle.write(Opcodes.CHAT_BOX_MESSAGE, outStream -> {
					outStream.writeUTF(username);
					outStream.writeUTF("42f4d9");
					outStream.writeUTF(message);
				})
			);
	}
}
