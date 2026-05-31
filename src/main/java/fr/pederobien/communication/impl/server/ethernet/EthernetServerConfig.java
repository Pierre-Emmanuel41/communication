package fr.pederobien.communication.impl.server.ethernet;

import fr.pederobien.communication.impl.server.ServerConfig;
import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.communication.interfaces.server.IEthernetServerConfig;
import fr.pederobien.communication.interfaces.server.IServerEthernetEndPoint;

public class EthernetServerConfig extends ServerConfig<IServerEthernetEndPoint, IEthernetEndPoint> implements IEthernetServerConfig {

	/**
	 * Creates a server configuration for ethernet implementation.
	 * 
	 * @param name  The server's name.
	 * @param point The server's end point.
	 */
	public EthernetServerConfig(String name, IServerEthernetEndPoint point) {
		super(name, point);
	}
}
