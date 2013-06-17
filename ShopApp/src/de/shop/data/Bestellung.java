package de.shop.data;

import java.io.Serializable;
import java.util.Date;

public class Bestellung implements Serializable {
	private static final long serialVersionUID = -3227854872557641281L;
	public Long id;
	public Date datum;

	public Bestellung() {
		super();
	}

	public Bestellung(Long id, Date datum) {
		super();
		this.id = id;
		this.datum = datum;
	}

	@Override
	public String toString() {
		return "Bestellung [id=" + id + ", datum=" + datum + "]";
	}
}
