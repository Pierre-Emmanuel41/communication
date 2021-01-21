package fr.pederobien.communication.interfaces;

import fr.pederobien.utils.IObservable;

public interface ITcpConnection extends IConnection<ICallbackRequestMessage>, IObservable<IObsTcpConnection> {
}
