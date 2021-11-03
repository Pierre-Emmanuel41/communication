package fr.pederobien.communication.impl;

import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.pederobien.communication.EConnectionState;
import fr.pederobien.communication.event.ConnectionLogEvent;
import fr.pederobien.communication.event.ConnectionLogEvent.ELogLevel;
import fr.pederobien.communication.event.DataReceivedEvent;
import fr.pederobien.communication.interfaces.IConnection;
import fr.pederobien.communication.interfaces.IConnectionOperation;
import fr.pederobien.utils.event.EventManager;

public abstract class ConnectionOperation implements IConnectionOperation {
	private EConnectionState state;
	private AtomicBoolean isDisposed;

	protected ConnectionOperation() {
		isDisposed = new AtomicBoolean(false);
	}

	@Override
	public EConnectionState getState() {
		return state;
	}

	@Override
	public boolean isDisposed() {
		return isDisposed.get();
	}

	/**
	 * Set the state of this connection.
	 * 
	 * @param state The current connection state.
	 */
	protected void setState(EConnectionState state) {
		this.state = state;
	}

	/**
	 * Check if the current state of this connection equals the specified state.
	 * 
	 * @param state The state to check.
	 * 
	 * @return True if and only if the current state equals the specified state.
	 */
	protected boolean isState(EConnectionState state) {
		return this.state == state;
	}

	/**
	 * Set the new value of the underlying {@link AtomicBoolean} isDispose attribute.
	 * 
	 * @param isDispose The new value.
	 * 
	 * @return True if the value is successfully updated, false otherwise.
	 */
	protected boolean setDisposed(boolean isDispose) {
		return isDisposed.compareAndSet(!isDispose, isDispose);
	}

	/**
	 * Throws an {@link IllegalStateException} if the connection is disposed. Do nothing otherwise.
	 */
	protected void checkDisposed() {
		if (isDisposed())
			throw new IllegalStateException("Object disposed");
	}

	/**
	 * Throw a {@link ConnectionLogEvent} based on the given parameters.
	 * 
	 * @param connection The connection source involved in this event.
	 * @param level      The log level.
	 * @param message    The log message.
	 * @param exception  The exception source of the log message.
	 */
	protected void onLogEvent(IConnection connection, ELogLevel level, String message, Exception exception) {
		EventManager.callEvent(new ConnectionLogEvent(connection, level, message, exception));
	}

	/**
	 * Throw a {@link ConnectionLogEvent} based on the given parameters.
	 * 
	 * @param connection The connection source involved in this event.
	 * @param level      The log level.
	 * @param message    The log message.
	 */
	protected void onLogEvent(IConnection connection, ELogLevel level, String message) {
		onLogEvent(connection, level, message, null);
	}

	/**
	 * Throw a {@link DataReceivedEvent} based on the given parameters.
	 * 
	 * @param connection The connection that received data.
	 * @param buffer     The raw buffer that contains the bytes received from the remote.
	 * @param length     The length of the raw data received from the remote.
	 */
	protected void onDataReceivedEvent(IConnection connection, byte[] buffer, int length) {
		EventManager.callEvent(new DataReceivedEvent(connection, buffer, length));
	}

	/**
	 * Cancel the specified task if not null.
	 * 
	 * @param task The task to cancel.
	 */
	protected void cancel(TimerTask task) {
		if (task != null)
			task.cancel();
	}

	protected enum Mode {
		CLIENT, SERVER
	}
}
