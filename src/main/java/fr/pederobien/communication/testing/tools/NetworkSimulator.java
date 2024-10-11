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

	private Mode mode;
	private IModifier modifier;
	private int counter;

	/**
	 * Creates a data modifier. It will modify the data sent from one point to another point.
	 * 
	 * @param mode The direction of the communication on which this modifier is listening.
	 * @param modifier To modify the data sent from one point to another point.
	 */
	public NetworkSimulator(Mode mode, IModifier modifier) {
		this.mode = mode;
		this.modifier = modifier;
	}

	@Override
	public byte[] simulate(Mode mode, Address remote, byte[] data) {
		if (this.mode != mode)
			return data;

		counter++;
		return modifier.modify(counter, data);
	}
}
