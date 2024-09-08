package fr.pederobien.communication.testing;

import java.util.ArrayList;
import java.util.List;

import fr.pederobien.communication.impl.connection.HeaderMessage;
import fr.pederobien.communication.interfaces.IExchange;
import fr.pederobien.communication.interfaces.IHeaderMessage;
import fr.pederobien.communication.interfaces.ILayer;

public class ExceptionLayer implements ILayer {
	
	public enum LayerExceptionMode {
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

	@Override
	public boolean initialise(IExchange exchange) throws Exception {
		return true;
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
		messages.add(new HeaderMessage(raw));
		return messages;
	}

}
