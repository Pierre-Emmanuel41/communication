package fr.pederobien.communication.impl.layer;

import fr.pederobien.utils.ByteWrapper;
import fr.pederobien.utils.ReadableByteWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Splitter {
    private final int maximum;
    private final Map<Integer, ByteWrapper> remaining;

    /**
     * Creates a splitter. If the size of a data is greater than the maximum value,
     * the data is split into different packet.
     *
     * @param maximum The maximum size of a packet.
     */
    public Splitter(int maximum) {
        this.maximum = maximum;

        remaining = new HashMap<Integer, ByteWrapper>();
    }

    /**
     * Creates one or more packets from the input data depending upon the data size is
     * greater than a specific value.
     *
     * @param ID   The ID of the packet.
     * @param data The bytes array to split if necessary.
     * @return A list of packets corresponding the to split array with additional
     * information.
     */
    public List<byte[]> pack(int ID, byte[] data) {
        List<byte[]> packets = new ArrayList<byte[]>();
        int full = data.length / maximum;
        int numberOfByteInLastPacket = data.length % maximum;
        int total = full + (numberOfByteInLastPacket > 0 ? 1 : 0);
        ReadableByteWrapper wrapper = ReadableByteWrapper.wrap(data);

        for (int current = 0; current < total; current++) {
            ByteWrapper packet = ByteWrapper.create();

            // bytes 0 -> 3: ID
            packet.putInt(ID);

            // bytes 4 -> 7: total
            packet.putInt(total);

            // byte 8 -> 11: current
            packet.putInt(current);

            int length = current < total - 1 ? maximum : numberOfByteInLastPacket;

            // bytes 12 -> 15: length
            packet.putInt(length);

            // bytes 16 -> 16 + length: payload
            packet.put(wrapper.next(length));

            packets.add(packet.get());
        }

        return packets;
    }

    /**
     * Concatenates the list of packets according to their identifiers.
     *
     * @param messages A list of bytes array to concatenate.
     * @return A list of messages received entirely.
     */
    public Map<Integer, byte[]> unpack(List<byte[]> messages) {
        Map<Integer, byte[]> requests = new HashMap<Integer, byte[]>();

        // Step 2: Concatenating messages into requests
        for (byte[] message : messages) {
            // Structure of a message:
            ReadableByteWrapper readable = ReadableByteWrapper.wrap(message);

            // bytes 0 -> 3: ID
            int ID = readable.nextInt();

            // bytes 4 -> 7: total
            int total = readable.nextInt();

            // byte 8 -> 11: current
            int current = readable.nextInt();

            // bytes 12 -> 15: length
            int length = readable.nextInt();

            // bytes 16 -> 26 + length: payload
            byte[] payload = readable.next(length);

            // Original request to split in different packets
            if (total == 1)
                requests.put(ID, payload);
            else {
                ReadableByteWrapper wrapper = registerRequest(ID, total, current, payload);
                if (wrapper != null)
                    requests.put(ID, wrapper.get());
            }
        }

        return requests;
    }

    /**
     * If a request is already registered for the given ID, the payload is added at
     * the end of the previous payload. When the value of current correspond to
     * total - 1, all the packets have been received. The request has been received
     * entirely and can be used. If no request has been received for the given ID,
     * the payload will be stored until a new packet is received.
     *
     * @param ID      The identifier of this request.
     * @param total   The number of packets to receive in order to receive a full
     *                request.
     * @param current The current packet number.
     * @param payload The payload of the request.
     * @return A byte wrapper if the message has been received entirely.
     */
    private ReadableByteWrapper registerRequest(int ID, int total, int current, byte[] payload) {
        ByteWrapper wrapper = remaining.get(ID);

        // No request is registered
        if (wrapper == null)
            remaining.put(ID, ByteWrapper.wrap(payload));
        else {
            wrapper.put(payload);

            // Request received entirely
            if (current == total - 1) {
                return wrapper.getAsReadableWrapper();
            }
        }

        return null;
    }
}
