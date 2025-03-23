package fr.pederobien.communication.interfaces.server;

import fr.pederobien.communication.interfaces.IConfiguration;

public interface IServerConfig<T> extends IConfiguration {

	/**
	 * @return The name of the server.
	 */
	String getName();

	/**
	 * @return The properties of the server communication point.
	 */
	T getPoint();

	/**
	 * @return The validator to authorize or not the client to be connected to the
	 *         server.
	 */
	IClientValidator<T> getClientValidator();
}
