package fr.pederobien.communication.testing.tools;

import fr.pederobien.communication.interfaces.layer.ICertificate;
import fr.pederobien.utils.ByteWrapper;
import fr.pederobien.utils.ReadableByteWrapper;

public class SimpleCertificate implements ICertificate {

    @Override
    public byte[] sign(byte[] message) {
        int checksum = checksum(message);
        return ByteWrapper.wrap(message).putInt(checksum).get();
    }

    @Override
    public byte[] authenticate(byte[] message) {
        ReadableByteWrapper wrapper = ReadableByteWrapper.wrap(message);
        byte[] unsigned = wrapper.next(message.length - 4);
        int checksum = wrapper.nextInt();
        return (checksum(unsigned) == checksum) ? unsigned : null;
    }

    /**
     * Sum all the bytes of the array.
     *
     * @param message The message from which a checksum is computed.
     * @return The checksum of the message.
     */
    private int checksum(byte[] message) {
        int checksum = 0;

        for (byte value : message)
            checksum += value;

        return checksum;
    }
}
