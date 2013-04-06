package de.shop.util;

import static javax.xml.bind.annotation.XmlAccessType.FIELD;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "str")
@XmlAccessorType(FIELD)
public class RestStringWrapper {
	@XmlAttribute
	private String v;

	public RestStringWrapper() {
		super();
	}

	public RestStringWrapper(String v) {
		super();
		this.v = v;
	}

	public String getV() {
		return v;
	}

	public void setV(String v) {
		this.v = v;
	}
}