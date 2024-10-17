package fr.pederobien.communication.impl.layer;

import java.util.ArrayList;
import java.util.List;

import fr.pederobien.communication.impl.connection.HeaderMessage;
import fr.pederobien.communication.interfaces.ICertificate;
import fr.pederobien.communication.interfaces.IHeaderMessage;
import fr.pederobien.communication.interfaces.ILayer;
import fr.pederobien.communication.interfaces.IToken;
import fr.pederobien.utils.ByteWrapper;
import fr.pederobien.utils.ReadableByteWrapper;

public class CertifiedLayer implements ILayer {
	private ICertificate certificate;
	private Encapsuler encapsuler;
	
	/**
	 * Creates a layer that sign each message using the given certificate.
	 * 
	 * @param certificate The certificate to sign or authenticate a message.
	 */
	public CertifiedLayer(ICertificate certificate) {
		this.certificate = certificate;
		
		encapsuler = new Encapsuler("(~@=", "#.?)");
	}
	
	@Override
	public boolean initialise(IToken token) throws Exception {
		return true;
	}

	@Override
	public byte[] pack(IHeaderMessage message) throws Exception {
		ByteWrapper wrapper = ByteWrapper.create();
		wrapper.putInt(message.getIdentifier());
		wrapper.putInt(message.getRequestID());
		wrapper.putInt(message.getBytes().length);
		wrapper.put(message.getBytes());
		return encapsuler.pack(certificate.sign(wrapper.get()));
	}

	@Override
	public List<IHeaderMessage> unpack(byte[] raw) throws Exception {
		List<IHeaderMessage> requests = new ArrayList<IHeaderMessage>();

		List<byte[]> signedMessages = encapsuler.unpack(raw);	
		for (byte[] signed : signedMessages) {
			
			byte[] message = certificate.authenticate(signed);
			
			// Message corrupted
			if (message == null)
				continue;
			
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
