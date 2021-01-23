package fr.pederobien.communication.interfaces;

import fr.pederobien.utils.IObservable;

public interface IUdpConnection extends IConnection<IRequestMessage>, IObservable<IObsConnection> {

}
