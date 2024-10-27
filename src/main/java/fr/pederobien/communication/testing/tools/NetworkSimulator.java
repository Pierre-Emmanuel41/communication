package fr.pederobien.communication.testing.tools;

import fr.pederobien.communication.interfaces.IConnection.Mode;
import fr.pederobien.communication.testing.tools.Network.Address;
import fr.pederobien.communication.testing.tools.Network.INetworkSimulator;

public class NetworkSimulator implements INetworkSimulator {

	public static interface IModifier {

		/**
		 * Modify or not the data sent by a client to a server.
		 * 
		 * @param counter The counter value incremented each time a request is sent from a client to a server.
		 * @param data The data sent from a client to a server.
		 * 
		 * @return The data a server should receive.
		 */
		byte[] modify(int counter, byte[] data);
	}

	private IModifier clientToServerModifier, serverToClientModifier;
	private int clientToServerCounter, serverToClientCounter;

	/**
	 * Creates a network simulator. It will modify the data sent from one point to another point.
	 * 
	 * @param clientToServerModifier The modification to apply when the direction of communication
	 *                               is CLIENT_TO_SERVER.
	 * @param serverToClientModifier The modification to apply when the direction of communication
	 *                               is SERVER_TO_CLIENT.
	 */
	public NetworkSimulator(IModifier clientToServerModifier, IModifier serverToClientModifier) {
		this.clientToServerModifier = clientToServerModifier;
		this.serverToClientModifier = serverToClientModifier;
	}

	@Override
	public byte[] simulate(Mode mode, Address remote, byte[] data) {
		if (mode == Mode.CLIENT_TO_SERVER) {
			clientToServerCounter++;
			if (clientToServerModifier != null)
				return clientToServerModifier.modify(clientToServerCounter, data);
		}
		else if (mode == Mode.SERVER_TO_CLIENT) {
			serverToClientCounter++;
			if (serverToClientModifier != null)
				return serverToClientModifier.modify(serverToClientCounter, data);
		}

		return data;
	}
}
