package fr.pederobien.communication.interfaces;

import fr.pederobien.communication.EConnectionState;
import fr.pederobien.utils.IObservable;

public interface ITcpConnection extends IConnection<ICallbackRequestMessage>, IObservable<IObsConnection> {

	/**
	 * @return The current connection state.
	 */
	EConnectionState getState();
}
