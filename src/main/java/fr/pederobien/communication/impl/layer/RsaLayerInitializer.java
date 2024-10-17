package fr.pederobien.communication.impl.layer;

import fr.pederobien.communication.impl.keyexchange.RsaKeyExchange;
import fr.pederobien.communication.interfaces.ICertificate;

public class RsaLayerInitializer extends LayerInitializer {
	
	public RsaLayerInitializer(ICertificate certificate) {
		super(new CertifiedLayer(certificate), token -> new RsaKeyExchange(token).exchange());
	}
}
