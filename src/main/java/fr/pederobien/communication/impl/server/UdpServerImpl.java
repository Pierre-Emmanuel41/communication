package fr.pederobien.communication.impl.server;

import fr.pederobien.communication.impl.EthernetEndPoint;
import fr.pederobien.communication.impl.connection.UdpConnectionImpl;
import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.communication.interfaces.connection.IUdpSocket;
import fr.pederobien.communication.interfaces.server.IClientInfo;
import fr.pederobien.communication.interfaces.server.IServerConfig;
import fr.pederobien.communication.interfaces.server.IServerImpl;

public class UdpServerImpl implements IServerImpl<IEthernetEndPoint> {
    private UdpServerSocket serverSocket;

    @Override
    public void open(IServerConfig<IEthernetEndPoint> config) throws Exception {
        serverSocket = new UdpServerSocket(config.getName(), config.getPoint().getAddress(), config.getPoint().getPort());

        // In case the port number from config is 0, the port number is defined by the host machine
        config.getPoint().setPort(serverSocket.getLocalPort());
    }

    @Override
    public void close() throws Exception {
        serverSocket.close();
    }

    @Override
    public IClientInfo<IEthernetEndPoint> waitForClient() throws Exception {
        IUdpSocket socket = serverSocket.accept();

        String address = socket.getInetAddress().getHostName();
        int port = socket.getInetAddress().getPort();

        // Creating remote end point
        EthernetEndPoint endPoint = new EthernetEndPoint(address, port);

        return new ClientInfo<IEthernetEndPoint>(endPoint, new UdpConnectionImpl(socket));
    }
}
