package fr.pederobien.communication.impl.server.state;

import fr.pederobien.communication.event.ServerCloseEvent;
import fr.pederobien.utils.event.EventManager;

public class Closed<T> extends State<T> {

	public Closed(Context<T> context) {
		super(context);
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		if (isEnabled) {
			try {

				// Server specific implementation to close the server
				getContext().getImpl().close();

				EventManager.callEvent(new ServerCloseEvent(getContext().getServer()));
				info("Server closed");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean open() {
		info("Opening server");
		getContext().setState(getContext().getOpened());
		return true;
	}

	@Override
	public boolean dispose() {
		info("Disposing server");
		getContext().setState(getContext().getDisposed());
		return true;
	}
}
