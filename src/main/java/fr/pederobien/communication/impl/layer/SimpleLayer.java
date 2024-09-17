package fr.pederobien.communication.impl.layer;

import java.util.ArrayList;
import java.util.List;

import fr.pederobien.communication.impl.connection.HeaderMessage;
import fr.pederobien.communication.interfaces.IExchange;
import fr.pederobien.communication.interfaces.IHeaderMessage;
import fr.pederobien.communication.interfaces.ILayer;
import fr.pederobien.utils.ByteWrapper;
import fr.pederobien.utils.ReadableByteWrapper;

public class SimpleLayer implements ILayer {
	private Encapsuler encapsuler;
	
	/**
	 * Creates a layer in order to extract several responses from raw data received from the remote.
	 */
	public SimpleLayer() {
		encapsuler = new Encapsuler("(~@=", "#.?)");
	}

	@Override
	public boolean initialise(IExchange exchange) {
		return true;
	}

	@Override
	public byte[] pack(IHeaderMessage message) throws Exception {
		ByteWrapper wrapper = ByteWrapper.create();
		wrapper.putInt(message.getIdentifier());
		wrapper.putInt(message.getRequestID());
		wrapper.putInt(message.getBytes().length);
		wrapper.put(message.getBytes());
		return encapsuler.pack(wrapper.get());
	}

	@Override
	public List<IHeaderMessage> unpack(byte[] raw) throws Exception {
		List<IHeaderMessage> requests = new ArrayList<IHeaderMessage>();

		List<byte[]> messages = encapsuler.unpack(raw);	
		for (byte[] message : messages) {
			// Structure of a message:
			// bytes 0 -> 3: ID
			// bytes 4 -> 7: requestID
			// byte 8 -> 11: length
			// byte 12 -> 12 + length: payload
			
			ReadableByteWrapper wrapper = ReadableByteWrapper.wrap(message);
			
			// bytes 0 -> 3: ID
			int ID = wrapper.nextInt();
			
			// bytes 4 -> 7: requestID
			int requestID = wrapper.nextInt();
			
			// byte 8 -> 11: length
			int length = wrapper.nextInt();
			
			// bytes 12 -> 12 + length: payload
			byte[] payload = wrapper.next(length);
			
			// Creating a header message
			requests.add(new HeaderMessage(ID, requestID, payload));
		}
		
		return requests;
	}
}
