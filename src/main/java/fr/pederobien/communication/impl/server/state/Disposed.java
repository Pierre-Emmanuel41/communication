package fr.pederobien.communication.impl.server.state;

import fr.pederobien.communication.event.ServerDisposeEvent;
import fr.pederobien.utils.event.EventManager;

public class Disposed<T, U> extends State<T, U> {

	public Disposed(Context<T, U> context) {
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
