package fr.pederobien.communication.testing.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import fr.pederobien.communication.interfaces.connection.IConnection.Mode;
import fr.pederobien.communication.testing.tools.Network.Address;
import fr.pederobien.communication.testing.tools.Network.INetworkerCorruptor;

public class NetworkCorruptor implements INetworkerCorruptor {
	private int clientToServerCounter, serverToClientCounter;
	private List<Integer> clientToServerCorruptors;
	private List<Integer> serverToClientCorruptors;
	private Corruptor corruptor;

	/**
	 * Creates a network corruptor.
	 */
	public NetworkCorruptor() {
		clientToServerCounter = 0;
		serverToClientCounter = 0;

		clientToServerCorruptors = new ArrayList<Integer>();
		serverToClientCorruptors = new ArrayList<Integer>();

		corruptor = new Corruptor();
	}

	@Override
	public byte[] corrupt(Mode mode, Address remote, byte[] data) {
		if (mode == Mode.CLIENT_TO_SERVER) {
			if (clientToServerCorruptors.contains(clientToServerCounter)) {
				return corruptor.corrupt(data);
			}
			clientToServerCounter++;

		} else if (mode == Mode.SERVER_TO_CLIENT) {
			if (serverToClientCorruptors.contains(serverToClientCounter)) {
				return corruptor.corrupt(data);
			}

			serverToClientCounter++;
		}

		return data;
	}

	/**
	 * An internal counter is incremented each time a request is being sent from the
	 * client to the server. This method set if for a specific counter value the
	 * data should be corrupted.
	 * 
	 * @param counters A list of counter values for which data must be corrupted.
	 */
	public void registerClientToServerCorruption(int... counters) {
		for (int i = 0; i < counters.length; i++) {
			if (!clientToServerCorruptors.contains(counters[i])) {
				clientToServerCorruptors.add(counters[i]);
			}
		}
	}

	/**
	 * An internal counter is incremented each time a request is being sent from the
	 * server to the client. This method set if for a specific counter value the
	 * data should be corrupted.
	 * 
	 * @param counters A list of counter values for which data must be corrupted.
	 */
	public void registerServerToClientCorruption(int... counters) {
		for (int i = 0; i < counters.length; i++) {
			if (!serverToClientCorruptors.contains(counters[i])) {
				serverToClientCorruptors.add(counters[i]);
			}
		}
	}

	private static class Corruptor {

		/**
		 * Corrupt the input data randomly.
		 * 
		 * @param data The bytes array that contains data transfered to the remote.
		 * 
		 * @return The corrupted data.
		 */
		public byte[] corrupt(byte[] data) {
			Random random = new Random();
			for (int i = 0; i < 5; i++) {
				int index = random.nextInt(0, data.length);
				int value = random.nextInt(-127, 126);

				data[index] = (byte) value;
			}
			return data;
		}
	}
}
