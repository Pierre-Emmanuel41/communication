package fr.pederobien.communication.impl.server.state;

public class Disposed<T> extends State<T> {

	public Disposed(Context<T> context) {
		super(context);
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		if (isEnabled) {
			info("Server disposed");
		}
	}
}
