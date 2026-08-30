package fr.pederobien.communication.impl.server;

import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.communication.interfaces.server.IEthernetServer;
import fr.pederobien.communication.interfaces.server.IEthernetServerConfig;
import fr.pederobien.communication.interfaces.server.IEthernetServerImpl;
import fr.pederobien.communication.interfaces.server.IServerConfig;
import fr.pederobien.communication.interfaces.server.IServerEthernetEndPoint;
import fr.pederobien.communication.interfaces.server.IServerImpl;

public class EthernetServer extends Server<IServerEthernetEndPoint, IEthernetEndPoint> implements IEthernetServer {

	/**
	 * Creates an ethernet server.
	 * 
	 * @param config The object that holds the server configuration.
	 * @param impl   The server specific implementation to open/close the server.
	 */
	public EthernetServer(IServerConfig<IServerEthernetEndPoint, IEthernetEndPoint> config, IServerImpl<IServerEthernetEndPoint, IEthernetEndPoint> impl) {
		super(config, impl);
	}

	/**
	 * Creates an ethernet server.
	 * 
	 * @param config The object that holds the server configuration.
	 * @param impl   The server specific implementation to open/close the server.
	 */
	public EthernetServer(IEthernetServerConfig config, IEthernetServerImpl impl) {
		super(config, impl);
	}

	@Override
	public int getPort() {
		return isOpened() ? getConfig().getPoint().getPort() : -1;
	}
}
