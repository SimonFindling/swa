package de.shop.artikelverwaltung.service;

import de.shop.util.AbstractShopException;

public abstract class AbstractArtikelverwaltungException extends AbstractShopException {
	private static final long serialVersionUID = 8488136763543303107L;

	public AbstractArtikelverwaltungException(String msg) {
		super(msg);
	}
	
	public AbstractArtikelverwaltungException(String msg, Throwable t) {
		super(msg, t);
	}
}