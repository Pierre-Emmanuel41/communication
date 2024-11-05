package fr.pederobien.communication.impl.layer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import fr.pederobien.communication.impl.connection.HeaderMessage;
import fr.pederobien.communication.interfaces.connection.IHeaderMessage;
import fr.pederobien.communication.interfaces.layer.ICertificate;
import fr.pederobien.communication.interfaces.layer.ILayer;
import fr.pederobien.utils.ByteWrapper;
import fr.pederobien.utils.ReadableByteWrapper;

public class CertifiedLayer implements ILayer {
	private ICertificate certificate;
	private Encapsuler encapsuler;
	private Function<byte[], byte[]> preSigning, postSigning;
	private Function<byte[], byte[]> preAuthentication, postAuthentication;

	/**
	 * Creates a layer that sign each message using the given certificate.
	 * 
	 * @param certificate The certificate to sign or authenticate a message.
	 */
	public CertifiedLayer(ICertificate certificate) {
		this.certificate = certificate;

		preSigning = data -> data;
		postSigning = data -> data;
		preAuthentication = data -> data;
		postAuthentication = data -> data;

		encapsuler = new Encapsuler("(~@=", "#.?)");
	}

	/**
	 * Set the action to perform before authenticating a bytes array.
	 * 
	 * @param preAuthentication The action to perform.
	 */
	public void setPreAuthentication(Function<byte[], byte[]> preAuthentication) {
		this.preAuthentication = preAuthentication;
	}

	/**
	 * Set the action to perform after authenticating a bytes array.
	 * 
	 * @param postAuthentication The action to perform.
	 */
	public void setPostAuthentication(Function<byte[], byte[]> postAuthentication) {
		this.postAuthentication = postAuthentication;
	}

	/**
	 * Set the action to perform before signing a bytes array.
	 * 
	 * @param preSigning The action to perform.
	 */
	public void setPreSigning(Function<byte[], byte[]> preSigning) {
		this.preSigning = preSigning;
	}

	/**
	 * Set the action to perform after signing a bytes array.
	 * 
	 * @param postSigning The action to perform.
	 */
	public void setPostSigning(Function<byte[], byte[]> postSigning) {
		this.postSigning = postSigning;
	}

	@Override
	public byte[] pack(IHeaderMessage message) throws Exception {
		ByteWrapper wrapper = ByteWrapper.create();
		wrapper.putInt(message.getIdentifier());
		wrapper.putInt(message.getRequestID());
		wrapper.putInt(message.getBytes().length);
		wrapper.put(message.getBytes());

		return encapsuler.pack(postSigning.apply(certificate.sign(preSigning.apply(wrapper.get()))));
	}

	@Override
	public List<IHeaderMessage> unpack(byte[] raw) throws Exception {
		List<IHeaderMessage> requests = new ArrayList<IHeaderMessage>();

		List<byte[]> signedMessages = encapsuler.unpack(raw);	
		for (byte[] signed : signedMessages) {

			byte[] message = postAuthentication.apply(certificate.authenticate(preAuthentication.apply(signed)));

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
