package fr.pederobien.communication.impl.server;

import fr.pederobien.communication.impl.EthernetEndPoint;
import fr.pederobien.communication.impl.connection.TcpConnectionImpl;
import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.communication.interfaces.server.IClientInfo;
import fr.pederobien.communication.interfaces.server.IServerConfig;
import fr.pederobien.communication.interfaces.server.IServerImpl;

import java.net.InetAddress;
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
        String address = config.getPoint().getAddress();
        int port = config.getPoint().getPort();

        // Note: The port number does not matter, if the value is out of range, the socket will throw an exception
        // if the value is 0, the host machine will choose an ephemeral (ie first free) port.

        // Case 1: Any address
        if (address.equals("*")) {
            serverSocket = new ServerSocket(port);
        }
        // Case 2: Specific hostname
        else {
            InetAddress netAddress = InetAddress.getByName(address);
            serverSocket = new ServerSocket(port, 50, netAddress);
        }

        // In case the port number from config is 0, the port number is defined by the host machine
        config.getPoint().setPort(serverSocket.getLocalPort());
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
