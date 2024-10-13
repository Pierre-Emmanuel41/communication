package fr.pederobien.communication.impl.connection;

import java.util.List;
import java.util.concurrent.Semaphore;

import fr.pederobien.communication.event.ConnectionDisposedEvent;
import fr.pederobien.communication.event.ConnectionEnableChangedEvent;
import fr.pederobien.communication.event.ConnectionLostEvent;
import fr.pederobien.communication.event.ConnectionUnstableEvent;
import fr.pederobien.communication.event.DataEvent;
import fr.pederobien.communication.event.RequestReceivedEvent;
import fr.pederobien.communication.impl.Exchange;
import fr.pederobien.communication.interfaces.ICallback.CallbackArgs;
import fr.pederobien.communication.interfaces.IConnection;
import fr.pederobien.communication.interfaces.IConnectionConfig;
import fr.pederobien.communication.interfaces.IHeaderMessage;
import fr.pederobien.communication.interfaces.IMessage;
import fr.pederobien.utils.Disposable;
import fr.pederobien.utils.IDisposable;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.LogEvent;
import fr.pederobien.utils.event.LogEvent.ELogLevel;

public abstract class Connection implements IConnection {
	private static final int MAX_EXCEPTION_NUMBER = 10;
	private IConnectionConfig config;
	private QueueManager queueManager;
	private CallbackManager callbackManager;
	private IDisposable disposable;
	private boolean isEnabled;
	private int sendingExceptionCounter;
	private int receivingExceptionCounter;
	private int extractingExceptionCounter;
	private int callbackExceptionCounter;
	private int requestExceptionCounter;
	
	// For connection initialization
	private boolean initialized;
	private Exchange exchange;
	
	// When the synchronous send has been called
	private Semaphore semaphore;
	private CallbackArgs argument;
	
	/**
	 * Create an abstract connection that send asynchronously messages to the remote.
	 * 
	 * @param config The object that holds the connection configuration.
	 */
	protected Connection(IConnectionConfig config) {
		this.config = config;
		
		queueManager = new QueueManager(config.getAddress(), config.getPort(), config.getMode());
		queueManager.setSendingQueue(message -> sendMessage(message));
		queueManager.setReceivingQueue(object -> receiveMessage(object));
		queueManager.setExtractingQueue(raw -> extractMessage(raw));
		queueManager.setCallbackQueue(management -> callbackMessage(management));
		queueManager.setRequestReceivedQueue(event -> dispatchRequestReceivedEvent(event));

		callbackManager = new CallbackManager(this);
		disposable = new Disposable();

		sendingExceptionCounter = 0;
		receivingExceptionCounter = 0;
		extractingExceptionCounter = 0;
		callbackExceptionCounter = 0;
		requestExceptionCounter = 0;
		isEnabled = true;
		initialized = false;

		exchange = new Exchange(this);
		semaphore = new Semaphore(0);
	}
	
	@Override
	public boolean initialise() throws Exception {
		// Start each asynchronous queue
		queueManager.start();
		
		// Initializing layer
		return initialized = getConfig().getLayer().initialise(exchange);
	}
	
	@Override
	public void send(IMessage message) {
		disposable.checkDisposed();

		if (isEnabled())
			send(0, message);
	}
	
	@Override
	public void answer(int requestID, IMessage message) {
		disposable.checkDisposed();

		if (isEnabled())
			send(requestID, message);
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
		if (disposable.dispose()) {
			disposeImpl();

			// Dispose each asynchronous queue
			queueManager.dispose();
			callbackManager.dispose();

			EventManager.callEvent(new ConnectionDisposedEvent(this));
		}
	}
	
	@Override
	public boolean isDisposed() {
		return disposable.isDisposed();
	}

	@Override
	public IConnectionConfig getConfig() {
		return config;
	}
	
	@Override
	public String toString() {
		String name = getConfig().getMode() == Mode.CLIENT_TO_SERVER ? "Server" : "Client";
		return String.format("[%s %s:%s]", name, getConfig().getAddress(), getConfig().getPort());
	}
	
	/**
	 * Execute the callback of the given callback management asynchronously.
	 * 
	 * @param management The management that hold the callback to execute.
	 */
	public void timeout(CallbackManagement management) {
		queueManager.getCallbackQueue().add(management);
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
	 * Connection specific implementation to close definitively the connection with the remote.
	 */
	protected abstract void disposeImpl();
	
	/**
	 * Throw an unstable connection event.
	 * 
	 * @param algo The unstable algorithm.
	 */
	protected void onUnstableConnection(String algo) {
		String name = getConfig().getMode() == Mode.CLIENT_TO_SERVER ? "Client" : "Server";
		String formatter = "[%s %s:%s (%s)] - Too much exceptions in a row, closing connection";
		String message = String.format(formatter, name, getConfig().getAddress(), getConfig().getPort(), algo);
		EventManager.callEvent(new LogEvent(ELogLevel.ERROR, message));

		setEnabled(false);
		EventManager.callEvent(new ConnectionUnstableEvent(this));
	}
	
	/**
	 * Send asynchronously a message to the remote.
	 * 
	 * @param message The message to send.
	 */
	private void sendMessage(IHeaderMessage message) {
		if (isEnabled()) {
			try {
				byte[] data = getConfig().getLayer().pack(message);

				callbackManager.start(message.getIdentifier());
				sendImpl(data);
	
				sendingExceptionCounter = 0;
			} catch (Exception exception) {
				sendingExceptionCounter++;
				checkUnstable(sendingExceptionCounter, "send");
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
				raw = receiveImpl(getConfig().getReceivingBufferSize());
				
				// When raw is null, a problem happened while waiting for receiving data from the remote
				if (raw == null)
					EventManager.callEvent(new ConnectionLostEvent(this));
				else {
					EventManager.callEvent(new DataEvent(this, raw));

					// Adding raw data for asynchronous extraction
					queueManager.getExtractingQueue().add(raw);
				}

				receivingExceptionCounter = 0;
			} catch (Exception exception) {
				receivingExceptionCounter++;
				checkUnstable(receivingExceptionCounter, "receive");

				if (!isDisposed())
					// Waiting again for the reception
					queueManager.getReceivingQueue().add(new Object());
			} finally {
				// If connection is not lost
				if (raw != null) {
					// Waiting again for the reception
					queueManager.getReceivingQueue().add(new Object());
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
				List<IHeaderMessage> requests = getConfig().getLayer().unpack(raw);
				
				// For each received request
				for (IHeaderMessage request : requests) {
					
					// A 0 identifier means it does not correspond to a response to a request
					if (request.getRequestID() != 0) {

						// Checking if there is a pending request
						CallbackManagement management = callbackManager.unregister(request);
						
						// Receiving expected response from the remote
						if (management != null)
							queueManager.getCallbackQueue().add(management);
					}

					else
						// Dispatching asynchronously a request received event.
						queueManager.getRequestReceivedQueue().add(new RequestReceivedEvent(this, request.getBytes(), request.getIdentifier()));
				}

				extractingExceptionCounter = 0;
			} catch (Exception e) {
				extractingExceptionCounter++;
				checkUnstable(extractingExceptionCounter, "extract");
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
			try {
				management.apply();

				callbackExceptionCounter = 0;
			} catch (Exception e) {
				callbackExceptionCounter++;
				checkUnstable(callbackExceptionCounter, "callback");
			}
		}
	}
	
	/**
	 * Creates internally a request received event and dispatch it to the request received handler.
	 * 
	 * @param event The event to dispatch.
	 */
	private void dispatchRequestReceivedEvent(RequestReceivedEvent event) {
		if (isEnabled()) {
			try {
				if (!initialized)
					exchange.notify(event);
				else if (getConfig().isAllowUnexpectedRequest())
					getConfig().getRequestReceivedHandler().onRequestReceivedEvent(event);

				requestExceptionCounter = 0;
			} catch (Exception e) {
				requestExceptionCounter++;
				checkUnstable(requestExceptionCounter, "dispatcher");
			}
		}
	}
	
	/**
	 * Creates a header message to be sent to the remote.
	 * 
	 * @param requestID The Identifier of the request to respond to.
	 * @param message The message to send to the remote.
	 */
	private void send(int requestID, IMessage message) {
		IMessage toSend = message;

		if (message.isSync()) {
			int timeout = message.getCallback().getTimeout() == -1 ? 10 : message.getCallback().getTimeout();
			toSend = new Message(message.getBytes(), message.isSync(), timeout, args -> {
				argument = args;
				semaphore.release();
			});
		}

		IHeaderMessage header = new HeaderMessage(requestID, toSend);
		callbackManager.register(header.getIdentifier(), toSend);
		queueManager.getSendingQueue().add(header);

		if (message.isSync()) {
			try {
				// Wait until the callback has been executed
				semaphore.acquire();
			} catch (Exception e) {
				argument = new CallbackArgs(-1, null, true);
			} finally {
				// Always draining the semaphore to force the synchronous send
				semaphore.drainPermits();
			}

			// Executing callback
			if (message.getCallback().getTimeout() != -1)
				message.getCallback().apply(argument);
		}
	}
	
	/**
	 * Check if the value of the counter equals the maximum exception counter. If so, an unstable connection event
	 * is thrown.
	 * 
	 * @param counter The counter to check.
	 * @param algo The unstable algorithm.
	 */
	private void checkUnstable(int counter, String algo) {
		if (counter == MAX_EXCEPTION_NUMBER)
			onUnstableConnection(algo);
	}
}
