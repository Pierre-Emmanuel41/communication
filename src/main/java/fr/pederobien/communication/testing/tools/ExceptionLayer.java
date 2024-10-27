package fr.pederobien.communication.testing.tools;

import java.util.ArrayList;
import java.util.List;

import fr.pederobien.communication.impl.connection.HeaderMessage;
import fr.pederobien.communication.impl.connection.Message;
import fr.pederobien.communication.interfaces.connection.IHeaderMessage;
import fr.pederobien.communication.interfaces.layer.ILayer;

public class ExceptionLayer implements ILayer {

	public enum LayerExceptionMode {
		NONE,
		PACK,
		UNPACK
	}

	private LayerExceptionMode mode;

	/**
	 * Creates a layer that throw an exception when the pack/unpack method is called.
	 * 
	 * @param mode The exception mode of the layer.
	 */
	public ExceptionLayer(LayerExceptionMode mode) {
		this.mode = mode;
	}

	/**
	 * Creates a layer that does not throw any exception when the pack/unpack method is called.
	 */
	public ExceptionLayer() {
		this(LayerExceptionMode.NONE);
	}

	@Override
	public byte[] pack(IHeaderMessage message) throws Exception {
		if (mode == LayerExceptionMode.PACK)
			throw new RuntimeException("Exception to test unstable counter");

		return message.getBytes();
	}

	@Override
	public List<IHeaderMessage> unpack(byte[] raw) throws Exception {
		if (mode == LayerExceptionMode.UNPACK)
			throw new RuntimeException("Exception to test unstable counter");

		List<IHeaderMessage> messages = new ArrayList<IHeaderMessage>();
		messages.add(new HeaderMessage(0, new Message(raw)));
		return messages;
	}
}
