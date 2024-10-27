package fr.pederobien.communication.impl.layer;

import java.util.function.Function;

import fr.pederobien.communication.interfaces.IToken;
import fr.pederobien.communication.interfaces.layer.ILayer;
import fr.pederobien.communication.interfaces.layer.ILayerInitializer;

public class LayerInitializer implements ILayerInitializer {
	private ILayer initialisation;
	private Function<IToken, ILayer> initialisationSequence;
	private ILayerInitializer impl;
	
	/**
	 * Creates a layer initializer.
	 * 
	 * @param initialisation The layer to use for initialisation.
	 * @param initialisationSequence A sequence to perform additional steps during initialisation.
	 */
	public LayerInitializer(ILayer initialisation, Function<IToken, ILayer> initialisationSequence) {
		this.initialisation = initialisation;
		this.initialisationSequence = initialisationSequence;

		impl = new NotInitializedState();
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
		return new LayerInitializer(initialisation, initialisationSequence);
	}
	
	private class NotInitializedState implements ILayerInitializer {

		@Override
		public boolean initialize(IToken token) throws Exception {
			ILayer initialized = initialisationSequence.apply(token);
			if (initialized == null)
				return false;

			impl = new InitializedState(initialized);
			return true;
		}

		@Override
		public ILayer getLayer() {
			return initialisation;
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
