package fr.pederobien.communication.impl.client.state;

public class Disposed extends State {

	/**
	 * Create a state where the client is disposed.
	 * 
	 * @param context The context of this state.
	 */
	public Disposed(Context context) {
		super(context);
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		if (isEnabled) {
			onLogEvent("Client disposed");
		}
	}

	@Override
	public void dispose() {
		throw new IllegalStateException("Client already disposed");
	}
}
