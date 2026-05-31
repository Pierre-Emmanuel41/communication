package fr.pederobien.communication.impl.client.ethernet;

import fr.pederobien.communication.impl.client.ClientConfig;
import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.communication.interfaces.client.IEthernetClientConfig;

public class EthernetClientConfig extends ClientConfig<IEthernetEndPoint> implements IEthernetClientConfig {

	/**
	 * Creates a configuration for an Ethernet client.
	 * 
	 * @param name     The name of the client.
	 * @param endPoint The server end point.
	 */
	public EthernetClientConfig(String name, IEthernetEndPoint endPoint) {
		super(name, endPoint);
	}

}
