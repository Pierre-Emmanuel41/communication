package fr.pederobien.communication.impl.connection;

import fr.pederobien.communication.interfaces.connection.IConnectionImpl;
import fr.pederobien.communication.interfaces.connection.IUdpSocket;
import fr.pederobien.utils.ByteWrapper;

import java.net.DatagramPacket;

public class UdpConnectionImpl implements IConnectionImpl {
    private final IUdpSocket socket;

    /**
     * Creates a connection specific for UDP protocol.
     *
     * @param socket The socket to use to send/receive data from the remote.
     */
    public UdpConnectionImpl(IUdpSocket socket) {
        this.socket = socket;
    }

    @Override
    public void send(byte[] data) throws Exception {
        socket.send(data);
    }

    @Override
    public byte[] receive() throws Exception {
        DatagramPacket packet = socket.receive();

        // Connection lost
        if (packet == null) {
            return null;
        }

        return ByteWrapper.wrap(packet.getData()).extract(0, packet.getLength());
    }

    @Override
    public void dispose() {
        if (socket == null) {
            return;
        }

        socket.close();
    }
}
