package fr.pederobien.communication.impl;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.pederobien.communication.event.ConnectionDisposedEvent;
import fr.pederobien.communication.event.ConnectionEnableChangedEvent;
import fr.pederobien.communication.event.ConnectionLostEvent;
import fr.pederobien.communication.event.ConnectionUnstableEvent;
import fr.pederobien.communication.event.DataEvent;
import fr.pederobien.communication.event.RequestReceivedEvent;
import fr.pederobien.communication.interfaces.ICallbackMessage;
import fr.pederobien.communication.interfaces.ICallbackMessage.CallbackArgs;
import fr.pederobien.communication.interfaces.IConnection;
import fr.pederobien.communication.interfaces.IConnectionConfig;
import fr.pederobien.communication.interfaces.IHeaderMessage;
import fr.pederobien.communication.interfaces.IMessage;
import fr.pederobien.utils.BlockingQueueTask;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.LogEvent;

public abstract class Connection implements IConnection {
	private static final int MAX_EXCEPTION_NUMBER = 10;
	private IConnectionConfig config;
	private String name;
	private BlockingQueueTask<HeaderMessage> sendingQueue;
	private BlockingQueueTask<byte[]> extractingQueue;
	private BlockingQueueTask<Object> receivingQueue;
	private BlockingQueueTask<CallbackManagement> callbackQueue;
	private CallbackMessageManager messageManager;
	private AtomicBoolean isDisposed;
	private boolean isEnabled;
	private int sendingExceptionCounter, receivingExceptionCounter, extractingExceptionCounter;
	
	// When the synchronous send has been called
	private Semaphore semaphore;
	private CallbackArgs argument;
	
	/**
	 * Create an abstract connection that send asynchronously messages to the remote.
	 * 
	 * @param config The object that holds the connection configuration.
	 * @param mode Represent the direction of the connection.
	 */
	protected Connection(IConnectionConfig config, Mode mode) {
		this.config = config;
		name = mode == Mode.CLIENT_TO_SERVER ? "Client" : "Server";
		
		sendingQueue = new BlockingQueueTask<HeaderMessage>(String.format("%s[send]", toString()), message -> sendMessage(message));
		receivingQueue = new BlockingQueueTask<Object>(String.format("%s[receive]", toString()), object -> receiveMessage(object));
		extractingQueue = new BlockingQueueTask<byte[]>(String.format("%s[extract]", toString()), raw -> extractMessage(raw));
		callbackQueue = new BlockingQueueTask<CallbackManagement>(String.format("%s[callback]", toString()), management -> callbackMessage(management));

		messageManager = new CallbackMessageManager(this);
		isDisposed = new AtomicBoolean(false);

		sendingExceptionCounter = 0;
		receivingExceptionCounter = 0;
		extractingExceptionCounter = 0;
		isEnabled = true;
		
		semaphore = new Semaphore(1);
	}
	
	@Override
	public void initialise() throws Exception {
		// Waiting for a message to be sent		
		sendingQueue.start();
		
		// Waiting for receiving message from network
		receivingQueue.start();
		receivingQueue.add(new Object());
		
		// Waiting for data to extract
		extractingQueue.start();
		
		// Waiting for callback to be called
		callbackQueue.start();
		
		// Initializing layer
		config.getLayer().initialise(this);
	}
	
	@Override
	public void sendSync(ICallbackMessage message) {
		try {
			// Decrementing by 1 the number of permits of the semaphore
			semaphore.acquire();
			
			// Creating a layer to force the current thread to wait for the response from the remote.
			ICallbackMessage internalMessage = new CallbackMessage(message.getBytes(), message.getTimeout(), args -> {
				argument = args;
				semaphore.release();
			});
			
			// Sending asynchronously the message to the remote
			send(internalMessage);
			
			// Wait until the callback has been executed
			semaphore.acquire();
			
			// Executing callback
			message.getCallback().accept(argument);
			
		} catch (Exception e) {
			message.getCallback().accept(new CallbackArgs(null, true));
		} finally {
			// Always releasing the semaphore to allow another synchronous send
			semaphore.release();
		}
	}
	
	@Override
	public void send(IMessage message) {
		checkDisposed();

		if (isEnabled())
			sendingQueue.add(new HeaderMessage(message));
	}
	
	@Override
	public void send(ICallbackMessage message) {
		checkDisposed();
		
		if (isEnabled())
			sendCallbackRequest(0, message);
	}
	
	@Override
	public boolean isEnabled() {
		return isEnabled;
	}
	
	@Override
	public void setEnabled(boolean isEnabled) {
		if (this.isEnabled == isEnabled)
			return;
		
		this.isEnabled = isEnabled;
		EventManager.callEvent(new ConnectionEnableChangedEvent(this, isEnabled));
	}
	
	@Override
	public void dispose() {
		if (isDisposed.compareAndSet(false, true)) {
			sendingQueue.dispose();
			receivingQueue.dispose();
			extractingQueue.dispose();
			messageManager.dispose();

			EventManager.callEvent(new ConnectionDisposedEvent(this));
		}
	}
	
	@Override
	public String toString() {
		return String.format("[%s %s:%s]", name, config.getAddress(), config.getPort());
	}
	
	/**
	 * Execute the callback of the given callback management asynchronously.
	 * 
	 * @param management The management that hold the callback to execute.
	 */
	public void timeout(CallbackManagement management) {
		callbackQueue.add(management);
	}
	
	/**
	 * Connection specific implementation to send a message to the remote.
	 * The bytes array is the result of the layer that has encapsulated the payload
	 * with other information in order to be received correctly.
	 * 
	 * @param data The byte array to send to the remote.
	 */
	protected abstract void sendImpl(byte[] data) throws Exception;
	
	/**
	 * Connection specific implementation to receive bytes from the remote.
	 * 
	 * @param receivingBufferSize The size of the bytes array used to receive data from the remote.
	 */
	protected abstract byte[] receiveImpl(int receivingBufferSize) throws Exception;
	
	/**
	 * Throws an {@link IllegalStateException} if the connection is disposed. Do nothing otherwise.
	 */
	protected void checkDisposed() {
		if (isDisposed.get())
			throw new IllegalStateException("Object disposed");
	}
	
	/**
	 * Throw an UnstableConnectionEvent.
	 */
	protected void onUnstableConnection(String message) {
		setEnabled(false);
		
		EventManager.callEvent(new LogEvent(message));
		EventManager.callEvent(new ConnectionUnstableEvent(this));
	}
	
	/**
	 * Send asynchronously a message to the remote.
	 * 
	 * @param message The message to send.
	 */
	private void sendMessage(HeaderMessage message) {
		if (isEnabled()) {
			try {
				byte[] data = config.getLayer().pack(message);

				sendImpl(data);
	
				messageManager.start(message);
				sendingExceptionCounter = 0;
			} catch (Exception exception) {
				sendingExceptionCounter++;
				if (sendingExceptionCounter == MAX_EXCEPTION_NUMBER) {
					String formatter = "[%s:%s - send] Too much exceptions in a row, closing connection";
					onUnstableConnection(String.format(formatter, config.getAddress(), config.getPort()));
				}
			}
		}
	}
	
	/**
	 * Wait asynchronously for receiving data from the remote.
	 */
	private void receiveMessage(Object object) {
		if (isEnabled()) {
			byte[] raw = null;
			try {
				raw = receiveImpl(config.getReceivingBufferSize());
				
				// When raw is null, a problem happened while waiting for receiving data from the remote
				if (raw == null)
					EventManager.callEvent(new ConnectionLostEvent(this));
				else {
					EventManager.callEvent(new DataEvent(this, config.getAddress(), config.getPort(), raw));

					// Adding raw data for asynchronous extraction
					extractingQueue.add(raw);
				}

				receivingExceptionCounter = 0;
			} catch (Exception exception) {
				receivingExceptionCounter++;
				if (receivingExceptionCounter == MAX_EXCEPTION_NUMBER) {
					String formatter = "[%s:%s - receive] Too much exceptions in a row, closing connection";
					onUnstableConnection(String.format(formatter, config.getAddress(), config.getPort()));
				}
			} finally {
				// If connection is not lost
				if (raw != null ) {
					// Waiting again for the reception
					receivingQueue.add(new Object());
				}
			}
		}
	}
	
	/**
	 * Extract asynchronously the requests from the raw buffer.
	 * 
	 * @param raw The bytes array received from the remote.
	 */
	private void extractMessage(byte[] raw) {
		if (isEnabled()) {
			try {

				// Extracting requests from the raw bytes array received from the network
				List<IHeaderMessage> requests = config.getLayer().unpack(raw);
				
				// For each received request
				for (IHeaderMessage request : requests) {
					
					// A 0 identifier means it does not correspond to a response to a request
					if (request.getRequestID() != 0) {

						// Checking if there is a pending request
						CallbackManagement management = messageManager.unregister(request);
						
						// Receiving expected response from the remote
						if (management != null)
							callbackQueue.add(management);
					}

					// Receiving unexpected request from the remote, checking if it should be executed
					else if (config.isAllowUnexpectedRequest()) {
						RequestReceivedEvent event = new RequestReceivedEvent(this, config.getAddress(), config.getPort(), request.getBytes());
						EventManager.callEvent(event);
		
						if (event.getCallbackResponse() != null)
							sendCallbackRequest(request.getID(), event.getCallbackResponse());
						else if (event.getSimpleResponse() != null)
							sendingQueue.add(new HeaderMessage(request.getID(), event.getSimpleResponse()));
					}
				}
				
				extractingExceptionCounter = 0;
			} catch (Exception e) {
				extractingExceptionCounter++;
				if (extractingExceptionCounter == MAX_EXCEPTION_NUMBER) {
					String formatter = "[%s:%s - extract] Too much exceptions in a row, closing connection";
					onUnstableConnection(String.format(formatter, config.getAddress(), config.getPort()));
				}
			}
		}
	}
	
	/**
	 * Execute the callback of the given management and check if a request (ie a response) should be sent back to the remote.
	 * 
	 * @param management The management whose the callback must be executed.
	 */
	private void callbackMessage(CallbackManagement management) {
		if (isEnabled()) {
			CallbackArgs args = management.apply();
			if (args.getCallbackRequest() != null)
				sendCallbackRequest(management.getResponse().getID(), args.getCallbackRequest());
			else if (args.getSimpleRequest() != null)
				sendingQueue.add(new HeaderMessage(management.getResponse().getID(), args.getSimpleRequest()));
		}
	}
	
	/**
	 * Creates a header message to be sent to the remote.
	 * 
	 * @param requestID The Identifier of the request to respond to.
	 * @param message The message to send to the remote.
	 */
	private void sendCallbackRequest(int requestID, ICallbackMessage message) {
		HeaderMessage headerMessage = new HeaderMessage(requestID, message);
		messageManager.register(headerMessage);
		sendingQueue.add(headerMessage);
	}
}
