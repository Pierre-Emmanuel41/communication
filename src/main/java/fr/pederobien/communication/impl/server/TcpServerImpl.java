package fr.pederobien.communication.impl.server;

import fr.pederobien.communication.impl.EthernetEndPoint;
import fr.pederobien.communication.impl.connection.TcpConnectionImpl;
import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.communication.interfaces.server.IClientInfo;
import fr.pederobien.communication.interfaces.server.IServerConfig;
import fr.pederobien.communication.interfaces.server.IServerImpl;

import java.net.ServerSocket;
import java.net.Socket;

public class TcpServerImpl implements IServerImpl<IEthernetEndPoint> {
    private ServerSocket serverSocket;

    /**
     * Creates a TCP implementation for a server.
     */
    public TcpServerImpl() {
        // Do nothing
    }

    @Override
    public void open(IServerConfig<IEthernetEndPoint> config) throws Exception {
        serverSocket = new ServerSocket(config.getPoint().getPort());
    }

    @Override
    public void close() throws Exception {
        serverSocket.close();
    }

    @Override
    public IClientInfo<IEthernetEndPoint> waitForClient() throws Exception {
        // Waiting for a new client
        Socket socket = serverSocket.accept();

        String address = socket.getInetAddress().getHostName();
        int port = socket.getPort();

        // Creating remote end point
        EthernetEndPoint endPoint = new EthernetEndPoint(address, port);

        return new ClientInfo<IEthernetEndPoint>(endPoint, new TcpConnectionImpl(socket));
    }
}
