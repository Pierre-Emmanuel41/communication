package fr.pederobien.communication.impl.client.state;

public class Disposed<T> extends State<T> {

	/**
	 * Create a state where the client is disposed.
	 *
	 * @param context The context of this state.
	 */
	public Disposed(Context<T> context) {
		super(context);
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		if (isEnabled) {
			info("Client disposed");
		}
	}

	@Override
	public void dispose() {
		throw new IllegalStateException("Client already disposed");
	}
}
