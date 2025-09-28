package fr.pederobien.communication.testing.tools;

import fr.pederobien.communication.interfaces.connection.IConnection.Mode;
import fr.pederobien.communication.testing.tools.Network.Address;
import fr.pederobien.communication.testing.tools.Network.INetworkerCorrupter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NetworkCorrupter implements INetworkerCorrupter {
	private final List<Integer> clientToServerCorrupters;
	private final List<Integer> serverToClientCorrupters;
	private final Corrupter corruptor;
	private int clientToServerCounter, serverToClientCounter;

	/**
	 * Creates a network corrupter.
	 */
	public NetworkCorrupter() {
		clientToServerCounter = 0;
		serverToClientCounter = 0;

		clientToServerCorrupters = new ArrayList<Integer>();
		serverToClientCorrupters = new ArrayList<Integer>();

		corruptor = new Corrupter();
	}

	@Override
	public byte[] corrupt(Mode mode, Address remote, byte[] data) {
		if (mode == Mode.CLIENT_TO_SERVER) {
			if (clientToServerCorrupters.contains(clientToServerCounter)) {
				return corruptor.corrupt(data);
			}
			clientToServerCounter++;

		} else if (mode == Mode.SERVER_TO_CLIENT) {
			if (serverToClientCorrupters.contains(serverToClientCounter)) {
				return corruptor.corrupt(data);
			}

			serverToClientCounter++;
		}

		return data;
	}

	/**
	 * An internal counter is incremented each time a request is being sent from the client to the server. This method set if for a
	 * specific counter value the data should be corrupted.
	 *
	 * @param counters A list of counter values for which data must be corrupted.
	 */
	public void registerClientToServerCorruption(int... counters) {
		for (int counter : counters) {
			if (!clientToServerCorrupters.contains(counter)) {
				clientToServerCorrupters.add(counter);
			}
		}
	}

	/**
	 * An internal counter is incremented each time a request is being sent from the server to the client. This method set if for a
	 * specific counter value the data should be corrupted.
	 *
	 * @param counters A list of counter values for which data must be corrupted.
	 */
	public void registerServerToClientCorruption(int... counters) {
		for (int counter : counters) {
			if (!serverToClientCorrupters.contains(counter)) {
				serverToClientCorrupters.add(counter);
			}
		}
	}

	private static class Corrupter {

		/**
		 * Corrupt the input data randomly.
		 *
		 * @param data The bytes array that contains data transferred to the remote.
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
