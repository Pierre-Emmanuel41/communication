package fr.pederobien.communication;

public enum EConnectionState {

	/**
	 * Trying to connect to remote following a Connect() command, or following a disconnection event.
	 **/
	CONNECTING,

	/**
	 * Connection is existing with remote.
	 **/
	CONNECTED,

	/**
	 * Trying to disconnect from remote following a Disconnect() command, or following a disconnection event.
	 **/
	DISCONNECTING,

	/**
	 * No connection is existing with remote. No request to perform any connection sent.
	 **/
	DISCONNECTED,

	/**
	 * The connection with the remote has been lost.
	 **/
	CONNECTION_LOST
}
