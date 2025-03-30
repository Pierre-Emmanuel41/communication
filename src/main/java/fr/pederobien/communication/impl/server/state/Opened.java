package fr.pederobien.communication.impl.server.state;

import fr.pederobien.communication.event.NewClientEvent;
import fr.pederobien.communication.impl.Communication;
import fr.pederobien.communication.impl.server.Client;
import fr.pederobien.communication.interfaces.connection.IConnection;
import fr.pederobien.communication.interfaces.server.IClientInfo;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.Logger;

public class Opened<T> extends State<T> {
	private Thread waiter;
	private boolean closeRequested;

	public Opened(Context<T> context) {
		super(context);
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		if (isEnabled) {
			try {

				// Server implementation specific to open the server
				getContext().getImpl().open(getConfig());

				closeRequested = false;

				String name = String.format("[%s %s - waitForClient]", getConfig().getName(), getConfig().getPoint());
				waiter = new Thread(() -> waitForClient(), name);
				waiter.setDaemon(true);
				waiter.start();

				info("Server opened");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean close() {
		info("Closing server");
		closeRequested = true;
		waiter.interrupt();

		getContext().setState(getContext().getClosed());
		return true;
	}

	private void waitForClient() {
		while (!closeRequested) {
			IClientInfo<T> info = null;

			try {

				// Server implementation specific to wait for a new client
				info = getImpl().waitForClient();

				// The client is not allowed to be connected with the server
				if (closeRequested || !getConfig().getClientValidator().isValid(info.getEndPoint())) {
					info.getImpl().dispose();
					continue;
				}
			} catch (Exception e) {
				if (getContext().getCounter().increment()) {
					break;
				}
			}

			if (info != null && !closeRequested) {
				boolean initialised = false;
				IConnection connection = null;

				try {
					connection = Communication.createConnection(getConfig(), info.getEndPoint(), info.getImpl());
					initialised = connection.initialise();
				} catch (Exception e) {
					if (getContext().getCounter().increment()) {
						break;
					}
				}

				if (!initialised || closeRequested) {
					if (!initialised) {
						Logger.warning("[%s] - Initialisation failure", getContext().getName());
					}

					connection.setEnabled(false);
					connection.dispose();
				} else {
					// Notifying observers that a client is connected
					EventManager.callEvent(new NewClientEvent(new Client(getContext().getServer(), connection)));
				}
			}
		}
	}
}
