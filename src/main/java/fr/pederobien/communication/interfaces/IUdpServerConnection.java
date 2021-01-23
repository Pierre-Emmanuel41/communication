package fr.pederobien.communication.interfaces;

import fr.pederobien.utils.IObservable;

public interface IUdpServerConnection extends IConnection<IAddressMessage>, IObservable<IObsConnection> {

}
