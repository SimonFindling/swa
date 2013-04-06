package de.shop.util;

import static javax.xml.bind.annotation.XmlAccessType.FIELD;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "long")
@XmlAccessorType(FIELD)
public class RestLongWrapper {
	@XmlAttribute
	private Long v;

	public RestLongWrapper() {
		super();
	}

	public RestLongWrapper(Long v) {
		super();
		this.v = v;
	}

	public Long getV() {
		return v;
	}

	public void setV(Long v) {
		this.v = v;
	}
}