package fr.pederobien.communication.impl.server.state;

import fr.pederobien.communication.event.ServerDisposeEvent;
import fr.pederobien.utils.event.EventManager;

public class Disposed<T> extends State<T> {

	public Disposed(Context<T> context) {
		super(context);
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		if (isEnabled) {

			EventManager.callEvent(new ServerDisposeEvent(getContext().getServer()));
			info("Server disposed");
		}
	}
}
