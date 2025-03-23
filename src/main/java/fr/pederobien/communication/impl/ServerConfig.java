package fr.pederobien.communication.impl;

import fr.pederobien.communication.interfaces.connection.IConnection.Mode;
import fr.pederobien.communication.interfaces.server.IClientValidator;
import fr.pederobien.communication.interfaces.server.IServerConfig;

public class ServerConfig<T> extends Configuration implements IServerConfig<T> {
	private String name;
	private T point;
	private IClientValidator<T> clientValidator;

	/**
	 * Creates a configuration that holds the parameters for a server.
	 * 
	 * @param name  The server's name.
	 * @param point The properties of the server communication point.
	 */
	protected ServerConfig(String name, T point) {
		super(Mode.SERVER_TO_CLIENT);

		this.name = name;
		this.point = point;
		clientValidator = endPoint -> true;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public T getPoint() {
		return point;
	}

	@Override
	public IClientValidator<T> getClientValidator() {
		return clientValidator;
	}

	/**
	 * Set the server client validator.
	 * 
	 * @param clientValidator The validator to authorize a client to be connected to
	 *                        the server.
	 */
	public void setClientValidator(IClientValidator<T> clientValidator) {
		this.clientValidator = clientValidator;
	}
}
