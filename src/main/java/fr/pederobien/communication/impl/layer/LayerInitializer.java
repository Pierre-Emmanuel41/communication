package fr.pederobien.communication.impl.layer;

import fr.pederobien.communication.interfaces.IToken;
import fr.pederobien.communication.interfaces.layer.ILayer;
import fr.pederobien.communication.interfaces.layer.ILayerInitializer;
import fr.pederobien.communication.interfaces.layer.IStep;

public class LayerInitializer implements ILayerInitializer {
	private ILayer initialisation;
	private IStep[] steps;
	private ILayerInitializer impl;

	/**
	 * Creates a layer initializer.
	 * 
	 * @param initialisation The layer to use for initialisation.
	 * @param steps A sequence to perform additional steps during initialisation.
	 */
	public LayerInitializer(ILayer initialisation, IStep... steps) {
		this.initialisation = initialisation;
		this.steps = steps;

		impl = new NotInitializedState(initialisation);
	}

	/**
	 * Creates a layer initializer with no initialisation sequence.
	 * The given layer should not need to exchange information with the remote.
	 * 
	 * @param layer The layer, already initialized.
	 */
	public LayerInitializer(ILayer layer) {
		this(layer, token -> layer);
	}

	/**
	 * Creates a layer initializer for a {@link SimpleLayer}
	 * The given layer should not need to exchange information with the remote.
	 * 
	 * @param layer The layer, already initialized.
	 */
	public LayerInitializer() {
		this(new SimpleLayer());
	}

	@Override
	public boolean initialize(IToken token) throws Exception {
		return impl.initialize(token);
	}

	@Override
	public ILayer getLayer() {
		return impl.getLayer();
	}

	@Override
	public ILayerInitializer copy() {
		return new LayerInitializer(initialisation, steps);
	}

	private class NotInitializedState implements ILayerInitializer {
		private ILayer layer;

		public NotInitializedState(ILayer layer) {
			this.layer = layer;
		}

		@Override
		public boolean initialize(IToken token) throws Exception {
			for (int i = 0; (i < steps.length) && (layer != null); i++)
				layer = steps[i].apply(token);

			if (layer == null)
				return false;

			impl = new InitializedState(layer);
			return true;
		}

		@Override
		public ILayer getLayer() {
			return layer;
		}

		@Override
		public ILayerInitializer copy() {
			return this;
		}
	}

	private class InitializedState implements ILayerInitializer {
		private ILayer initialized;

		public InitializedState(ILayer initialized) {
			this.initialized = initialized;
		}

		@Override
		public boolean initialize(IToken token) throws Exception {
			return true;
		}

		@Override
		public ILayer getLayer() {
			return initialized;
		}

		@Override
		public ILayerInitializer copy() {
			return this;
		}
	}
}
